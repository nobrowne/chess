package service;

import dataaccess.auth.AuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.user.UserDAO;

public class AdminService {
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;

    public AdminService(AuthDAO authDAO, GameDAO gameDAO, UserDAO userDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
    }

    public void clear() {
        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();
    }
}
