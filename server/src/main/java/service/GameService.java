package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.auth.AuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.user.UserDAO;
import model.GameData;
import service.exceptions.AlreadyTakenException;
import service.exceptions.InvalidInputException;
import service.exceptions.UnauthorizedUserException;

import java.util.ArrayList;
import java.util.Random;

public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final UserDAO userDAO;
    private final AuthService authService;

    public GameService(AuthDAO authDAO, GameDAO gameDAO, UserDAO userDAO, AuthService authService) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
        this.authService = authService;
    }

    public ArrayList<GameData> listGames(String authToken) throws DataAccessException, UnauthorizedUserException {
        authService.validateAuthToken(authToken);

        return gameDAO.listGames();
    }

    public Integer createGame(String authToken, String gameName) throws DataAccessException, UnauthorizedUserException {
        authService.validateAuthToken(authToken);

        int gameID = generateGameID();
        ChessGame newGame = new ChessGame();
        GameData gameData = new GameData(gameID, null, null, gameName, newGame);
        gameDAO.createGame(gameData);

        return gameID;
    }

    public void joinGame(String authToken, ChessGame.TeamColor teamColor, int gameID)
            throws DataAccessException, UnauthorizedUserException, InvalidInputException, AlreadyTakenException {
        authService.validateAuthToken(authToken);

        GameData game = gameDAO.getGame(gameID);

        if (game == null) {
            throw new InvalidInputException("error: invalid gameID");
        }

        String username = authDAO.getAuth(authToken).username();

        switch (teamColor) {
            case WHITE -> {
                if (game.whiteUsername() != null) {
                    throw new AlreadyTakenException("error: white team already taken");
                }
                gameDAO.updateGame(
                        new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game()));
            }
            case BLACK -> {
                if (game.blackUsername() != null) {
                    throw new AlreadyTakenException("error: black team already taken");
                }
                gameDAO.updateGame(
                        new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game()));
            }
            default -> throw new InvalidInputException("error: invalid team color");
        }
    }

    private Integer generateGameID() throws DataAccessException {
        Random rand = new Random();

        int gameID;
        do {
            gameID = rand.nextInt(1000);
        } while (gameDAO.getGame(gameID) != null);

        return gameID;
    }
}
