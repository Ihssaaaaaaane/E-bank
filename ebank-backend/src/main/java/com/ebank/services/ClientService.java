// src/main/java/com/ebank/services/ClientService.java
package com.ebank.services;

import com.ebank.dtos.NewClientRequestDTO;
import com.ebank.entities.Client;
import com.ebank.entities.User;
import com.ebank.enums.Role;
import com.ebank.exceptions.ClientAlreadyExistsException;
import com.ebank.repositories.ClientRepository;
import com.ebank.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ebank.dtos.NewAccountRequestDTO;
import com.ebank.entities.BankAccount;
import com.ebank.enums.AccountStatus; // Importez l'Enum AccountStatus
import com.ebank.repositories.BankAccountRepository;
import com.ebank.exceptions.ResourceNotFoundException; // Nouvelle exception à créer
import com.ebank.exceptions.InvalidRibException; // Nouvelle exception à créer

import java.math.BigDecimal;
import java.util.Optional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // Service d'envoi de mail (À créer)
    private final BankAccountRepository bankAccountRepository;


    /**
     * UC-2: Ajoute un nouveau client en vérifiant les règles de gestion.
     * @param request DTO contenant les informations du nouveau client.
     * @return Le client créé.
     */
    public Client addNewClient(NewClientRequestDTO request) {

        // --- 1. Vérification des règles d'unicité (RG_4 et RG_6) ---

        // RG_4: Le numéro d'identité doit être unique.
        if (clientRepository.findByIdentityNumber(request.getIdentityNumber()).isPresent()) {
            throw new ClientAlreadyExistsException("Le numéro d'identité existe déjà.");
        }

        // RG_6: L'adresse mail doit être unique.
        if (clientRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ClientAlreadyExistsException("L'adresse mail est déjà utilisée.");
        }

        // --- 2. Préparation des identifiants (RG_7) ---

        // Générer un mot de passe temporaire unique
        String initialPassword = UUID.randomUUID().toString().substring(0, 8);
        String username = generateUsername(request.getFirstName(), request.getLastName());

        // --- 3. Création de l'entité User ---
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(initialPassword)); // RG_1
        newUser.setRole(Role.CLIENT);
        userRepository.save(newUser);

        // --- 4. Création de l'entité Client ---
        Client newClient = new Client();
        newClient.setFirstName(request.getFirstName());
        newClient.setLastName(request.getLastName());
        newClient.setBirthDate(request.getBirthDate());
        newClient.setPostalAddress(request.getPostalAddress());
        newClient.setEmail(request.getEmail());
        newClient.setIdentityNumber(request.getIdentityNumber());

        // Lier User et Client
        newClient.setUser(newUser);

        Client savedClient = clientRepository.save(newClient);

        //--- 5. Envoi du mail (RG_7) ---
        emailService.sendLoginCredentials(newClient.getEmail(), username, initialPassword);

        return savedClient;
    }

    // Logique simple pour générer un nom d'utilisateur initial
    private String generateUsername(String firstName, String lastName) {
        return (firstName.toLowerCase().substring(0, 1) + lastName.toLowerCase()).replace(" ", "");
    }

    /**
     * UC-3: Crée un nouveau compte bancaire pour un client existant.
     */
    public BankAccount addNewBankAccount(NewAccountRequestDTO request) {

        // --- 1. Vérification RG_8 (Le numéro d'identité doit exister) ---
        Client client = clientRepository.findByIdentityNumber(request.getIdentityNumber())
                .orElseThrow(() -> new ResourceNotFoundException("RG_8: Le numéro d'identité du client n'existe pas."));

        // --- 2. Vérification RG_9 (Le RIB doit être un RIB valide) ---
        if (!isRibValid(request.getRib())) {
            throw new InvalidRibException("RG_9: Le RIB n'est pas un RIB valide.");
        }

        // --- 3. Vérification si un compte avec ce RIB existe déjà (unicité) ---
        if (bankAccountRepository.findByRib(request.getRib()).isPresent()) {
            throw new InvalidRibException("Ce RIB est déjà utilisé pour un autre compte.");
        }

        // --- 4. Création du compte ---
        BankAccount newAccount = new BankAccount();
        newAccount.setRib(request.getRib());
        newAccount.setClient(client);
        newAccount.setBalance(BigDecimal.ZERO); // Compte créé à zéro

        // RG_10: Le compte bancaire sera crée avec le statut « Ouvert »
        newAccount.setStatus(AccountStatus.OUVERT);

        return bankAccountRepository.save(newAccount);
    }

    /**
     * Logique de validation simple du RIB (RG_9).
     * Dans un vrai système, ce serait un algorithme de vérification complexe.
     * Pour ce projet, vérifions juste la longueur et le format.
     */
    private boolean isRibValid(String rib) {
        // Validation simple: doit être non null, non vide et avoir une longueur standard (par ex. 24 caractères pour un IBAN simple)
        return rib != null && rib.length() >= 10 && rib.matches("^[a-zA-Z0-9]*$");
    }

}