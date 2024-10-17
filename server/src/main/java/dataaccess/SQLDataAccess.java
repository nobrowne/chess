package dataaccess;

import model.AuthData;
import model.UserData;

public class SQLDataAccess implements DataAccess {
    @Override
    public UserData getUser(UserData user) throws DataAccessException {
        return null;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public AuthData getAuth(AuthData auth) throws DataAccessException {
        return null;
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {

    }

    @Override
    public void deleteAuth(AuthData auth) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {

    }
}
