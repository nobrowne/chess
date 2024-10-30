package dataaccess.game;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.GameData;

import java.sql.SQLException;
import java.util.ArrayList;

public class SQLGameDAO implements GameDAO {
    public SQLGameDAO() throws DataAccessException {
        configureTable();
    }

    @Override
    public GameData getGame(Integer gameID) throws DataAccessException {
        return null;
    }

    @Override
    public ArrayList<GameData> listGames() throws DataAccessException {
        return null;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {

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
}
