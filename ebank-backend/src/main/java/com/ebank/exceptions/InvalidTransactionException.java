package com.ebank.exceptions;

// Pour RG_11 et RG_12
public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}