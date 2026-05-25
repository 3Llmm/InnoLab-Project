package at.fhtw.ctfbackend.controller;

public class AdminStateConflictException extends RuntimeException {
    public AdminStateConflictException(String message) {
        super(message);
    }
}
