package com.udea.lab12026p.mapper;

import com.udea.lab12026p.dto.CustomerDTO;
import com.udea.lab12026p.dto.TransactionDTO;
import com.udea.lab12026p.entity.Customer;
import com.udea.lab12026p.entity.Transaction;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapperTest {

    @Test
    void customerMapper_entidadADtoYViceversa() {
        Customer c = new Customer(1L, "111", "Ana", "Gomez", 500.0);
        CustomerDTO dto = CustomerMapper.INSTANCE.toDTO(c);
        assertEquals("Ana", dto.getFirstName());
        assertEquals(500.0, dto.getBalance());

        Customer back = CustomerMapper.INSTANCE.toEntity(dto);
        assertEquals("111", back.getAccountNumber());
    }

    @Test
    void transactionMapper_entidadADto() {
        Transaction t = new Transaction(5L, "A", "B", 10.0, LocalDateTime.now());
        TransactionDTO dto = TransactionMapper.INSTANCE.toDTO(t);
        assertEquals(5L, dto.getId());
        assertEquals("B", dto.getReceiverAccountNumber());
        assertEquals(10.0, dto.getAmount());
    }
}
