package com.djeno.lab1.exceptions;

public class AppNotFoundException extends RuntimeException {
    public AppNotFoundException(Long id) {
        super("Приложение с ID " + id + " не найдено");
    }
}
