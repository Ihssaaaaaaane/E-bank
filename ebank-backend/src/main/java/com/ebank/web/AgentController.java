package com.ebank.web;

import com.ebank.dtos.NewClientRequestDTO;
import com.ebank.exceptions.ClientAlreadyExistsException;
import com.ebank.services.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ebank.dtos.NewAccountRequestDTO;
import com.ebank.exceptions.ResourceNotFoundException;
import com.ebank.exceptions.InvalidRibException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/agents") // Sécurisé par Spring Security dans SecurityConfig
@RequiredArgsConstructor
public class AgentController {

    private final ClientService clientService;

    // UC-2: Ajouter un nouveau client (Réservé à AGENT_GUICHET)
    @PostMapping("/clients")
    public ResponseEntity<?> addNewClient(@Valid @RequestBody NewClientRequestDTO request) {
        try {
            clientService.addNewClient(request);
            return new ResponseEntity<>("Client ajouté avec succès. Les identifiants ont été envoyés par mail (RG_7).", HttpStatus.CREATED);
        } catch (ClientAlreadyExistsException e) {
            // Gère les erreurs d'unicité (RG_4, RG_6)
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT); // 409 Conflict
        } catch (IllegalStateException e) {
            // Configuration email manquante (ex: variables d'environnement non visibles)
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            // Exemple: échec SMTP (auth, TLS, etc.)
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // Gère d'autres erreurs potentielles
            return new ResponseEntity<>("Erreur interne lors de la création du client.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // UC-3: Nouveau compte bancaire (Réservé à AGENT_GUICHET)
    @PostMapping("/accounts")
    public ResponseEntity<?> addNewAccount(@Valid @RequestBody NewAccountRequestDTO request) {
        try {
            clientService.addNewBankAccount(request);
            return new ResponseEntity<>("Compte bancaire créé avec succès pour le client " + request.getIdentityNumber() + ".", HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            // Gère RG_8
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // 404
        } catch (InvalidRibException e) {
            // Gère RG_9 et l'unicité du RIB
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // 400
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur interne lors de la création du compte.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}