package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;

public class Service {
    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public UserData registerUser(UserData user) throws UsernameTakenException, DataAccessException {
        if (dataAccess.getUser(user) != null) {
            throw new UsernameTakenException("username already taken");
        }

        dataAccess.createUser(user);

        return user;
    }
}
