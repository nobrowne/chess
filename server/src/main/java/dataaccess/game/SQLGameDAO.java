package dataaccess.game;

import chess.ChessGame;
import chess.GameState;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import model.GameData;

public class SQLGameDAO implements GameDAO {
  public SQLGameDAO() throws DataAccessException {
    configureTable();
  }

  @Override
  public GameData getGame(Integer gameID) throws DataAccessException {
    String statement = "SELECT * FROM game WHERE gameID=?";

    try (java.sql.Connection conn = DatabaseManager.getConnection();
        java.sql.PreparedStatement ps = conn.prepareStatement(statement)) {

      ps.setInt(1, gameID);

      try (java.sql.ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          int retrievedGameID = rs.getInt("gameID");
          String whiteUsername = rs.getString("whiteUsername");
          String blackUsername = rs.getString("blackUsername");
          String gameName = rs.getString("gameName");
          ChessGame game = deserializeGame(rs.getString("game"));

          return new GameData(retrievedGameID, whiteUsername, blackUsername, gameName, game);
        } else {
          return null;
        }
      }
    } catch (SQLException ex) {
      throw new DataAccessException(String.format("Error: %s", ex.getMessage()));
    }
  }

  @Override
  public ArrayList<GameData> listGames() throws DataAccessException {
    ArrayList<GameData> gamesList = new ArrayList<>();

    String statement = "SELECT gameID FROM game";

    try (java.sql.Connection conn = DatabaseManager.getConnection();
        java.sql.PreparedStatement ps = conn.prepareStatement(statement);
        java.sql.ResultSet resultSet = ps.executeQuery()) {

      while (resultSet.next()) {
        int gameID = resultSet.getInt("gameID");
        GameData gameData = getGame(gameID);
        if (gameData != null) {
          gamesList.add(gameData);
        }
      }
    } catch (SQLException ex) {
      throw new DataAccessException(String.format("Error: %s", ex.getMessage()));
    }

    return gamesList;
  }

  @Override
  public int createGame(String gameName, ChessGame game) throws DataAccessException {
    String statement =
        "INSERT INTO game (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";

    try (java.sql.Connection conn = DatabaseManager.getConnection();
        java.sql.PreparedStatement ps =
            conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {

      ps.setNull(1, Types.VARCHAR);
      ps.setNull(2, Types.VARCHAR);
      ps.setString(3, gameName);
      ps.setString(4, serializeGame(game));

      ps.executeUpdate();

      try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          int gameId = generatedKeys.getInt(1);
          updateGameState(gameId, GameState.IN_PLAY);
          return gameId;
        } else {
          throw new SQLException("creating game failed, no ID retrieved");
        }
      }

    } catch (SQLException ex) {
      throw new DataAccessException(
          String.format("Error: could not update database: %s", ex.getMessage()));
    }
  }

  @Override
  public void updateGame(GameData game) throws DataAccessException {
    if (getGame(game.gameID()) == null) {
      throw new DataAccessException("Error: specified game does not exist");
    }

    String statement =
        "UPDATE game SET whiteUsername=?, blackUsername=?, gameName=?, game=? WHERE gameID=?";

    try (java.sql.Connection conn = DatabaseManager.getConnection();
        java.sql.PreparedStatement ps = conn.prepareStatement(statement)) {

      ps.setString(1, game.whiteUsername());
      ps.setString(2, game.blackUsername());
      ps.setString(3, game.gameName());
      ps.setString(4, serializeGame(game.game()));
      ps.setInt(5, game.gameID());

      ps.executeUpdate();

    } catch (SQLException ex) {
      throw new DataAccessException(String.format("Error: %s", ex.getMessage()));
    }
  }

  @Override
  public void clear() throws DataAccessException {
    String statement = "TRUNCATE game";

    try (java.sql.Connection conn = DatabaseManager.getConnection();
        java.sql.PreparedStatement ps = conn.prepareStatement(statement)) {

      ps.executeUpdate();

    } catch (SQLException ex) {
      throw new DataAccessException(
          String.format("Error: could not update database: %s", ex.getMessage()));
    }
  }

  @Override
  public void updateGameState(int gameID, GameState gameState) throws DataAccessException {
    if (getGame(gameID) == null) {
      throw new DataAccessException("Error: specified game does not exist");
    }

    String statement = "UPDATE game SET gameState=? WHERE gameID=?";

    try (java.sql.Connection conn = DatabaseManager.getConnection();
        java.sql.PreparedStatement ps = conn.prepareStatement(statement)) {

      ps.setString(1, gameState.name());
      ps.setInt(2, gameID);

      ps.executeUpdate();

    } catch (SQLException ex) {
      throw new DataAccessException(String.format("Error: %s", ex.getMessage()));
    }
  }

  @Override
  public GameState getGameState(int gameID) throws DataAccessException {
    String statement = "SELECT gameState FROM game WHERE gameID=?";

    try (java.sql.Connection conn = DatabaseManager.getConnection();
        java.sql.PreparedStatement ps = conn.prepareStatement(statement)) {

      ps.setInt(1, gameID);

      try (java.sql.ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return GameState.valueOf(rs.getString("gameState"));
        } else {
          return null;
        }
      }
    } catch (SQLException ex) {
      throw new DataAccessException(String.format("Error: %s", ex.getMessage()));
    }
  }

  private void configureTable() throws DataAccessException {
    String createStatement =
        """
                CREATE TABLE IF NOT EXISTS game (
                gameID INT AUTO_INCREMENT,
                whiteUsername VARCHAR(256),
                blackUsername VARCHAR(256),
                gameName VARCHAR(256) NOT NULL,
                game TEXT,
                gameState TEXT,
                PRIMARY KEY (gameID)
                )
                """;

    try (java.sql.Connection conn = DatabaseManager.getConnection();
        java.sql.PreparedStatement preparedStatement = conn.prepareStatement(createStatement)) {
      preparedStatement.executeUpdate();

    } catch (SQLException ex) {
      throw new DataAccessException(
          String.format("Unable to configure database: %s", ex.getMessage()));
    }
  }

  private String serializeGame(ChessGame game) {
    return new Gson().toJson(game);
  }

  private ChessGame deserializeGame(String game) throws SQLException {
    return new Gson().fromJson(game, ChessGame.class);
  }
}
