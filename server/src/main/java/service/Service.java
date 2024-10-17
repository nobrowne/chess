package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;

public class Service {
    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public UserData registerUser(UserData user) throws UsernameTakenException, DataAccessException, InvalidInputException {
        String username = user.username();
        String password = user.password();
        String email = user.email();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            throw new InvalidInputException("username, password, and email must all be filled");
        }

        if (dataAccess.getUser(username) != null) {
            throw new UsernameTakenException("username already taken");
        }

        dataAccess.createUser(user);

        return user;
    }
}
