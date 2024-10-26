package dataaccess.user;

import dataaccess.DataAccessException;
import model.UserData;

public class SQLUserDAO implements UserDAO {
    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {

    }
}
