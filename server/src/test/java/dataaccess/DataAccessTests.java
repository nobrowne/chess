package dataaccess;

import static org.junit.jupiter.api.Assertions.*;

import chess.ChessGame;
import dataaccess.auth.AuthDAO;
import dataaccess.auth.SQLAuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.game.SQLGameDAO;
import dataaccess.user.SQLUserDAO;
import dataaccess.user.UserDAO;
import java.util.ArrayList;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataAccessTests {
  private static AuthDAO authDAO;
  private static GameDAO gameDAO;
  private static UserDAO userDAO;

  private static UserData user1;
  private static UserData user2;
  private static UserData user3;

  private static AuthData auth1;
  private static AuthData auth2;
  private static AuthData auth3;

  private static String game1Name;
  private static String game2Name;
  private static String game3Name;

  private static String fakeUsername;
  private static String fakeAuthToken;
  private static int fakeGameID;

  @BeforeAll
  public static void init() throws DataAccessException {
    // Initialize DAO objects
    authDAO = new SQLAuthDAO();
    gameDAO = new SQLGameDAO();
    userDAO = new SQLUserDAO();

    // Initialize the static fields for users
    user1 = new UserData("u1", "p1", "e1");
    user2 = new UserData("u2", "p2", "e2");
    user3 = new UserData("u3", "p3", "e3");

    // Initialize the static fields for auths
    auth1 = new AuthData(user1.username(), "abc123");
    auth2 = new AuthData(user2.username(), "def456");
    auth3 = new AuthData(user3.username(), "ghi789");

    // Initialize the static fields for games
    game1Name = "g1";
    game2Name = "g2";
    game3Name = "g3";
    // Example variables for fake data
    fakeUsername = "fakeUser";
    fakeAuthToken = "fakeAuth";
    fakeGameID = 31415;
  }

  @BeforeEach
  void setUp() throws DataAccessException {
    authDAO.clear();
    gameDAO.clear();
    userDAO.clear();

    userDAO.createUser(user1);
    userDAO.createUser(user2);
    userDAO.createUser(user3);

    authDAO.createAuth(auth1);
    authDAO.createAuth(auth2);
    authDAO.createAuth(auth3);

    gameDAO.createGame(game1Name, new ChessGame());
    gameDAO.createGame(game2Name, new ChessGame());
    gameDAO.createGame(game3Name, new ChessGame());
  }

  @Test
  public void addingUserIsSuccessful() throws DataAccessException {
    assertNotNull(userDAO.getUser(user1.username()));
  }

  @Test
  public void addingUserWithMissingFieldThrowsDataAccessException() {
    UserData incompleteUser = new UserData(user1.username(), user1.password(), null);

    assertThrows(DataAccessException.class, () -> userDAO.createUser(incompleteUser));
  }

  @Test
  public void addingUserWithDuplicateUsernameThrowsDataAccessException() {
    UserData sameUsernameUser = new UserData(user1.username(), "password", "email@email.com");

    assertThrows(DataAccessException.class, () -> userDAO.createUser(sameUsernameUser));
  }

  @Test
  public void gettingUserIsSuccessful() throws DataAccessException {
    UserData user = userDAO.getUser(user1.username());

    assertEquals(user1.username(), user.username());
    assertEquals(user1.password(), user.password());
    assertEquals(user1.email(), user.email());
  }

  @Test
  public void gettingUserWithFakeUsernameReturnsNull() throws DataAccessException {
    assertNull(userDAO.getUser(fakeUsername));
  }

  @Test
  public void clearingUsersIsSuccessful() throws DataAccessException {
    userDAO.clear();

    assertNull(userDAO.getUser(user1.username()));
    assertNull(userDAO.getUser(user2.username()));
    assertNull(userDAO.getUser(user3.username()));
  }

  @Test
  public void addingAuthIsSuccessful() throws DataAccessException {
    assertNotNull(authDAO.getAuth(auth1.authToken()));
  }

  @Test
  public void addingAuthWithMissingFieldThrowsDataAccessException() {
    AuthData incompleteAuth = new AuthData(null, auth1.authToken());

    assertThrows(DataAccessException.class, () -> authDAO.createAuth(incompleteAuth));
  }

  @Test
  public void addingAuthWithDuplicateAuthTokenThrowsDataAccessException() {
    AuthData sameAuthTokenAuth = new AuthData(fakeUsername, auth1.authToken());

    assertThrows(DataAccessException.class, () -> authDAO.createAuth(sameAuthTokenAuth));
  }

  @Test
  public void gettingAuthIsSuccessful() throws DataAccessException {
    AuthData auth = authDAO.getAuth(auth1.authToken());

    assertEquals(auth1.authToken(), auth.authToken());
    assertEquals(auth1.username(), auth.username());
  }

  @Test
  public void gettingAuthWithFakeAuthTokenReturnsNull() throws DataAccessException {
    assertNull(authDAO.getAuth(fakeAuthToken));
  }

  @Test
  public void clearingAuthsIsSuccessful() throws DataAccessException {
    authDAO.clear();

    assertNull(authDAO.getAuth(auth1.authToken()));
    assertNull(authDAO.getAuth(auth2.authToken()));
    assertNull(authDAO.getAuth(auth3.authToken()));
  }

  @Test
  public void addingGameIsSuccessful() throws DataAccessException {
    assertNotNull(gameDAO.getGame(1));
  }

  @Test
  public void addingGameWithMissingGameIDThrowsDataAccessException() {
    assertThrows(DataAccessException.class, () -> gameDAO.createGame(null, new ChessGame()));
  }

  @Test
  public void gettingGameIsSuccessful() throws DataAccessException {
    GameData game = gameDAO.getGame(1);

    assertEquals(1, game.gameID());
    assertNull(game.whiteUsername());
    assertNull(game.blackUsername());
    assertEquals(game1Name, game.gameName());
    assertNotNull(game.game());
  }

  @Test
  public void gettingGameWithNonexistentGameIDReturnsNull() throws DataAccessException {
    assertNull(gameDAO.getGame(fakeGameID));
  }

  @Test
  public void listingGamesIsSuccessFul() throws DataAccessException {
    ArrayList<GameData> allGames = gameDAO.listGames();

    assertEquals(3, allGames.size());
  }

  @Test
  public void listingGamesWhenTableIsEmptyReturnsEmptyList() throws DataAccessException {
    gameDAO.clear();
    ArrayList<GameData> allGames = gameDAO.listGames();

    assertEquals(0, allGames.size());
  }

  @Test
  public void updatingGameIsSuccessful() throws DataAccessException {
    String whiteUsername = "whitePlayer";
    ChessGame newGame = new ChessGame();

    GameData gameWithNewWhiteTeamUser = new GameData(1, whiteUsername, null, game1Name, newGame);
    gameDAO.updateGame(gameWithNewWhiteTeamUser);

    GameData updatedGame = gameDAO.getGame(1);

    assertNull(updatedGame.blackUsername());
    assertEquals(whiteUsername, updatedGame.whiteUsername());
    assertEquals(game1Name, updatedGame.gameName());
  }

  @Test
  public void updatingGameWithBadGameIDThrowsDataAccessException() {
    String whiteUsername = "whitePlayer";
    ChessGame newGame = new ChessGame();

    GameData gameWithBadGameID = new GameData(fakeGameID, whiteUsername, null, game1Name, newGame);

    assertThrows(DataAccessException.class, () -> gameDAO.updateGame(gameWithBadGameID));
  }

  @Test
  public void clearingGamesIsSuccessful() throws DataAccessException {
    gameDAO.clear();
    ArrayList<GameData> allGames = gameDAO.listGames();

    assertEquals(0, allGames.size());
  }
}
