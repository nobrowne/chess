package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class Service {
    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws DataAccessException, InvalidInputException, AlreadyTakenException {
        String username = user.username();
        String password = user.password();
        String email = user.email();

        if (username == null || password == null || email == null) {
            throw new InvalidInputException("error: username, password, and email must all be filled");
        }

        if (dataAccess.getUser(username) != null) {
            throw new AlreadyTakenException("error: username already taken");
        }

        dataAccess.createUser(user);

        String authToken = generateAuthToken();
        AuthData authData = new AuthData(authToken, username);
        dataAccess.createAuth(authData);

        return authData;
    }

    public AuthData login(UserData user) throws DataAccessException, UnauthorizedUserException {
        String username = user.username();
        String password = user.password();

        if (dataAccess.getUser(username) == null) {
            throw new UnauthorizedUserException("error: user has not registered an account yet");
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
        validateAuthToken(authToken);
        dataAccess.deleteAuth(authToken);
    }

    public ArrayList<GameData> listGames(String authToken) throws DataAccessException, UnauthorizedUserException {
        validateAuthToken(authToken);

        return dataAccess.listGames();
    }

    public Integer createGame(String authToken, String gameName) throws DataAccessException, UnauthorizedUserException {
        validateAuthToken(authToken);

        int gameID = generateGameID();
        ChessGame newGame = new ChessGame();
        GameData gameData = new GameData(gameID, null, null, gameName, newGame);
        dataAccess.createGame(gameData);

        return gameID;
    }

    public void joinGame(String authToken, ChessGame.TeamColor teamColor, int gameID) throws DataAccessException, UnauthorizedUserException, InvalidInputException, AlreadyTakenException {
        validateAuthToken(authToken);

        GameData game = dataAccess.getGame(gameID);

        if (game == null) {
            throw new InvalidInputException("error: invalid gameID");
        }
        
        String username = dataAccess.getAuth(authToken).username();

        if (teamColor == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null) {
                throw new AlreadyTakenException("error: white team already taken");
            }
            dataAccess.updateGame(new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game()));
        } else if (teamColor == ChessGame.TeamColor.BLACK) {
            if (game.blackUsername() != null) {
                throw new AlreadyTakenException("error: black team already taken");
            }
            dataAccess.updateGame(new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game()));
        } else {
            throw new InvalidInputException("error: invalid team color");
        }
    }

    public void clearApplication() throws DataAccessException {
        dataAccess.clearApplication();
    }

    private void validateAuthToken(String authToken) throws DataAccessException, UnauthorizedUserException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new UnauthorizedUserException("error: unauthorized user");
        }
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
