package com.msel.app.controller;

import com.msel.app.dto.CreateTransactionRequest;
import com.msel.app.dto.TransactionDTO;
import com.msel.app.service.TransactionService;
import com.msel.app.util.StructuredLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final StructuredLogger structuredLogger;

    public TransactionController(TransactionService transactionService, StructuredLogger structuredLogger) {
        this.transactionService = transactionService;
        this.structuredLogger = structuredLogger;
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody CreateTransactionRequest request, HttpServletRequest httpRequest) {
        String requestId = (String) httpRequest.getAttribute("X-Request-ID");
        Map<String, Object> context = new HashMap<>();
        context.put("action", "create_transaction");
        context.put("user_id", request.getUserId());
        context.put("amount", request.getAmount());
        context.put("currency", request.getCurrency());
        structuredLogger.logEvent("transaction_creation_started", requestId, context);
        
        TransactionDTO transactionDTO = transactionService.createTransaction(request);
        
        context.put("transaction_id", transactionDTO.getId());
        context.put("status", transactionDTO.getStatus());
        structuredLogger.logEvent("transaction_created", requestId, context);
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionDTO);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable UUID transactionId, HttpServletRequest httpRequest) {
        String requestId = (String) httpRequest.getAttribute("X-Request-ID");
        Map<String, Object> context = new HashMap<>();
        context.put("action", "get_transaction");
        context.put("transaction_id", transactionId);
        structuredLogger.logEvent("transaction_fetch_started", requestId, context);
        
        TransactionDTO transactionDTO = transactionService.getTransactionById(transactionId);
        
        structuredLogger.logEvent("transaction_fetched", requestId, context);
        return ResponseEntity.ok(transactionDTO);
    }

    @PostMapping("/{transactionId}/validate")
    public ResponseEntity<TransactionDTO> validateTransaction(@PathVariable UUID transactionId, HttpServletRequest httpRequest) {
        String requestId = (String) httpRequest.getAttribute("X-Request-ID");
        Map<String, Object> context = new HashMap<>();
        context.put("action", "validate_transaction");
        context.put("transaction_id", transactionId);
        structuredLogger.logEvent("transaction_validation_started", requestId, context);
        
        TransactionDTO transactionDTO = transactionService.validateTransaction(transactionId);
        
        context.put("status", transactionDTO.getStatus());
        structuredLogger.logEvent("transaction_validated", requestId, context);
        return ResponseEntity.ok(transactionDTO);
    }

    @PostMapping("/{transactionId}/fail")
    public ResponseEntity<TransactionDTO> failTransaction(@PathVariable UUID transactionId, HttpServletRequest httpRequest) {
        String requestId = (String) httpRequest.getAttribute("X-Request-ID");
        Map<String, Object> context = new HashMap<>();
        context.put("action", "fail_transaction");
        context.put("transaction_id", transactionId);
        structuredLogger.logEvent("transaction_fail_started", requestId, context);
        
        TransactionDTO transactionDTO = transactionService.failTransaction(transactionId);
        
        context.put("status", transactionDTO.getStatus());
        structuredLogger.logEvent("transaction_failed", requestId, context);
        return ResponseEntity.ok(transactionDTO);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionDTO>> getUserTransactions(@PathVariable UUID userId, HttpServletRequest httpRequest) {
        String requestId = (String) httpRequest.getAttribute("X-Request-ID");
        Map<String, Object> context = new HashMap<>();
        context.put("action", "get_user_transactions");
        context.put("user_id", userId);
        structuredLogger.logEvent("user_transactions_fetch_started", requestId, context);
        
        List<TransactionDTO> transactions = transactionService.getUserTransactions(userId);
        
        context.put("transaction_count", transactions.size());
        structuredLogger.logEvent("user_transactions_fetched", requestId, context);
        return ResponseEntity.ok(transactions);
    }
}
