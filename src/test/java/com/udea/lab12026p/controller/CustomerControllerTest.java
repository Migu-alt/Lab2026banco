package com.udea.lab12026p.controller;

import com.udea.lab12026p.dto.CustomerDTO;
import com.udea.lab12026p.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController controller;

    private final CustomerDTO dto = new CustomerDTO(1L, "Ana", "Gomez", "111", 1000.0);

    @Test
    void getAllCustomers_devuelve200() {
        when(customerService.getAllCustomers()).thenReturn(List.of(dto));
        var response = controller.getAllCustomers();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getCustomerById_devuelve200() {
        when(customerService.getCustomerById(1L)).thenReturn(dto);
        assertEquals("111", controller.getCustomerById(1L).getBody().getAccountNumber());
    }

    @Test
    void createCustomer_ok() {
        when(customerService.createCustomer(dto)).thenReturn(dto);
        assertEquals(HttpStatus.OK, controller.createCustomer(dto).getStatusCode());
    }

    @Test
    void createCustomer_sinBalance_lanzaExcepcion() {
        CustomerDTO sinSaldo = new CustomerDTO(null, "Ana", "Gomez", "111", null);
        assertThrows(IllegalArgumentException.class, () -> controller.createCustomer(sinSaldo));
    }
}
