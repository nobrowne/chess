package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

import java.util.Objects;
import java.util.UUID;

public class Service {
    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws UsernameTakenException, DataAccessException, InvalidInputException {
        String username = user.username();
        String password = user.password();
        String email = user.email();

        if (username == null || password == null || email == null) {
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

    public AuthData login(UserData user) throws DataAccessException, UserNotRegisteredException, UnauthorizedUserException {
        String username = user.username();
        String password = user.password();

        if (dataAccess.getUser(username) == null) {
            throw new UserNotRegisteredException("user has not registered an account yet");
        }

        UserData registeredUser = dataAccess.getUser(username);
        if (!Objects.equals(password, registeredUser.password())) {
            throw new UnauthorizedUserException("invalid password");
        }

        String authToken = generateAuthToken();
        AuthData authData = new AuthData(authToken, username);
        dataAccess.createAuth(authData);

        return authData;
    }

    public void logout(String authToken) throws DataAccessException, UnauthorizedUserException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new UnauthorizedUserException("unauthorized user");
        }

        dataAccess.deleteAuth(authToken);
    }

    public void clearApplication() throws DataAccessException {
        dataAccess.clearApplication();
    }

    private String generateAuthToken() throws DataAccessException {
        String authToken;
        do {
            authToken = UUID.randomUUID().toString();
        } while (dataAccess.getAuth(authToken) != null);

        return authToken;
    }
}
