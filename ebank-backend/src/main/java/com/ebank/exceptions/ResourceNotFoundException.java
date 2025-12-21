package com.ebank.exceptions;

// Pour RG_8 (Client non trouvé)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}