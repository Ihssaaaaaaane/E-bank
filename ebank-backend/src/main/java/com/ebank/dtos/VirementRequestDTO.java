package com.ebank.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VirementRequestDTO {

    // RIB du compte à débiter. Affiché par défaut si compte unique.
    @NotBlank(message = "Le RIB du compte émetteur est obligatoire.")
    private String sourceRib;

    // RIB du compte à créditer.
    @NotBlank(message = "Le RIB du compte destinataire est obligatoire.")
    private String destinationRib;

    // Montant du virement. Doit être positif.
    @NotNull(message = "Le montant du virement est obligatoire.")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à zéro.")
    private BigDecimal amount;

    // Motif du virement.
    @NotBlank(message = "Le motif est obligatoire.")
    private String reason;
}