package dataaccess;

import model.AuthData;
import model.UserData;

public interface DataAccess {
    UserData getUser(String username) throws DataAccessException;

    void createUser(UserData user) throws DataAccessException;

    AuthData getAuth(String authToken) throws DataAccessException;

    void createAuth(AuthData auth) throws DataAccessException;

    void deleteAuth(AuthData auth) throws DataAccessException;

    void clearApplication() throws DataAccessException;
}
