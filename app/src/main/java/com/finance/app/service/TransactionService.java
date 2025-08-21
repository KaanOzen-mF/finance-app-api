package com.finance.app.service;

import com.finance.app.model.Budget;
import com.finance.app.model.Transaction;
import com.finance.app.model.User;
import com.finance.app.repository.BudgetRepository;
import com.finance.app.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    // Budget, Budget Repository and Pot Services are injected to handle budget-related logic.

    private final BudgetService budgetService;
    private final PotService potService;
    private final BudgetRepository budgetRepository;

    public TransactionService(TransactionRepository transactionRepository, BudgetService budgetService, PotService potService, BudgetRepository budgetRepository) {
        this.transactionRepository = transactionRepository;
        this.budgetService = budgetService;
        this.potService = potService;
        this.budgetRepository = budgetRepository;
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction, User user) {
        transaction.setUser(user);
        Transaction savedTransaction = transactionRepository.save(transaction);

        // According to the expense type, update the related budget
        if ("EXPENSE".equalsIgnoreCase(savedTransaction.getType())) {
            // Find the related budget for the transaction
            List<Budget> userBudgets = budgetRepository.findByUserId(user.getId());
            Budget matchingBudget = userBudgets.stream()
                    .filter(b -> b.getCategory().equalsIgnoreCase(savedTransaction.getCategory()))
                    .filter(b -> !savedTransaction.getDate().isBefore(b.getStartDate()) && !savedTransaction.getDate().isAfter(b.getEndDate()))
                    .findFirst()
                    .orElse(null);

            if (matchingBudget != null) {
                // If related budget is found, update the current spent amount
                budgetService.updateCurrentSpent(matchingBudget.getId(), savedTransaction.getAmount());
            }
        }

        return savedTransaction;
    }

    public List<Transaction> findTransactionsByUserId(UUID userId) {
        return transactionRepository.findByUserId(userId);
    }

    public Transaction findById(UUID id) {
        return transactionRepository.findById(id).orElse(null);
    }

    @Transactional
    public Transaction updateTransaction(UUID id, Transaction updatedTransaction) {
        return transactionRepository.findById(id)
                .map(existingTransaction -> {
                    // Backup the existing transaction if it is an expense
                    if ("EXPENSE".equalsIgnoreCase(existingTransaction.getType())) {
                       // Find related budget and revert the expense
                    }

                    existingTransaction.setDescription(updatedTransaction.getDescription());
                    existingTransaction.setAmount(updatedTransaction.getAmount());
                    existingTransaction.setCategory(updatedTransaction.getCategory());
                    existingTransaction.setDate(updatedTransaction.getDate());
                    existingTransaction.setType(updatedTransaction.getType());

                    // Yeni harcamayı bütçeye yansıt
                    // Bütçe güncelleme mantığı buraya gelecek

                    return transactionRepository.save(existingTransaction);
                })
                .orElse(null);
    }

    @Transactional
    public void deleteTransaction(UUID id) {
        transactionRepository.findById(id)
                .ifPresent(existingTransaction -> {
                    // Eski harcamayı geri al
                    if ("EXPENSE".equalsIgnoreCase(existingTransaction.getType())) {
                        // İlgili bütçeyi bulma ve harcamayı geri alma mantığı buraya gelecek
                    }
                    transactionRepository.delete(existingTransaction);
                });
    }
}