// src/main/java/com/ebank/entities/Operation.java
package com.ebank.entities;

import com.ebank.enums.OperationType; // Nouveau Enum à créer
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RG_15 : Traçage avec date précise
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationType type; // Débit ou Crédit

    @Column(nullable = false)
    private BigDecimal amount; // Montant

    private String title; // Intitulé de l'opération

    // Relation Operation -> BankAccount (Plusieurs opérations pour un compte)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private BankAccount bankAccount;


}

