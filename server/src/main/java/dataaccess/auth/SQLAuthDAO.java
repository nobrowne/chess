package dataaccess.auth;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.AuthData;

import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO {
    public SQLAuthDAO() throws DataAccessException {
        configureTable();
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {

    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {

    }

    private void configureTable() throws DataAccessException {
        String createStatement = """
                CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(256) NOT NULL,
                username VARCHAR(256) NOT NULL,
                PRIMARY KEY (authToken),
                FOREIGN KEY (username) REFERENCE user(username)
                );
                """;

        try (java.sql.Connection conn = DatabaseManager.getConnection();
             java.sql.PreparedStatement preparedStatement = conn.prepareStatement(createStatement)) {
            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}