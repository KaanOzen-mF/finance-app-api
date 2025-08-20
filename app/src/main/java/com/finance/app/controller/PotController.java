package com.finance.app.controller;

import com.finance.app.model.Pot;
import com.finance.app.model.User;
import com.finance.app.repository.UserRepository;
import com.finance.app.service.PotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pots")
public class PotController {

    private final PotService potService;
    private final UserRepository userRepository;

    public PotController(PotService potService, UserRepository userRepository) {
        this.potService = potService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @PostMapping
    public ResponseEntity<Pot> createPot(@RequestBody Pot pot) {
        User user = getAuthenticatedUser();
        Pot createdPot = potService.createPot(pot, user);
        return new ResponseEntity<>(createdPot, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Pot>> getAllPots() {
        User user = getAuthenticatedUser();
        List<Pot> pots = potService.findPotsByUserId(user.getId());
        return ResponseEntity.ok(pots);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pot> getPotById(@PathVariable UUID id) {
        User user = getAuthenticatedUser();
        Pot pot = potService.findById(id);

        if (pot != null && pot.getUser().getId().equals(user.getId())) {
            return ResponseEntity.ok(pot);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pot> updatePot(@PathVariable UUID id, @RequestBody Pot updatedPot) {
        User user = getAuthenticatedUser();
        Pot existingPot = potService.findById(id);

        if (existingPot != null && existingPot.getUser().getId().equals(user.getId())) {
            updatedPot.setUser(user);
            Pot result = potService.updatePot(id, updatedPot);
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePot(@PathVariable UUID id) {
        User user = getAuthenticatedUser();
        Pot existingPot = potService.findById(id);

        if (existingPot != null && existingPot.getUser().getId().equals(user.getId())) {
            potService.deletePot(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/add-money")
    public ResponseEntity<Pot> addMoney(@PathVariable UUID id, @RequestBody Map<String, BigDecimal> body) {
        User user = getAuthenticatedUser();
        Pot pot = potService.findById(id);

        if (pot != null && pot.getUser().getId().equals(user.getId())) {
            BigDecimal amount = body.get("amount");
            Pot updatedPot = potService.addMoneyToPot(id, amount);
            return ResponseEntity.ok(updatedPot);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/withdraw-money")
    public ResponseEntity<Pot> withdrawMoney(@PathVariable UUID id, @RequestBody Map<String, BigDecimal> body) {
        User user = getAuthenticatedUser();
        Pot pot = potService.findById(id);

        if (pot != null && pot.getUser().getId().equals(user.getId())) {
            BigDecimal amount = body.get("amount");
            Pot updatedPot = potService.withdrawMoneyFromPot(id, amount);
            return ResponseEntity.ok(updatedPot);
        }
        return ResponseEntity.notFound().build();
    }
}