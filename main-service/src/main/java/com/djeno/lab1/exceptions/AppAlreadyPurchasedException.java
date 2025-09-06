package com.djeno.lab1.exceptions;

public class AppAlreadyPurchasedException extends RuntimeException {
    public AppAlreadyPurchasedException(String message) {
        super(message);
    }
}
