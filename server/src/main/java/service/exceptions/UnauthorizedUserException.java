package service.exceptions;

import exception.ResponseException;

public class UnauthorizedUserException extends ResponseException {
    public UnauthorizedUserException(String message) {
        super(401, message);
    }
}
