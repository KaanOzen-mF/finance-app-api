package com.finance.app.service;

import com.finance.app.model.Pot;
import com.finance.app.model.User;
import com.finance.app.repository.PotRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PotService {

    private final PotRepository potRepository;

    public PotService(PotRepository potRepository) {
        this.potRepository = potRepository;
    }

    public Pot createPot(Pot pot, User user) {
        pot.setUser(user);
        return potRepository.save(pot);
    }

    public List<Pot> findPotsByUserId(UUID userId) {
        return potRepository.findByUserId(userId);
    }

    public Pot findById(UUID id) {
        return potRepository.findById(id).orElse(null);
    }

    @Transactional
    public Pot addMoneyToPot(UUID potId, BigDecimal amount) {
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with ID: " + potId));

        pot.setCurrentAmount(pot.getCurrentAmount().add(amount));
        return potRepository.save(pot);
    }

    @Transactional
    public Pot withdrawMoneyFromPot(UUID potId, BigDecimal amount) {
        Pot pot = potRepository.findById(potId)
                .orElseThrow(() -> new IllegalArgumentException("Pot not found with ID: " + potId));

        if (pot.getCurrentAmount().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds in pot.");
        }

        pot.setCurrentAmount(pot.getCurrentAmount().subtract(amount));
        return potRepository.save(pot);
    }

    public Pot updatePot(UUID id, Pot updatedPot) {
        return potRepository.findById(id)
                .map(pot -> {
                    pot.setName(updatedPot.getName());
                    pot.setCategory(updatedPot.getCategory());
                    pot.setTargetAmount(updatedPot.getTargetAmount());
                    return potRepository.save(pot);
                })
                .orElse(null);
    }

    public void deletePot(UUID id) {
        potRepository.deleteById(id);
    }
}