package com.ebank.entities;

import com.ebank.enums.AccountStatus; // Nouveau Enum à créer
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String rib; // Numéro du RIB (RG_9: Validité à vérifier dans le service)

    @Column(nullable = false)
    private BigDecimal balance; // Solde du compte (Utiliser BigDecimal pour l'argent)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // RG_10 : Le compte sera crée avec le statut "Ouvert" par défaut
    private AccountStatus status = AccountStatus.OUVERT;

    // Relation BankAccount -> Client (Plusieurs comptes pour un client)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    // Relation BankAccount -> Operation (Un compte a plusieurs opérations)
    @OneToMany(mappedBy = "bankAccount", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Operation> operations;


}