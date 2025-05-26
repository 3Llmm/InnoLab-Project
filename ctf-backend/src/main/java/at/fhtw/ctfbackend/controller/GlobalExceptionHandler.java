package at.fhtw.ctfbackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(RuntimeException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Invalid username or password");

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Access denied");

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoHandlerFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Resource not found");

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({DisabledException.class, LockedException.class})
    public ResponseEntity<Map<String, String>> handleAccountIssues(Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex instanceof DisabledException ?
                "Account is disabled" : "Account is locked");

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
}