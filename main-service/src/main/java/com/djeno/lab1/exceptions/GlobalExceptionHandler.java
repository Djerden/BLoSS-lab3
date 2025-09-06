package com.djeno.lab1.exceptions;

import com.djeno.lab1.persistence.DTO.error.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String path = violation.getPropertyPath().toString();
                    // Извлекаем только имя поля (последнюю часть пути)
                    String fieldName = path.substring(path.lastIndexOf('.') + 1);
                    return fieldName + ": " + violation.getMessage();
                })
                .collect(Collectors.joining("; "));

        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Validation Failed");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "User Not Found");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, "Invalid Username or Password");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, "Access Denied");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Собираем все ошибки валидации в одну строку
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Validation Failed");
    }

    @ExceptionHandler(NegativeValueException.class)
    public ResponseEntity<ErrorResponse> handleNegativeValueException(NegativeValueException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Negative Withdraw Value");
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(InsufficientBalanceException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Insufficient Balance");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(ExpiredJwtException ex) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, "JWT Token Expired");
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, "Username Already Exists");
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, "Email Already Exists");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "User Not Found");
    }

    @ExceptionHandler(AppAlreadyPurchasedException.class)
    public ResponseEntity<ErrorResponse> handleAppAlreadyPurchased(AppAlreadyPurchasedException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "App Already Purchased");
    }

    @ExceptionHandler(CardAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePCardAlreadyExistsException(CardAlreadyExistsException ex) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, "Card with this number already exists");
    }

    @ExceptionHandler(PrimaryCardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePrimaryCardNotFound(PrimaryCardNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "Primary Card Not Found");
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponse> handlePaymentProcessing(PaymentProcessingException ex) {
        return buildErrorResponse(ex, HttpStatus.PAYMENT_REQUIRED, "Payment Failed");
    }

    @ExceptionHandler(LastPrimaryCardException.class)
    public ResponseEntity<ErrorResponse> handleLastPrimaryCard(LastPrimaryCardException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Last Primary Card");
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCardNotFound(CardNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "Card Not Found");
    }

    @ExceptionHandler(PaymentRequiredException.class)
    public ResponseEntity<ErrorResponse> handlePaymentRequired(PaymentRequiredException ex) {
        return buildErrorResponse(ex, HttpStatus.PAYMENT_REQUIRED, "Payment Required");
    }

    @ExceptionHandler(NotEnoughPrivileges.class)
    public ResponseEntity<ErrorResponse> handleNotEnoughPrivileges(NotEnoughPrivileges ex) {
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN, "Access Denied");
    }

    @ExceptionHandler(AppNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAppNotFoundException(AppNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "App not found");
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileException(InvalidFileException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Invalid File");
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorResponse> handleFileUploadException(FileUploadException ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "File Upload Failed");
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFoundException(CategoryNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "Category Not Found");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception ex, HttpStatus status, String error) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                ex.getMessage()
        );
        return new ResponseEntity<>(response, status);
    }
}
