package dataaccess.auth;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import java.sql.SQLException;
import model.AuthData;

public class SQLAuthDAO implements AuthDAO {
  public SQLAuthDAO() throws DataAccessException {

    configureTable();
  }

  @Override
  public AuthData getAuth(String authToken) throws DataAccessException {
    String statement = "SELECT * FROM auth WHERE authToken=?";

    try (java.sql.Connection conn = DatabaseManager.getConnection();
        java.sql.PreparedStatement ps = conn.prepareStatement(statement)) {

      ps.setString(1, authToken);

      try (java.sql.ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          String retrievedAuthToken = rs.getString("authToken");
          String username = rs.getString("username");

          return new AuthData(retrievedAuthToken, username);
        } else {
          return null;
        }
      }
    } catch (SQLException ex) {
      throw new DataAccessException(String.format("error: %s", ex.getMessage()));
    }
  }

  @Override
  public void createAuth(AuthData auth) throws DataAccessException {
    String statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";

    try (java.sql.Connection conn = DatabaseManager.getConnection();
        java.sql.PreparedStatement ps = conn.prepareStatement(statement)) {

      ps.setString(1, auth.authToken());
      ps.setString(2, auth.username());

      ps.executeUpdate();

    } catch (SQLException ex) {
      throw new DataAccessException(
          String.format("error: could not update database: %s", ex.getMessage()));
    }
  }

  @Override
  public void deleteAuth(String authToken) throws DataAccessException {
    String statement = "DELETE FROM auth WHERE authToken=?";

    try (java.sql.Connection conn = DatabaseManager.getConnection();
        java.sql.PreparedStatement ps = conn.prepareStatement(statement)) {

      ps.setString(1, authToken);

      ps.executeUpdate();

    } catch (SQLException ex) {
      throw new DataAccessException(
          String.format("error: could not update database: %s", ex.getMessage()));
    }
  }

  @Override
  public void clear() throws DataAccessException {
    String statement = "TRUNCATE auth";

    try (java.sql.Connection conn = DatabaseManager.getConnection();
        java.sql.PreparedStatement ps = conn.prepareStatement(statement)) {

      ps.executeUpdate();

    } catch (SQLException ex) {
      throw new DataAccessException(
          String.format("error: could not update database: %s", ex.getMessage()));
    }
  }

  private void configureTable() throws DataAccessException {
    String createStatement =
        """
                CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(256) NOT NULL,
                username VARCHAR(256) NOT NULL,
                PRIMARY KEY (authToken)
                );
                """;

    try (java.sql.Connection conn = DatabaseManager.getConnection();
        java.sql.PreparedStatement preparedStatement = conn.prepareStatement(createStatement)) {
      preparedStatement.executeUpdate();

    } catch (SQLException ex) {
      throw new DataAccessException(
          String.format("Unable to configure database: %s", ex.getMessage()));
    }
  }
}
