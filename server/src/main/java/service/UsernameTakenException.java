package service;

import exception.ResponseException;

public class UsernameTakenException extends ResponseException {
    public UsernameTakenException(String message) {
        super(403, message);
    }
}
