package dataaccess.user;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import java.sql.SQLException;
import model.UserData;

public class SQLUserDAO implements UserDAO {
  public SQLUserDAO() throws DataAccessException {
    configureTable();
  }

  @Override
  public UserData getUser(String username) throws DataAccessException {
    String statement = "SELECT * FROM user WHERE username=?";

    try (java.sql.Connection conn = DatabaseManager.getConnection();
        java.sql.PreparedStatement ps = conn.prepareStatement(statement)) {

      ps.setString(1, username);

      try (java.sql.ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          String retrievedUsername = rs.getString("username");
          String password = rs.getString("password");
          String email = rs.getString("email");

          return new UserData(retrievedUsername, password, email);
        } else {
          return null;
        }
      }
    } catch (SQLException ex) {
      throw new DataAccessException(String.format("Error: %s", ex.getMessage()));
    }
  }

  @Override
  public void createUser(UserData user) throws DataAccessException {
    String statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";

    try (java.sql.Connection conn = DatabaseManager.getConnection();
        java.sql.PreparedStatement ps = conn.prepareStatement(statement)) {

      ps.setString(1, user.username());
      ps.setString(2, user.password());
      ps.setString(3, user.email());

      ps.executeUpdate();

    } catch (SQLException ex) {
      throw new DataAccessException(
          String.format("Error: could not update database: %s", ex.getMessage()));
    }
  }

  @Override
  public void clear() throws DataAccessException {
    String statement = "TRUNCATE user";

    try (java.sql.Connection conn = DatabaseManager.getConnection();
        java.sql.PreparedStatement ps = conn.prepareStatement(statement)) {

      ps.executeUpdate();

    } catch (SQLException ex) {
      throw new DataAccessException(
          String.format("Error: could not update database: %s", ex.getMessage()));
    }
  }

  private void configureTable() throws DataAccessException {
    String createStatement =
        """
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
      throw new DataAccessException(
          String.format("Error: unable to configure database: %s", ex.getMessage()));
    }
  }
}
