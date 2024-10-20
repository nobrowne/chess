package service;

import exception.ResponseException;

public class AlreadyTakenException extends ResponseException {
    public AlreadyTakenException(String message) {
        super(403, message);
    }
}
