package dataaccess;

import model.AuthData;
import model.UserData;

public interface DataAccess {
    UserData getUser(UserData user) throws DataAccessException;

    void createUser(UserData user) throws DataAccessException;

    AuthData getAuth(AuthData auth) throws DataAccessException;

    void createAuth(AuthData auth) throws DataAccessException;

    void deleteAuth(AuthData auth) throws DataAccessException;

    void clear() throws DataAccessException;
}
