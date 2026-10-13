package com.udea.lab12026p.service;

import com.udea.lab12026p.dto.CustomerDTO;
import com.udea.lab12026p.entity.Customer;
import com.udea.lab12026p.mapper.CustomerMapper;
import com.udea.lab12026p.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService service;

    private final Customer customer = new Customer(1L, "111", "Ana", "Gomez", 1000.0);
    private final CustomerDTO customerDTO = new CustomerDTO(1L, "Ana", "Gomez", "111", 1000.0);

    @Test
    void getAllCustomers_devuelveLista() {
        when(customerRepository.findAll()).thenReturn(List.of(customer));
        when(customerMapper.toDTO(customer)).thenReturn(customerDTO);

        List<CustomerDTO> result = service.getAllCustomers();

        assertEquals(1, result.size());
        assertEquals("Ana", result.get(0).getFirstName());
    }

    @Test
    void getCustomerById_existe() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerMapper.toDTO(customer)).thenReturn(customerDTO);

        assertEquals("111", service.getCustomerById(1L).getAccountNumber());
    }

    @Test
    void getCustomerById_noExiste_lanzaExcepcion() {
        when(customerRepository.findById(9L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getCustomerById(9L));
        assertEquals("Cliente no encontrado", ex.getMessage());
    }

    @Test
    void createCustomer_guardaYDevuelveDTO() {
        when(customerMapper.toEntity(customerDTO)).thenReturn(customer);
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerMapper.toDTO(any(Customer.class))).thenReturn(customerDTO);

        assertEquals(1000.0, service.createCustomer(customerDTO).getBalance());
    }
}
