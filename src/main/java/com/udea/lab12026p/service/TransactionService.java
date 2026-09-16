package com.udea.lab12026p.service;

import com.udea.lab12026p.dto.TransactionDTO;
import com.udea.lab12026p.entity.Customer;
import com.udea.lab12026p.entity.Transaction;
import com.udea.lab12026p.repository.CustomerRepository;
import com.udea.lab12026p.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerRepository customerRepository; // Para validar cuentas

    @Transactional
    public TransactionDTO transferMoney(TransactionDTO transactionDTO) {
        // Validar que los números de cuenta no sean nulos
        if (transactionDTO.getSenderAccountNumber() == null || transactionDTO.getReceiverAccountNumber() == null) {
            throw new IllegalArgumentException("Los números de cuenta del remitente y receptor son obligatorios.");
        }

        // Buscar los clientes por número de cuenta
        Customer sender = customerRepository.findByAccountNumber(transactionDTO.getSenderAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta del remitente no existe."));
        Customer receiver = customerRepository.findByAccountNumber(transactionDTO.getReceiverAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta del receptor no existe."));
//    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found"));

        // Validar que el monto sea válido y positivo
        if (transactionDTO.getAmount() == null || transactionDTO.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto de la transacción debe ser positivo.");
        }

        // Validar que el remitente tenga saldo suficiente
        if (sender.getBalance() < transactionDTO.getAmount()) {
            throw new IllegalArgumentException("Saldo insuficiente en la cuenta del remitente.");
            //throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }

        // Realizar la transferencia
        sender.setBalance(sender.getBalance() - transactionDTO.getAmount());
        receiver.setBalance(receiver.getBalance() + transactionDTO.getAmount());

        // Guardar los cambios en las cuentas
        customerRepository.save(sender);
        customerRepository.save(receiver);


        // Crear y guardar la transacción
        Transaction transaction = new Transaction();
        transaction.setSenderAccountNumber(sender.getAccountNumber());
        transaction.setReceiverAccountNumber(receiver.getAccountNumber());
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setTimestamp(LocalDateTime.now());

        transaction = transactionRepository.save(transaction);

        // Devolver la transacción creada como DTO
        TransactionDTO savedTransaction = new TransactionDTO();
        savedTransaction.setId(transaction.getId());
        savedTransaction.setSenderAccountNumber(transaction.getSenderAccountNumber());
        savedTransaction.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
        savedTransaction.setAmount(transaction.getAmount());
        savedTransaction.setTimestamp(transaction.getTimestamp());

        return savedTransaction;
    }

    public List<TransactionDTO> getTransactionsForAccount(String accountNumber) {
        List<Transaction> transactions = transactionRepository.findBySenderAccountNumberOrReceiverAccountNumber(accountNumber, accountNumber);
        return transactions.stream().map(transaction -> {
            TransactionDTO dto = new TransactionDTO();
            dto.setId(transaction.getId());
            dto.setSenderAccountNumber(transaction.getSenderAccountNumber());
            dto.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
            dto.setAmount(transaction.getAmount());
            dto.setTimestamp(transaction.getTimestamp());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Actualiza una transacción existente.
     *
     * Una transacción representa una transferencia que ya modificó los saldos
     * de las cuentas involucradas, por lo que actualizar únicamente el registro
     * dejaría los saldos inconsistentes. Por eso, antes de aplicar los nuevos
     * valores se revierte el efecto de la transacción original (incluso si
     * cambian las cuentas origen/destino) y luego se aplica el nuevo efecto.
     *
     * Toda la operación es atómica: si alguna validación falla, no se persiste
     * ningún cambio (ni en las cuentas ni en la transacción).
     */
    @Transactional
    public TransactionDTO updateTransaction(Long id, TransactionDTO transactionDTO) {
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("La transacción con id " + id + " no existe."));

        if (transactionDTO.getSenderAccountNumber() == null || transactionDTO.getReceiverAccountNumber() == null) {
            throw new IllegalArgumentException("Los números de cuenta del remitente y receptor son obligatorios.");
        }
        if (transactionDTO.getAmount() == null || transactionDTO.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto de la transacción debe ser positivo.");
        }

        // 1) Revertir el efecto de la transacción original sobre los saldos.
        Customer originalSender = customerRepository.findByAccountNumber(existing.getSenderAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta remitente original ya no existe."));
        Customer originalReceiver = customerRepository.findByAccountNumber(existing.getReceiverAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta receptora original ya no existe."));

        originalSender.setBalance(originalSender.getBalance() + existing.getAmount());
        originalReceiver.setBalance(originalReceiver.getBalance() - existing.getAmount());

        // 2) Validar las cuentas de la nueva transacción.
        //    Como Hibernate mantiene una única instancia por entidad dentro del
        //    mismo contexto de persistencia, si la nueva cuenta coincide con
        //    originalSender/originalReceiver, se reutiliza la misma instancia
        //    ya actualizada en el paso anterior (evitando sobrescribir el ajuste).
        Customer newSender = customerRepository.findByAccountNumber(transactionDTO.getSenderAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta del remitente no existe."));
        Customer newReceiver = customerRepository.findByAccountNumber(transactionDTO.getReceiverAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta del receptor no existe."));

        // 3) Validar saldo suficiente y aplicar el nuevo efecto.
        if (newSender.getBalance() < transactionDTO.getAmount()) {
            throw new IllegalArgumentException("Saldo insuficiente en la cuenta del remitente para el nuevo monto.");
        }

        newSender.setBalance(newSender.getBalance() - transactionDTO.getAmount());
        newReceiver.setBalance(newReceiver.getBalance() + transactionDTO.getAmount());

        customerRepository.save(originalSender);
        customerRepository.save(originalReceiver);
        customerRepository.save(newSender);
        customerRepository.save(newReceiver);

        // 4) Actualizar y guardar la transacción.
        existing.setSenderAccountNumber(newSender.getAccountNumber());
        existing.setReceiverAccountNumber(newReceiver.getAccountNumber());
        existing.setAmount(transactionDTO.getAmount());
        Transaction updated = transactionRepository.save(existing);

        TransactionDTO result = new TransactionDTO();
        result.setId(updated.getId());
        result.setSenderAccountNumber(updated.getSenderAccountNumber());
        result.setReceiverAccountNumber(updated.getReceiverAccountNumber());
        result.setAmount(updated.getAmount());
        result.setTimestamp(updated.getTimestamp());
        return result;
    }

    /**
     * Elimina una transacción revirtiendo primero su efecto sobre los saldos
     * de las cuentas involucradas, para que la eliminación no deje balances
     * inconsistentes.
     */
    @Transactional
    public void deleteTransaction(Long id) {
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("La transacción con id " + id + " no existe."));

        Customer sender = customerRepository.findByAccountNumber(existing.getSenderAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta del remitente no existe."));
        Customer receiver = customerRepository.findByAccountNumber(existing.getReceiverAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta del receptor no existe."));

        sender.setBalance(sender.getBalance() + existing.getAmount());
        receiver.setBalance(receiver.getBalance() - existing.getAmount());

        customerRepository.save(sender);
        customerRepository.save(receiver);

        transactionRepository.delete(existing);
    }
}