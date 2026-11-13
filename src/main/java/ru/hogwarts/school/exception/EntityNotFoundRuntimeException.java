package ru.hogwarts.school.exception;

public class EntityNotFoundRuntimeException extends RuntimeException {
    public EntityNotFoundRuntimeException(String message) {
        super(message);
    }
}
