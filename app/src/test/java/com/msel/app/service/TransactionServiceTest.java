package com.msel.app.service;

import com.msel.app.dto.CreateTransactionRequest;
import com.msel.app.dto.TransactionDTO;
import com.msel.app.entity.Transaction;
import com.msel.app.entity.User;
import com.msel.app.repository.TransactionRepository;
import com.msel.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;
    private User testUser;
    private UUID testTransactionId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testTransactionId = UUID.randomUUID();

        testUser = User.builder()
                .id(testUserId)
                .name("John Doe")
                .email("john@example.com")
                .createdAt(LocalDateTime.now())
                .build();

        testTransaction = Transaction.builder()
                .id(testTransactionId)
                .userId(testUserId)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(Transaction.TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateTransaction() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .userId(testUserId)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .build();

        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionDTO result = transactionService.createTransaction(request);

        assertNotNull(result);
        assertEquals(testTransactionId, result.getId());
        assertEquals("100.00", result.getAmount().toString());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void testCreateTransactionUserNotFound() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .userId(testUserId)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .build();

        when(userRepository.existsById(testUserId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    void testGetTransactionById() {
        when(transactionRepository.findById(testTransactionId)).thenReturn(Optional.of(testTransaction));

        TransactionDTO result = transactionService.getTransactionById(testTransactionId);

        assertNotNull(result);
        assertEquals(testTransactionId, result.getId());
        assertEquals("100.00", result.getAmount().toString());
    }

    @Test
    void testGetTransactionByIdNotFound() {
        when(transactionRepository.findById(testTransactionId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> transactionService.getTransactionById(testTransactionId));
    }

    @Test
    void testValidateTransaction() {
        when(transactionRepository.findById(testTransactionId)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionDTO result = transactionService.validateTransaction(testTransactionId);

        assertNotNull(result);
        assertEquals(testTransactionId, result.getId());
    }

    @Test
    void testFailTransaction() {
        when(transactionRepository.findById(testTransactionId)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionDTO result = transactionService.failTransaction(testTransactionId);

        assertNotNull(result);
        assertEquals(testTransactionId, result.getId());
    }
}
