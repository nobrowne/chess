package dataaccess.auth;

import dataaccess.DataAccessException;
import model.AuthData;

public interface AuthDAO {
  AuthData getAuth(String authToken) throws DataAccessException;

  void createAuth(AuthData auth) throws DataAccessException;

  void deleteAuth(String authToken) throws DataAccessException;

  void clear() throws DataAccessException;
}
