package service;

import dataaccess.DataAccessException;
import dataaccess.auth.AuthDAO;
import service.exceptions.UnauthorizedUserException;

public class AuthService {
  private final AuthDAO authDAO;

  public AuthService(AuthDAO authDAO) {
    this.authDAO = authDAO;
  }

  public void validateAuthToken(String authToken)
      throws DataAccessException, UnauthorizedUserException {
    if (authDAO.getAuth(authToken) == null) {
      throw new UnauthorizedUserException("Error: unauthorized user");
    }
  }
}
