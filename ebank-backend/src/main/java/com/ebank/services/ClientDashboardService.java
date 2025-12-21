// src/main/java/com/ebank/services/ClientDashboardService.java
package com.ebank.services;

import com.ebank.dtos.AccountDetailsDTO;
import com.ebank.dtos.AccountSummaryDTO;
import com.ebank.dtos.OperationDTO;
import com.ebank.entities.BankAccount;
import com.ebank.entities.Client;
import com.ebank.entities.Operation;
import com.ebank.entities.User;
import com.ebank.exceptions.ResourceNotFoundException;
import com.ebank.repositories.OperationRepository;
import com.ebank.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import essentiel

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientDashboardService {

    private final UserRepository userRepository;
    private final OperationRepository operationRepository;

    /**
     * Méthode utilitaire pour trouver le client à partir du nom d'utilisateur authentifié.
     * @param username Nom d'utilisateur (tiré du JWT).
     * @return L'entité Client associée.
     */
    private Client getClientByUsername(String username) {
        // 1. Trouver l'entité User
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé."));

        // 2. Utiliser la méthode du Repository pour charger le Client lié
        // Cette méthode gère l'Optional et le cast implicite.
        return userRepository.findClientDetailsByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé pour cet utilisateur."));
    }

    /**
     * UC-4: Récupère les détails d'un compte spécifique et les opérations paginées.
     * @param username Utilisateur connecté
     * @param accountId ID du compte (peut être null, on prend alors le premier)
     * @param page Page des opérations à afficher
     * @param size Nombre d'opérations par page
     * @return DTO contenant le solde et les opérations.
     */
    @Transactional // CLÉ : Assure que les collections LAZY (comme les opérations) sont chargées.
    public AccountDetailsDTO getDashboardData(String username, Long accountId, int page, int size) {

        Client client = getClientByUsername(username);

        // --- 1. Récupération des comptes (doit être EAGER ou dans cette transaction) ---
        Collection<BankAccount> accounts = client.getBankAccounts();

        if (accounts == null || accounts.isEmpty()) {
            throw new ResourceNotFoundException("Aucun compte bancaire trouvé pour ce client. Veuillez contacter votre agent.");
        }

        // --- 2. Détermination du compte à afficher ---
        BankAccount targetAccount;
        if (accountId != null) {
            targetAccount = accounts.stream()
                    .filter(acc -> acc.getId().equals(accountId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Compte ID " + accountId + " non trouvé pour cet utilisateur."));
        } else {
            // Si l'ID n'est pas spécifié, prend le premier compte (par défaut)
            targetAccount = accounts.iterator().next();
        }

        // --- 3. Pagination des opérations (UC-4) ---
        Page<Operation> operationsPage = operationRepository.findByBankAccount(
                targetAccount,
                PageRequest.of(page, size)
        );

        // --- 4. Construction du DTO ---
        AccountDetailsDTO dto = new AccountDetailsDTO();
        dto.setAccountId(targetAccount.getId());
        dto.setRib(targetAccount.getRib());
        dto.setBalance(targetAccount.getBalance());
        dto.setClientName(client.getFirstName() + " " + client.getLastName());
        List<OperationDTO> operationDTOs = operationsPage.getContent().stream()
                .map(op -> {
                    OperationDTO opDto = new OperationDTO();
                    opDto.setId(op.getId());
                    opDto.setDate(op.getDate());
                    opDto.setAmount(op.getAmount());
                    opDto.setType(op.getType());
                    opDto.setTitle(op.getTitle());
                    return opDto;
                })
                .collect(Collectors.toList());
        dto.setOperations(operationDTOs); // Ligne 90 corrigée
        dto.setTotalPages(operationsPage.getTotalPages());

        return dto;
    }

    /**
     * UC-4: Récupère la liste de tous les comptes du client (pour la liste déroulante du Frontend).
     * @param username Utilisateur connecté.
     * @return Liste simple des comptes (ID et RIB).
     */
    private final EntityManager entityManager;
    @Transactional
    public List<AccountSummaryDTO> getClientAccounts(String username) {
        // 1. Récupérer le client
        Client client = getClientByUsername(username);

        // 2. FORCE REFRESH : Détruit le cache pour cette entité spécifique
        // Cela garantit que la liste EAGER des comptes est à jour
        try {
            entityManager.refresh(client);
        } catch (Exception e) {
            System.err.println("Échec du refresh client : " + e.getMessage());
        }

        Collection<BankAccount> accounts = client.getBankAccounts();

        if (accounts == null || accounts.isEmpty()) {
            return List.of();
        }

        return accounts.stream()
                .map(acc -> new AccountSummaryDTO(acc.getId(), acc.getRib()))
                .collect(Collectors.toList());
    }

}