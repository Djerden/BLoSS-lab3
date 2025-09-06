package com.djeno.lab1.exceptions;

public class NotEnoughPrivileges extends RuntimeException {
    public NotEnoughPrivileges(String message) {
        super(message);
    }
}
