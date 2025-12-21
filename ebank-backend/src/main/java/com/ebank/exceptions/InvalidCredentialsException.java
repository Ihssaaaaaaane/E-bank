package com.ebank.exceptions;

/**
 * Exception personnalisée pour les erreurs d'authentification ou de mot de passe.
 * Utilisée pour indiquer un mot de passe ou un identifiant incorrect lors de la connexion
 * ou du changement de mot de passe.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}