package service;

import dataaccess.auth.AuthDAO;
import dataaccess.user.UserDAO;
import model.AuthData;
import model.UserData;

public class UserService {
    private final AuthDAO authDAO;
    private final UserDAO userDAO;

    public UserService(AuthDAO authDAO, UserDAO userDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
    }

    public AuthData register(UserData user) {
        // TODO implement
        return null;
    }

    public AuthData login(UserData user) {
        // TODO implement
        return null;
    }

    public void logout(AuthData credentials) {
        // TODO implement
    }
}
