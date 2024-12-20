package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.auth.AuthDAO;
import dataaccess.game.GameDAO;
import model.GameData;
import request.CreateGameRequest;
import request.JoinGameRequest;
import result.CreateGameResult;
import result.ListGamesResult;
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

  public ListGamesResult listGames(String authToken)
      throws DataAccessException, UnauthorizedUserException {
    authService.validateAuthToken(authToken);

    return new ListGamesResult(gameDAO.listGames());
  }

  public CreateGameResult createGame(String authToken, CreateGameRequest createGameRequest)
      throws DataAccessException, UnauthorizedUserException {
    authService.validateAuthToken(authToken);

    ChessGame newGame = new ChessGame();

    return new CreateGameResult(gameDAO.createGame(createGameRequest.gameName(), newGame));
  }

  public void joinGame(String authToken, JoinGameRequest joinGameRequest)
      throws DataAccessException,
          UnauthorizedUserException,
          InvalidInputException,
          AlreadyTakenException {
    authService.validateAuthToken(authToken);

    GameData game = gameDAO.getGame(joinGameRequest.gameID());

    if (game == null) {
      throw new InvalidInputException("Error: invalid gameID");
    }

    String username = authDAO.getAuth(authToken).username();

    ChessGame.TeamColor teamColor = joinGameRequest.playerColor();
    if (teamColor == null) {
      throw new InvalidInputException("Error: invalid team color");
    }

    switch (teamColor) {
      case WHITE -> {
        if (game.whiteUsername() != null) {
          throw new AlreadyTakenException("Error: white team already taken");
        }
        gameDAO.updateGame(
            new GameData(
                game.gameID(), username, game.blackUsername(), game.gameName(), game.game()));
      }
      case BLACK -> {
        if (game.blackUsername() != null) {
          throw new AlreadyTakenException("Error: black team already taken");
        }
        gameDAO.updateGame(
            new GameData(
                game.gameID(), game.whiteUsername(), username, game.gameName(), game.game()));
      }
      default -> throw new InvalidInputException("Error: invalid team color");
    }
  }
}
