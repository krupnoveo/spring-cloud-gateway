package ru.krupnoveo.edu.gateway.exception;

public class UnathourizedException extends RuntimeException {
    public UnathourizedException(String message) {
        super(message);
    }
}
