package ru.hogwarts.school.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.hogwarts.school.exception.EntityNotFoundRuntimeException;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(EntityNotFoundRuntimeException.class)
    ResponseEntity<String> EntityNotFoundRuntimeExceptionHandler(EntityNotFoundRuntimeException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
}
