package com.djeno.lab1.exceptions;

public class PrimaryCardNotFoundException extends RuntimeException {
    public PrimaryCardNotFoundException(String message) {
        super(message);
    }
}
