package com.ebank.entities;

import com.ebank.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // Utilisé comme login

    @Column(nullable = false)
    private String password; // Doit être crypté (RG_1)

    @Enumerated(EnumType.STRING)
    private Role role; // CLIENT ou AGENT_GUICHET

    // Relation optionnelle : OneToOne avec Client (si l'utilisateur est un client)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Client clientDetails;
}