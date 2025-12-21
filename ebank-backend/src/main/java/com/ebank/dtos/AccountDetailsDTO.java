package com.ebank.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AccountDetailsDTO {
    private String rib; // Le numéro du RIB [cite: 45]
    private BigDecimal balance; // Le solde du compte [cite: 46]
    private List<OperationDTO> operations; // Les dix dernières opérations [cite: 47]
    private Long accountId; // ID du compte pour la sélection dans le Frontend
    private String clientName; // Nom complet du client
    private int totalPages; // Pour la pagination 
}