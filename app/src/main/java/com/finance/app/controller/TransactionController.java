package com.finance.app.controller;

import com.finance.app.model.Transaction;
import com.finance.app.model.User;
import com.finance.app.repository.UserRepository;
import com.finance.app.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    public TransactionController(TransactionService transactionService, UserRepository userRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        User user = getAuthenticatedUser();
        Transaction createdTransaction = transactionService.createTransaction(transaction, user);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        User user = getAuthenticatedUser();
        List<Transaction> transactions = transactionService.findTransactionsByUserId(user.getId());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable UUID id) {
        User user = getAuthenticatedUser();
        Transaction transaction = transactionService.findById(id);

        if (transaction != null && transaction.getUser().getId().equals(user.getId())) {
            return ResponseEntity.ok(transaction);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable UUID id, @RequestBody Transaction updatedTransaction) {
        User user = getAuthenticatedUser();
        Transaction existingTransaction = transactionService.findById(id);

        if (existingTransaction != null && existingTransaction.getUser().getId().equals(user.getId())) {
            updatedTransaction.setUser(user);
            Transaction result = transactionService.updateTransaction(id, updatedTransaction);
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id) {
        User user = getAuthenticatedUser();
        Transaction existingTransaction = transactionService.findById(id);

        if (existingTransaction != null && existingTransaction.getUser().getId().equals(user.getId())) {
            transactionService.deleteTransaction(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}