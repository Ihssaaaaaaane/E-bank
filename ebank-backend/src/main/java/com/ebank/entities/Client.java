package com.ebank.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.FetchType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RG_4 : Le numéro d'identité doit être unique
    @Column(unique = true, nullable = false)
    private String identityNumber;

    // RG_5 : Nom et prénom obligatoires
    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String firstName;

    // RG_5 : Date anniversaire obligatoire
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date birthDate;

    // RG_6 : L'adresse mail doit être unique et obligatoire (RG_5)
    @Column(unique = true, nullable = false)
    private String email;

    // RG_5 : Adresse postal obligatoire
    @Column(nullable = false)
    private String postalAddress;

    // Liaison OneToOne avec l'entité User pour l'authentification
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;


    @OneToMany(mappedBy = "client", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<BankAccount> bankAccounts = new ArrayList<>();



}