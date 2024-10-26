package service.exceptions;

import exception.ResponseException;

public class InvalidInputException extends ResponseException {
    public InvalidInputException(String message) {
        super(400, message);
    }
}
