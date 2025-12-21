// src/main/java/com/ebank/data/DataLoader.java
package com.ebank.data;

import com.ebank.entities.BankAccount;
import com.ebank.entities.Client;
import com.ebank.entities.User;
import com.ebank.enums.AccountStatus;
import com.ebank.enums.Role;
import com.ebank.repositories.BankAccountRepository;
import com.ebank.repositories.ClientRepository;
import com.ebank.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Date;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;
    private final BankAccountRepository bankAccountRepository;

    @Bean
    @Transactional
    public CommandLineRunner initDatabase() {
        return args -> {

            // --- 1. AGENT_GUICHET ---
            String agentUsername = "agent";
            if (userRepository.findByUsername(agentUsername).isEmpty()) {
                User agent = new User(null, agentUsername, passwordEncoder.encode("agentpass"), Role.AGENT_GUICHET, null);
                userRepository.save(agent);
            }

            // --- 2. CLIENT DE TEST (Utilisé pour le Dashboard UC-4) ---
            String clientUsername = "Client"; // Utilisateur ciblé
            String identityNumber = "XXFZ976";
            String clientRib = "RIB153455";

            if (userRepository.findByUsername(clientUsername).isEmpty()) {

                // A. Création des entités
                User clientUser = new User();
                clientUser.setUsername(clientUsername);
                clientUser.setPassword(passwordEncoder.encode("clientpass"));
                clientUser.setRole(Role.CLIENT);

                Client clientDetails = new Client();
                clientDetails.setFirstName("ihssane");
                clientDetails.setLastName("ClientTest");
                clientDetails.setIdentityNumber(identityNumber);
                clientDetails.setEmail("client.tes@ebank.com");
                clientDetails.setPostalAddress("123 Rue du Test");
                clientDetails.setBirthDate(new Date());

                // B. Lier les entités (relation bidirectionnelle)
                // CLÉ 1: Liaison
                clientDetails.setUser(clientUser);
                clientUser.setClientDetails(clientDetails);

                // C. Sauvegarde du Client (Côté propriétaire ou cascade)
                // Puisque l'utilisateur est nouveau, le clientRepository.save() persistera
                // le User via la cascade si elle est configurée (CascadeType.ALL)
                Client savedClient = clientRepository.save(clientDetails);

                // D. Création du Compte Bancaire
                BankAccount account = new BankAccount();
                account.setRib(clientRib);
                account.setBalance(new BigDecimal("10000.50"));
                account.setStatus(AccountStatus.OUVERT);
                // Dans DataLoader.java, après avoir créé le compte
                account.setClient(savedClient);
                savedClient.getBankAccounts().add(account); // Ajouter cette ligne
                clientRepository.save(savedClient); // Sauvegarder à nouveau pour s'assurer

                // Lier le Compte au Client GÉRÉ
                account.setClient(savedClient);

                bankAccountRepository.save(account);

                System.out.println("Client de test initialisé avec compte et solde : " + clientUsername);
            }
        };
    }
}