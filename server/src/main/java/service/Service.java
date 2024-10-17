package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class Service {
    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData registerUser(UserData user) throws UsernameTakenException, DataAccessException, InvalidInputException {
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

        String authToken = generateAuthToken();
        AuthData authData = new AuthData(authToken, username);
        dataAccess.createAuth(authData);

        return authData;
    }

    private String generateAuthToken() throws DataAccessException {
        String authToken;
        do {
            authToken = UUID.randomUUID().toString();
        } while (dataAccess.getAuth(authToken) != null);

        return authToken;
    }
}
