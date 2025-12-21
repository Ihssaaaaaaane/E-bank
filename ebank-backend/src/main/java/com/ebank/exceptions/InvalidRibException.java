package com.ebank.exceptions;

// Pour RG_9 (Validation du RIB)
public class InvalidRibException extends RuntimeException {
    public InvalidRibException(String message) {
        super(message);
    }
}