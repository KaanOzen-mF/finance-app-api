package com.finance.app.repository;

import com.finance.app.model.Pot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PotRepository extends JpaRepository<Pot, UUID> {
    List<Pot> findByUserId(UUID userId);
}