package com.ebank.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewAccountRequestDTO {

    // Utilisé pour vérifier RG_8 (le client doit exister)
    @NotBlank(message = "Le numéro d'identité du client est obligatoire")
    private String identityNumber;

    // Utilisé pour vérifier RG_9 (validation du RIB à faire dans le service)
    @NotBlank(message = "Le RIB est obligatoire")
    private String rib;
}