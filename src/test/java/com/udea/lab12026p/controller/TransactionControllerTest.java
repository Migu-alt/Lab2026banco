package com.udea.lab12026p.controller;

import com.udea.lab12026p.dto.TransactionDTO;
import com.udea.lab12026p.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController controller;

    private final TransactionDTO dto = new TransactionDTO();

    @Test
    void transferMoney_ok_devuelve200() {
        when(transactionService.transferMoney(dto)).thenReturn(dto);
        ResponseEntity<?> response = controller.transferMoney(dto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void transferMoney_error_devuelve400() {
        when(transactionService.transferMoney(dto)).thenThrow(new IllegalArgumentException("monto invalido"));
        ResponseEntity<?> response = controller.transferMoney(dto);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("monto invalido", response.getBody());
    }

    @Test
    void getTransactionsByAccount_devuelveLista() {
        when(transactionService.getTransactionsForAccount("A")).thenReturn(List.of(dto));
        assertEquals(1, controller.getTransactionsByAccount("A").size());
    }

    @Test
    void updateTransaction_ok_devuelve200() {
        when(transactionService.updateTransaction(1L, dto)).thenReturn(dto);
        assertEquals(HttpStatus.OK, controller.updateTransaction(1L, dto).getStatusCode());
    }

    @Test
    void updateTransaction_noExiste_devuelve404() {
        when(transactionService.updateTransaction(1L, dto)).thenThrow(new NoSuchElementException("no existe"));
        assertEquals(HttpStatus.NOT_FOUND, controller.updateTransaction(1L, dto).getStatusCode());
    }

    @Test
    void updateTransaction_invalida_devuelve400() {
        when(transactionService.updateTransaction(1L, dto)).thenThrow(new IllegalArgumentException("mal"));
        assertEquals(HttpStatus.BAD_REQUEST, controller.updateTransaction(1L, dto).getStatusCode());
    }

    @Test
    void deleteTransaction_ok_devuelve204() {
        assertEquals(HttpStatus.NO_CONTENT, controller.deleteTransaction(1L).getStatusCode());
    }

    @Test
    void deleteTransaction_noExiste_devuelve404() {
        doThrow(new NoSuchElementException("no existe")).when(transactionService).deleteTransaction(1L);
        assertEquals(HttpStatus.NOT_FOUND, controller.deleteTransaction(1L).getStatusCode());
    }

    @Test
    void deleteTransaction_invalida_devuelve400() {
        doThrow(new IllegalArgumentException("mal")).when(transactionService).deleteTransaction(1L);
        assertEquals(HttpStatus.BAD_REQUEST, controller.deleteTransaction(1L).getStatusCode());
    }
}
