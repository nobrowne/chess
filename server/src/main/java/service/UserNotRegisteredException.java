package service;

import exception.ResponseException;

public class UserNotRegisteredException extends ResponseException {
    public UserNotRegisteredException(String message) {
        super(401, message);
    }
}
