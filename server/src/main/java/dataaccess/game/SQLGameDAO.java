package dataaccess.game;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

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
            throw new DataAccessException(String.format("error: %s", ex.getMessage()));
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
            throw new DataAccessException(String.format("error: %s", ex.getMessage()));
        }

        return gamesList;
    }

    @Override
    public int createGame(String gameName, ChessGame game) throws DataAccessException {
        String statement = "INSERT INTO game (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";

        try (java.sql.Connection conn = DatabaseManager.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {

            ps.setNull(1, Types.VARCHAR);
            ps.setNull(2, Types.VARCHAR);
            ps.setString(3, gameName);
            ps.setString(4, serializeGame(game));

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("creating game failed, no ID retrieved");
                }
            }

        } catch (SQLException ex) {
            throw new DataAccessException(String.format("error: could not update database: %s", ex.getMessage()));
        }
    }

    @Override
    public void updateGame(GameData game) {

    }

    @Override
    public void clear() throws DataAccessException {
        String statement = "TRUNCATE game";

        try (java.sql.Connection conn = DatabaseManager.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(statement)) {

            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException(String.format("error: could not update database: %s", ex.getMessage()));
        }
    }

    private void configureTable() throws DataAccessException {
        String createStatement = """
                CREATE TABLE IF NOT EXISTS game (
                gameID INT AUTO_INCREMENT,
                whiteUsername VARCHAR(256),
                blackUsername VARCHAR(256),
                gameName VARCHAR(256) NOT NULL,
                game TEXT,
                PRIMARY KEY (gameID)
                )
                """;

        try (java.sql.Connection conn = DatabaseManager.getConnection();
             java.sql.PreparedStatement preparedStatement = conn.prepareStatement(createStatement)) {
            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }

    private String serializeGame(ChessGame game) {
        return new Gson().toJson(game);
    }

    private ChessGame deserializeGame(String game) throws SQLException {
        return new Gson().fromJson(game, ChessGame.class);
    }
}
