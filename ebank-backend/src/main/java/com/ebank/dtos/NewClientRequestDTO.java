package com.ebank.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class NewClientRequestDTO {

    // RG_5: Le nom et le prénom sont obligatoires.
    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    // RG_5: Date anniversaire obligatoire.
    @NotNull(message = "La date anniversaire est obligatoire")
    private Date birthDate;

    // RG_5: Adresse postale obligatoire.
    @NotBlank(message = "L'adresse postale est obligatoire")
    private String postalAddress;

    // RG_5: Adresse mail obligatoire. RG_6: L'adresse mail doit être unique (vérifié dans le service).
    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    // RG_4: Le numéro d'identité doit être unique (vérifié dans le service).
    @NotBlank(message = "Le numéro d'identité est obligatoire")
    private String identityNumber;
}