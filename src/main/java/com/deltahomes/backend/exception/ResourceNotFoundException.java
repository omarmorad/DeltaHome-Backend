package com.deltahomes.backend.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, UUID id) {
        super(resource + " not found with id: " + id);
    }
}
