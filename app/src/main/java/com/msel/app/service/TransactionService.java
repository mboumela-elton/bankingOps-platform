package com.msel.app.service;

import com.msel.app.exception.ResourceNotFoundException;
import com.msel.app.dto.CreateTransactionRequest;
import com.msel.app.dto.TransactionDTO;
import com.msel.app.entity.Transaction;
import com.msel.app.entity.User;
import com.msel.app.repository.TransactionRepository;
import com.msel.app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                            UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public TransactionDTO createTransaction(CreateTransactionRequest request) {
        log.info("Creating new transaction for user: {} with amount: {} {}", 
            request.getUserId(), request.getAmount(), request.getCurrency());

        if (!userRepository.existsById(request.getUserId())) {
            log.warn("User not found for transaction creation: {}", request.getUserId());
            throw new ResourceNotFoundException("User not found");
        }

        Transaction transaction = Transaction.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created successfully with id: {} and status: {}", 
            savedTransaction.getId(), savedTransaction.getStatus());

        return convertToDTO(savedTransaction);
    }

    public TransactionDTO getTransactionById(UUID transactionId) {
        log.info("Fetching transaction with id: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.warn("Transaction not found with id: {}", transactionId);
                    return new ResourceNotFoundException("Transaction not found");
                });

        return convertToDTO(transaction);
    }

    public List<TransactionDTO> getUserTransactions(UUID userId) {
        log.info("Fetching all transactions for user: {}", userId);

        if (!userRepository.existsById(userId)) {
            log.warn("User not found for fetching transactions: {}", userId);
            throw new ResourceNotFoundException("User not found");
        }

        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        log.info("Found {} transactions for user: {}", transactions.size(), userId);

        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO validateTransaction(UUID transactionId) {
        log.info("Validating transaction with id: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.warn("Transaction not found for validation: {}", transactionId);
                    return new ResourceNotFoundException("Transaction not found");
                });

        if (!transaction.getStatus().equals(Transaction.TransactionStatus.PENDING)) {
            log.warn("Cannot validate transaction in status: {}", transaction.getStatus());
            throw new RuntimeException("Only PENDING transactions can be validated");
        }

        transaction.setStatus(Transaction.TransactionStatus.VALIDATED);
        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("Transaction validated successfully: {}", transactionId);

        return convertToDTO(updatedTransaction);
    }

    public TransactionDTO failTransaction(UUID transactionId) {
        log.info("Failing transaction with id: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.warn("Transaction not found for failure: {}", transactionId);
                    return new ResourceNotFoundException("Transaction not found");
                });

        if (!transaction.getStatus().equals(Transaction.TransactionStatus.PENDING)) {
            log.warn("Cannot fail transaction in status: {}", transaction.getStatus());
            throw new RuntimeException("Only PENDING transactions can be failed");
        }

        transaction.setStatus(Transaction.TransactionStatus.FAILED);
        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("Transaction failed successfully: {}", transactionId);

        return convertToDTO(updatedTransaction);
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus().toString())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
