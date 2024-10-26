package service;

import dataaccess.DataAccessException;
import dataaccess.auth.AuthDAO;
import dataaccess.user.UserDAO;
import model.AuthData;
import model.UserData;
import service.exceptions.AlreadyTakenException;
import service.exceptions.InvalidInputException;
import service.exceptions.UnauthorizedUserException;

import java.util.Objects;
import java.util.UUID;

public class UserService {
    private final AuthDAO authDAO;
    private final UserDAO userDAO;
    private final AuthService authService;

    public UserService(AuthDAO authDAO, UserDAO userDAO, AuthService authService) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
        this.authService = authService;
    }

    public AuthData register(UserData user) throws DataAccessException, InvalidInputException, AlreadyTakenException {
        String username = user.username();
        String password = user.password();
        String email = user.email();

        if (username == null || password == null || email == null) {
            throw new InvalidInputException("error: username, password, and email must all be filled");
        }

        if (userDAO.getUser(username) != null) {
            throw new AlreadyTakenException("error: username already taken");
        }

        userDAO.createUser(user);

        String authToken = generateAuthToken();
        AuthData authData = new AuthData(authToken, username);
        authDAO.createAuth(authData);

        return authData;
    }

    public AuthData login(UserData user) throws DataAccessException, UnauthorizedUserException {
        String username = user.username();
        String password = user.password();

        if (userDAO.getUser(username) == null) {
            throw new UnauthorizedUserException("error: user has not registered an account yet");
        }

        UserData registeredUser = userDAO.getUser(username);
        if (!Objects.equals(password, registeredUser.password())) {
            throw new UnauthorizedUserException("error: invalid password");
        }

        String authToken = generateAuthToken();
        AuthData authData = new AuthData(authToken, username);
        authDAO.createAuth(authData);

        return authData;
    }

    public void logout(String authToken) throws DataAccessException, UnauthorizedUserException {
        authService.validateAuthToken(authToken);
        authDAO.deleteAuth(authToken);
    }

    private String generateAuthToken() throws DataAccessException {
        String authToken;
        do {
            authToken = UUID.randomUUID().toString();
        } while (authDAO.getAuth(authToken) != null);

        return authToken;
    }
}
