package com.finance.app.service;

import com.finance.app.model.Budget;
import com.finance.app.model.User;
import com.finance.app.repository.BudgetRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public Budget createBudget(Budget budget, User user) {
        budget.setUser(user);
        return budgetRepository.save(budget);
    }

    public List<Budget> findBudgetsByUserId(UUID userId) {
        return budgetRepository.findByUserId(userId);
    }

    public Budget findById(UUID id) {
        return budgetRepository.findById(id).orElse(null);
    }

    public Budget updateBudget(UUID id, Budget updatedBudget) {
        return budgetRepository.findById(id)
                .map(budget -> {
                    budget.setName(updatedBudget.getName());
                    budget.setCategory(updatedBudget.getCategory());
                    budget.setAmount(updatedBudget.getAmount());
                    budget.setStartDate(updatedBudget.getStartDate());
                    budget.setEndDate(updatedBudget.getEndDate());
                    return budgetRepository.save(budget);
                })
                .orElse(null);
    }

    public void deleteBudget(UUID id) {
        budgetRepository.deleteById(id);
    }
}