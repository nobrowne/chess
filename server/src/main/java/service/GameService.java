package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.auth.AuthDAO;
import dataaccess.game.GameDAO;
import java.util.ArrayList;
import model.GameData;
import service.exceptions.AlreadyTakenException;
import service.exceptions.InvalidInputException;
import service.exceptions.UnauthorizedUserException;

public class GameService {
  private final AuthDAO authDAO;
  private final GameDAO gameDAO;
  private final AuthService authService;

  public GameService(AuthDAO authDAO, GameDAO gameDAO, AuthService authService) {
    this.authDAO = authDAO;
    this.gameDAO = gameDAO;
    this.authService = authService;
  }

  public ArrayList<GameData> listGames(String authToken)
      throws DataAccessException, UnauthorizedUserException {
    authService.validateAuthToken(authToken);

    return gameDAO.listGames();
  }

  public Integer createGame(String authToken, String gameName)
      throws DataAccessException, UnauthorizedUserException {
    authService.validateAuthToken(authToken);

    ChessGame newGame = new ChessGame();

    return gameDAO.createGame(gameName, newGame);
  }

  public void joinGame(String authToken, ChessGame.TeamColor teamColor, int gameID)
      throws DataAccessException,
          UnauthorizedUserException,
          InvalidInputException,
          AlreadyTakenException {
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
            new GameData(
                game.gameID(), username, game.blackUsername(), game.gameName(), game.game()));
      }
      case BLACK -> {
        if (game.blackUsername() != null) {
          throw new AlreadyTakenException("error: black team already taken");
        }
        gameDAO.updateGame(
            new GameData(
                game.gameID(), game.whiteUsername(), username, game.gameName(), game.game()));
      }
      default -> throw new InvalidInputException("error: invalid team color");
    }
  }
}
