package com.datathon.api.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/** Exceptions carry English messages for logs; the text the user sees is chosen here, in Croatian. */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e) {
        return respond(HttpStatus.NOT_FOUND, "Traženi zapis ne postoji.");
    }

    @ExceptionHandler(TooManyRequestsException.class)
    ResponseEntity<ErrorResponse> handleTooManyRequests(TooManyRequestsException e) {
        log.warn("Rate limit hit: {}", e.getMessage());
        return respond(HttpStatus.TOO_MANY_REQUESTS, "Previše zahtjeva. Pokušajte ponovno za minutu.");
    }

    @ExceptionHandler(InvalidRequestException.class)
    ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException e) {
        log.info("Invalid request: {}", e.getMessage());
        return respond(HttpStatus.BAD_REQUEST, "Neispravan zahtjev. Provjerite unesene podatke.");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ErrorResponse> handleUnreadableOrInvalidBody(Exception e) {
        log.info("Rejected request body: {}", e.getMessage());
        return respond(HttpStatus.BAD_REQUEST, "Neispravan zahtjev. Provjerite unesene podatke.");
    }

    /** Admin routes are not for end users, so the reason is passed through. */
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorResponse> handleStatus(ResponseStatusException e) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        return respond(status, e.getReason() != null ? e.getReason() : status.getReasonPhrase());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Došlo je do pogreške na poslužitelju.");
    }

    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message, status.getReasonPhrase()));
    }
}
