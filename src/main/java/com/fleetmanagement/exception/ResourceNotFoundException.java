package com.fleetmanagement.exception;

/**
 * Custom exception for resource not found scenarios
 * Used throughout the application for consistent error handling
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}