package dataaccess.auth;

import dataaccess.DataAccessException;
import model.AuthData;

public class SQLAuthDAO implements AuthDAO {
    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {

    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {

    }
}
