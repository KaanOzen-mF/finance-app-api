package com.finance.app.controller;

import com.finance.app.model.Budget;
import com.finance.app.model.User;
import com.finance.app.repository.UserRepository;
import com.finance.app.service.BudgetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;

    public BudgetController(BudgetService budgetService, UserRepository userRepository) {
        this.budgetService = budgetService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();

        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @PostMapping
    public ResponseEntity<Budget> createBudget(@RequestBody Budget budget) {
        User user = getAuthenticatedUser();
        Budget createdBudget = budgetService.createBudget(budget, user);
        return new ResponseEntity<>(createdBudget, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Budget>> getAllBudgets() {
        User user = getAuthenticatedUser();
        List<Budget> budgets = budgetService.findBudgetsByUserId(user.getId());
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudgetById(@PathVariable UUID id) {
        User user = getAuthenticatedUser();
        Budget budget = budgetService.findById(id);

        // Kullanıcının sadece kendi bütçesine erişebildiğini kontrol et
        if (budget != null && budget.getUser().getId().equals(user.getId())) {
            return ResponseEntity.ok(budget);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Budget> updateBudget(@PathVariable UUID id, @RequestBody Budget updatedBudget) {
        User user = getAuthenticatedUser();
        Budget existingBudget = budgetService.findById(id);

        // Kullanıcının sadece kendi bütçesini güncelleyebildiğini kontrol et
        if (existingBudget != null && existingBudget.getUser().getId().equals(user.getId())) {
            updatedBudget.setUser(user); // Kullanıcıyı set et
            Budget result = budgetService.updateBudget(id, updatedBudget);
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable UUID id) {
        User user = getAuthenticatedUser();
        Budget existingBudget = budgetService.findById(id);

        // Kullanıcının sadece kendi bütçesini silebildiğini kontrol et
        if (existingBudget != null && existingBudget.getUser().getId().equals(user.getId())) {
            budgetService.deleteBudget(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}