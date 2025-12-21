package com.ebank.repositories;

import com.ebank.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    // Pour vérifier RG_4 (Numéro d'identité unique)
    Optional<Client> findByIdentityNumber(String identityNumber);

    // Pour vérifier RG_6 (Email unique)
    Optional<Client> findByEmail(String email);
}