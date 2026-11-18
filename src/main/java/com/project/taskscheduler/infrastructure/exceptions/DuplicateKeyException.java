package com.project.taskscheduler.infrastructure.exceptions;

public class DuplicateKeyException extends RuntimeException {
    public DuplicateKeyException(String mensagem) {
        super(mensagem);
    }

    public DuplicateKeyException(String mensagem, Throwable throwable) {
        super(mensagem, throwable);
    }
}
