package com.ebank.repositories;

import com.ebank.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    // Utilisé pour l'unicité du RIB (et aussi pour les virements UC-5)
    Optional<BankAccount> findByRib(String rib);
}