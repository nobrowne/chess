package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Objects;
import java.util.Random;
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
            throw new InvalidInputException("error: username, password, and email must all be filled");
        }

        if (dataAccess.getUser(username) != null) {
            throw new UsernameTakenException("error: username already taken");
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
            throw new UserNotRegisteredException("error: user has not registered an account yet");
        }

        UserData registeredUser = dataAccess.getUser(username);
        if (!Objects.equals(password, registeredUser.password())) {
            throw new UnauthorizedUserException("error: invalid password");
        }

        String authToken = generateAuthToken();
        AuthData authData = new AuthData(authToken, username);
        dataAccess.createAuth(authData);

        return authData;
    }

    public void logout(String authToken) throws DataAccessException, UnauthorizedUserException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new UnauthorizedUserException("error: unauthorized user");
        }

        dataAccess.deleteAuth(authToken);
    }

    public Integer createGame(String authToken, String gameName) throws DataAccessException, UnauthorizedUserException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new UnauthorizedUserException("error: unauthorized user");
        }

        int gameID = generateGameID();
        ChessGame newGame = new ChessGame();
        GameData gameData = new GameData(gameID, null, null, gameName, newGame);
        dataAccess.createGame(gameData);

        return gameID;
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

    private Integer generateGameID() throws DataAccessException {
        Random rand = new Random();

        int gameID;
        do {
            gameID = rand.nextInt(1000);
        } while (dataAccess.getGame(gameID) != null);

        return gameID;
    }
}
