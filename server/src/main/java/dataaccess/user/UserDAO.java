package dataaccess.user;

import dataaccess.DataAccessException;
import model.UserData;

public interface UserDAO {
    UserData getUser(String username) throws DataAccessException;

    void createUser(UserData user) throws DataAccessException;

    void clear() throws DataAccessException;
}
