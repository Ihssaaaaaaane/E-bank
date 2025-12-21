package com.ebank.web;

import com.ebank.dtos.AccountDetailsDTO;
import com.ebank.exceptions.ResourceNotFoundException;
import com.ebank.services.ClientDashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.ebank.dtos.VirementRequestDTO;
import com.ebank.services.VirementService;
import com.ebank.exceptions.InvalidTransactionException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients") // Sécurisé pour CLIENT dans SecurityConfig
@RequiredArgsConstructor
public class ClientController {

    private final ClientDashboardService dashboardService;
    private final VirementService virementService;

    // UC-4: Consulter le tableau de bord
    // Le 'Authentication' est automatiquement fourni par Spring Security,
    // contenant les informations du Token JWT validé.
    @GetMapping("/dashboard")
    public AccountDetailsDTO getDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String username = userDetails.getUsername();

        // CORRECTION ICI : Changer le nom de la méthode de getDashboardDetails à getDashboardData
        return dashboardService.getDashboardData(username, accountId, page, size);
    }

    // API supplémentaire pour obtenir la liste des comptes (pour la liste déroulante)
    @GetMapping("/accounts")
    public ResponseEntity<?> getClientAccountsList(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(dashboardService.getClientAccounts(username));
    }
    // UC-5: Effectuer un nouveau virement (Réservé à CLIENT)
    @PostMapping("/virement")
    public ResponseEntity<?> makeTransfer(
            @Valid @RequestBody VirementRequestDTO request,
            Authentication authentication) {

        String username = authentication.getName(); // Login du client émetteur

        try {
            String destinationDisplayName = virementService.transfer(request, username);
            return new ResponseEntity<>("Virement de " + request.getAmount() + "€ effectué avec succès vers " + destinationDisplayName + ".", HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            // Compte source/destinataire non trouvé
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // 404
        } catch (InvalidTransactionException e) {
            // Gère RG_11 (statut) et RG_12 (solde)
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // 400
        } catch (Exception e) {
            return new ResponseEntity<>("Échec du virement. Erreur interne.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}