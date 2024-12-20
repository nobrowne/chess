package dataaccess.game;

import chess.ChessGame;
import chess.GameState;
import dataaccess.DataAccessException;
import java.util.ArrayList;
import model.GameData;

public interface GameDAO {
  GameData getGame(Integer gameID) throws DataAccessException;

  ArrayList<GameData> listGames() throws DataAccessException;

  int createGame(String gameName, ChessGame game) throws DataAccessException;

  void updateGame(GameData game) throws DataAccessException;

  void clear() throws DataAccessException;

  void updateGameState(int gameID, GameState gameState) throws DataAccessException;

  GameState getGameState(int gameID) throws DataAccessException;
}
