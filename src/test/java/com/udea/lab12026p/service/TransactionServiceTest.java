package com.udea.lab12026p.service;

import com.udea.lab12026p.dto.TransactionDTO;
import com.udea.lab12026p.entity.Customer;
import com.udea.lab12026p.entity.Transaction;
import com.udea.lab12026p.repository.CustomerRepository;
import com.udea.lab12026p.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private TransactionService service;

    private Customer sender;
    private Customer receiver;

    @BeforeEach
    void setUp() {
        sender = new Customer(1L, "A", "Ana", "Gomez", 100.0);
        receiver = new Customer(2L, "B", "Beto", "Perez", 50.0);
    }

    private TransactionDTO dto(String from, String to, Double amount) {
        TransactionDTO d = new TransactionDTO();
        d.setSenderAccountNumber(from);
        d.setReceiverAccountNumber(to);
        d.setAmount(amount);
        return d;
    }

    private Transaction existing(String from, String to, double amount) {
        return new Transaction(1L, from, to, amount, LocalDateTime.now());
    }

    // ---------------------------------------------------------------- transferMoney

    @Test
    void transferMoney_ok_actualizaSaldosYGuardaTransaccion() {
        when(customerRepository.findByAccountNumber("A")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("B")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });

        TransactionDTO result = service.transferMoney(dto("A", "B", 30.0));

        assertEquals(70.0, sender.getBalance());
        assertEquals(80.0, receiver.getBalance());
        assertEquals(10L, result.getId());
        assertEquals("A", result.getSenderAccountNumber());
        assertEquals("B", result.getReceiverAccountNumber());
        assertEquals(30.0, result.getAmount());
        assertNotNull(result.getTimestamp());
        verify(customerRepository).save(sender);
        verify(customerRepository).save(receiver);
    }

    @Test
    void transferMoney_cuentasNulas_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> service.transferMoney(dto(null, "B", 10.0)));
        assertThrows(IllegalArgumentException.class, () -> service.transferMoney(dto("A", null, 10.0)));
    }

    @Test
    void transferMoney_remitenteNoExiste_lanzaExcepcion() {
        when(customerRepository.findByAccountNumber("A")).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.transferMoney(dto("A", "B", 10.0)));
        assertTrue(ex.getMessage().contains("remitente"));
    }

    @Test
    void transferMoney_receptorNoExiste_lanzaExcepcion() {
        when(customerRepository.findByAccountNumber("A")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("B")).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.transferMoney(dto("A", "B", 10.0)));
        assertTrue(ex.getMessage().contains("receptor"));
    }

    @Test
    void transferMoney_montoInvalido_lanzaExcepcion() {
        when(customerRepository.findByAccountNumber("A")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("B")).thenReturn(Optional.of(receiver));
        assertThrows(IllegalArgumentException.class, () -> service.transferMoney(dto("A", "B", -5.0)));
        assertThrows(IllegalArgumentException.class, () -> service.transferMoney(dto("A", "B", 0.0)));
        assertThrows(IllegalArgumentException.class, () -> service.transferMoney(dto("A", "B", null)));
    }

    @Test
    void transferMoney_saldoInsuficiente_lanzaExcepcion() {
        when(customerRepository.findByAccountNumber("A")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("B")).thenReturn(Optional.of(receiver));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.transferMoney(dto("A", "B", 1000.0)));
        assertTrue(ex.getMessage().contains("Saldo insuficiente"));
        assertEquals(100.0, sender.getBalance());
        assertEquals(50.0, receiver.getBalance());
    }

    // ---------------------------------------------------------------- getTransactionsForAccount

    @Test
    void getTransactionsForAccount_devuelveDTOs() {
        when(transactionRepository.findBySenderAccountNumberOrReceiverAccountNumber("A", "A"))
                .thenReturn(List.of(existing("A", "B", 30.0)));

        List<TransactionDTO> result = service.getTransactionsForAccount("A");

        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getSenderAccountNumber());
        assertEquals(30.0, result.get(0).getAmount());
    }

    // ---------------------------------------------------------------- updateTransaction

    @Test
    void updateTransaction_ok_revierteYReaplica() {
        // Estado tras una transferencia original de 30: A=70, B=80
        sender.setBalance(70.0);
        receiver.setBalance(80.0);
        Transaction tx = existing("A", "B", 30.0);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));
        when(customerRepository.findByAccountNumber("A")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("B")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionDTO result = service.updateTransaction(1L, dto("A", "B", 20.0));

        // Revertir: A=100, B=50. Aplicar 20: A=80, B=70
        assertEquals(80.0, sender.getBalance());
        assertEquals(70.0, receiver.getBalance());
        assertEquals(20.0, result.getAmount());
        assertEquals(1L, result.getId());
    }

    @Test
    void updateTransaction_noExiste_lanzaNoSuchElement() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.updateTransaction(99L, dto("A", "B", 10.0)));
    }

    @Test
    void updateTransaction_cuentasNulas_lanzaExcepcion() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing("A", "B", 30.0)));
        assertThrows(IllegalArgumentException.class, () -> service.updateTransaction(1L, dto(null, "B", 10.0)));
    }

    @Test
    void updateTransaction_montoInvalido_lanzaExcepcion() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing("A", "B", 30.0)));
        assertThrows(IllegalArgumentException.class, () -> service.updateTransaction(1L, dto("A", "B", 0.0)));
    }

    @Test
    void updateTransaction_remitenteOriginalNoExiste_lanzaExcepcion() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing("A", "B", 30.0)));
        when(customerRepository.findByAccountNumber("A")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.updateTransaction(1L, dto("A", "B", 10.0)));
    }

    @Test
    void updateTransaction_receptorOriginalNoExiste_lanzaExcepcion() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing("A", "B", 30.0)));
        when(customerRepository.findByAccountNumber("A")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("B")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.updateTransaction(1L, dto("A", "B", 10.0)));
    }

    @Test
    void updateTransaction_nuevoRemitenteNoExiste_lanzaExcepcion() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing("A", "B", 30.0)));
        when(customerRepository.findByAccountNumber("A")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("B")).thenReturn(Optional.of(receiver));
        when(customerRepository.findByAccountNumber("X")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.updateTransaction(1L, dto("X", "B", 10.0)));
    }

    @Test
    void updateTransaction_nuevoReceptorNoExiste_lanzaExcepcion() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing("A", "B", 30.0)));
        when(customerRepository.findByAccountNumber("A")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("B")).thenReturn(Optional.of(receiver));
        when(customerRepository.findByAccountNumber("X")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.updateTransaction(1L, dto("A", "X", 10.0)));
    }

    @Test
    void updateTransaction_saldoInsuficiente_lanzaExcepcion() {
        sender.setBalance(0.0);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing("A", "B", 30.0)));
        when(customerRepository.findByAccountNumber("A")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("B")).thenReturn(Optional.of(receiver));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateTransaction(1L, dto("A", "B", 100.0)));
        assertTrue(ex.getMessage().contains("Saldo insuficiente"));
    }

    // ---------------------------------------------------------------- deleteTransaction

    @Test
    void deleteTransaction_ok_revierteSaldosYElimina() {
        sender.setBalance(70.0);
        receiver.setBalance(80.0);
        Transaction tx = existing("A", "B", 30.0);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));
        when(customerRepository.findByAccountNumber("A")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("B")).thenReturn(Optional.of(receiver));

        service.deleteTransaction(1L);

        assertEquals(100.0, sender.getBalance());
        assertEquals(50.0, receiver.getBalance());
        verify(transactionRepository).delete(tx);
    }

    @Test
    void deleteTransaction_noExiste_lanzaNoSuchElement() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.deleteTransaction(99L));
    }

    @Test
    void deleteTransaction_remitenteNoExiste_lanzaExcepcion() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing("A", "B", 30.0)));
        when(customerRepository.findByAccountNumber("A")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.deleteTransaction(1L));
    }

    @Test
    void deleteTransaction_receptorNoExiste_lanzaExcepcion() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing("A", "B", 30.0)));
        when(customerRepository.findByAccountNumber("A")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("B")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.deleteTransaction(1L));
    }
}
