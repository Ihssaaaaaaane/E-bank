// src/main/java/com/ebank/services/VirementService.java
package com.ebank.services;

import com.ebank.dtos.VirementRequestDTO;
import com.ebank.entities.BankAccount;
import com.ebank.entities.Operation;
import com.ebank.enums.AccountStatus;
import com.ebank.enums.OperationType;
import com.ebank.exceptions.InvalidTransactionException;
import com.ebank.exceptions.ResourceNotFoundException;
import com.ebank.repositories.BankAccountRepository;
import com.ebank.repositories.OperationRepository;
import com.ebank.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class VirementService {

    private final BankAccountRepository bankAccountRepository;
    private final OperationRepository operationRepository;
    private final UserRepository userRepository; // Pour vérifier l'appartenance

    @Transactional
    public String transfer(VirementRequestDTO request, String username) {

        // 1. Validation de base
        if (request.getSourceRib().equals(request.getDestinationRib())) {
            throw new InvalidTransactionException("Impossible de transférer des fonds vers le même compte.");
        }

        // 2. Trouver les comptes
        BankAccount sourceAccount = bankAccountRepository.findByRib(request.getSourceRib())
                .orElseThrow(() -> new ResourceNotFoundException("Compte source non trouvé."));

        BankAccount destinationAccount = bankAccountRepository.findByRib(request.getDestinationRib())
                .orElseThrow(() -> new ResourceNotFoundException("Compte destinataire non trouvé."));

        // 3. Vérifier l'appartenance (RG_9)
        boolean isOwner = userRepository.findByUsername(username)
                .map(user -> user.getClientDetails())
                .filter(client -> client.getBankAccounts().contains(sourceAccount))
                .isPresent();

        if (!isOwner) {
            throw new InvalidTransactionException("Le compte source n'appartient pas à l'utilisateur connecté.");
        }

        // 4. Vérifier le statut du compte source (RG_11)
        if (!sourceAccount.getStatus().equals(AccountStatus.OUVERT)) {
            throw new InvalidTransactionException("Le compte source n'est pas ouvert (Statut: " + sourceAccount.getStatus() + ").");
        }

        // 5. Vérifier le solde (RG_12)
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InvalidTransactionException("Solde insuffisant. Solde actuel: " + sourceAccount.getBalance() + "€.");
        }

        // 6. Débiter et Créditer
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

        String destinationDisplayName = getCounterpartyDisplayName(destinationAccount);
        String sourceDisplayName = getCounterpartyDisplayName(sourceAccount);

        // 7. Enregistrer les opérations (RG_13, RG_14, RG_15)
        Operation debitOperation = new Operation();
        debitOperation.setDate(new Date());
        debitOperation.setAmount(request.getAmount());
        debitOperation.setType(OperationType.DEBIT);
        debitOperation.setTitle("Virement vers " + destinationDisplayName + ": " + request.getReason());
        debitOperation.setBankAccount(sourceAccount);
        operationRepository.save(debitOperation);

        Operation creditOperation = new Operation();
        creditOperation.setDate(new Date());
        creditOperation.setAmount(request.getAmount());
        creditOperation.setType(OperationType.CREDIT);
        creditOperation.setTitle("Virement en votre faveur de " + sourceDisplayName + " : " + request.getReason());
        creditOperation.setBankAccount(destinationAccount);
        operationRepository.save(creditOperation);

        // Sauvegarde des comptes mise à jour (grâce à @Transactional et l'état Managed)
        // bankAccountRepository.save(sourceAccount); est implicite.
        return destinationDisplayName;
    }

    private String getCounterpartyDisplayName(BankAccount account) {
        if (account == null) {
            return "RIB inconnu";
        }
        if (account.getClient() != null) {
            String firstName = account.getClient().getFirstName();
            String lastName = account.getClient().getLastName();
            String fullName = ((firstName == null ? "" : firstName.trim()) + " " + (lastName == null ? "" : lastName.trim())).trim();
            if (!fullName.isEmpty()) {
                return fullName;
            }
        }
        return "RIB " + account.getRib();
    }
}