package service;

import exception.ResponseException;

public class InvalidPasswordException extends ResponseException {
    public InvalidPasswordException(String message) {
        super(401, message);
    }
}
