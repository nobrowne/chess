package dataaccess;

import exception.ResponseException;

/** Indicates there was an error connecting to the database */
public class DataAccessException extends ResponseException {
  public DataAccessException(String message) {
    super(500, message);
  }
}
