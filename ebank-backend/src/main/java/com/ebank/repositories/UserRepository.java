package com.ebank.repositories;

import com.ebank.entities.Client;
import com.ebank.entities.User;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par son nom d'utilisateur (login).
     * Ceci est nécessaire pour l'implémentation de UserDetailsService.
     */
    Optional<User> findByUsername(String username);

    @Query("SELECT u.clientDetails FROM User u WHERE u.id = :userId")
    @QueryHints({
            @QueryHint(name = "org.hibernate.cacheable", value = "false"), // Désactive le cache de niveau 2
            @QueryHint(name = "javax.persistence.cache.retrieveMode", value = "BYPASS") // Force le rechargement
    })
    Optional<Client> findClientDetailsByUserId(@Param("userId") Long userId);
}
