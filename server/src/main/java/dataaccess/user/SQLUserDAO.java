package dataaccess.user;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.UserData;

import java.sql.SQLException;

public class SQLUserDAO implements UserDAO {
    public SQLUserDAO() throws DataAccessException {
        configureTable();
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String json = new Gson().toJson(user);
    }

    @Override
    public void clear() throws DataAccessException {

    }

    private void configureTable() throws DataAccessException {
        String createStatement = """
                CREATE TABLE IF NOT EXISTS user (
                username VARCHAR(256) NOT NULL,
                password VARCHAR(256) NOT NULL,
                email VARCHAR(256) NOT NULL,
                PRIMARY KEY (username)
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
