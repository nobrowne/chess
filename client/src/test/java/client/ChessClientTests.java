package client;

import static org.junit.jupiter.api.Assertions.*;

import exception.ResponseException;
import java.util.ArrayList;
import model.UserData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;
import serverfacade.ServerFacade;

public class ChessClientTests {
  private static ChessClient chessClient;
  private static ServerFacade serverFacade;
  private static Server server;
  private static UserData newUser;
  private static UserData existingUser;

  @BeforeAll
  public static void init() {
    server = new Server();
    int port = server.run(0);

    String serverUrl = "http://localhost:" + port;
    chessClient = new ChessClient(serverUrl);
    serverFacade = new ServerFacade(serverUrl);

    newUser = new UserData("user1", "password", "email@email.com");
    existingUser = new UserData("existingUser", "password", "email@email.com");
  }

  @AfterAll
  public static void stopServer() {
    server.stop();
  }

  @BeforeEach
  public void setUp() throws ResponseException {
    serverFacade.clearApplication();

    chessClient.eval(
        String.format(
            "register %s %s %s",
            existingUser.username(), existingUser.password(), existingUser.email()));
  }

  @Test
  public void successfulRegistration() {
    String registrationInfo =
        String.format("register %s %s %s", newUser.username(), newUser.password(), newUser.email());
    String registrationMessage = chessClient.eval(registrationInfo);

    assertEquals("You are registered as " + newUser.username(), registrationMessage);
  }

  @Test
  public void registeringWithMissingInfo() {
    String registrationInfo =
        String.format("register %s %s", newUser.username(), newUser.password());
    String registrationMessage = chessClient.eval(registrationInfo);

    assertEquals("error: username, password, and email must all be filled", registrationMessage);
  }

  @Test
  public void registeringWithTakenUsername() {
    String registrationInfo =
        String.format(
            "register %s %s %s", existingUser.username(), newUser.password(), newUser.email());
    String registrationMessage = chessClient.eval(registrationInfo);

    assertEquals("error: username already taken", registrationMessage);
  }

  @Test
  public void successfulLogin() throws ResponseException {
    String loginInfo =
        String.format("login %s %s", existingUser.username(), existingUser.password());
    String loginMessage = chessClient.eval(loginInfo);

    assertFalse(loginMessage.contains("error"));
    assertFalse(loginMessage.contains("missing"));
  }

  @Test
  public void loggingInWithoutRegisteredUsername() {
    String loginInfo = String.format("login %s %s", newUser.username(), newUser.password());
    String loginMessage = chessClient.eval(loginInfo);

    assertEquals("error: user has not registered an account yet", loginMessage);
  }

  @Test
  public void loggingInWithMissingInfo() {
    String loginInfo = String.format("login %s", newUser.username());
    String loginMessage = chessClient.eval(loginInfo);

    assertEquals("error: username and password must be filled", loginMessage);
  }

  @Test
  public void loggingInWithIncorrectPassword() {
    String loginInfo = String.format("login %s %s", existingUser.username(), "badPassword");
    String loginMessage = chessClient.eval(loginInfo);

    assertEquals("error: invalid password", loginMessage);
  }

  @Test
  public void successfulLogout() {
    String loginInfo =
        String.format("login %s %s", existingUser.username(), existingUser.password());
    chessClient.eval(loginInfo);

    chessClient.eval("logout");

    assertEquals(
        chessClient.help(),
        """
        - register <USERNAME> <PASSWORD> <EMAIL>: create an account
        - login <USERNAME> <PASSWORD>: play chess
        - quit: shut down the application
        - help: see possible commands
      """);
  }

  @Test
  public void successfulLogoutExtraParameters() {
    String loginInfo =
        String.format("login %s %s", existingUser.username(), existingUser.password());
    chessClient.eval(loginInfo);

    chessClient.eval("logout because I'm bored");

    assertEquals(
        chessClient.help(),
        """
            - register <USERNAME> <PASSWORD> <EMAIL>: create an account
            - login <USERNAME> <PASSWORD>: play chess
            - quit: shut down the application
            - help: see possible commands
          """);
  }

  @Test
  public void loggingOutWithoutBeingLoggedIn() {
    String logoutMessage = chessClient.eval("logout because I'm bored");

    assertEquals("error: cannot log out if not logged in", logoutMessage);
  }

  @Test
  public void successfulCreateGame() {
    String loginInfo =
        String.format("login %s %s", existingUser.username(), existingUser.password());
    chessClient.eval(loginInfo);

    String gameName = "what a fun game";
    String createGameInfo = String.format("create %s", gameName);
    String createGameMessage = chessClient.eval(createGameInfo);

    assertEquals("You have created a new game called " + gameName, createGameMessage);
  }

  @Test
  public void creatingGameWithNoName() {
    String loginInfo =
        String.format("login %s %s", existingUser.username(), existingUser.password());
    chessClient.eval(loginInfo);
    String createGameMessage = chessClient.eval("create");

    assertEquals("error: game name must be filled", createGameMessage);
  }

  @Test
  public void creatingGameWithoutLoggingIn() {
    String gameName = "what a fun game";
    String createGameInfo = String.format("create %s", gameName);
    String createGameMessage = chessClient.eval(createGameInfo);

    assertEquals("error: cannot create game if not logged in", createGameMessage);
  }

  @Test
  public void successfulListGames() throws ResponseException {
    String loginInfo =
        String.format("login %s %s", existingUser.username(), existingUser.password());
    chessClient.eval(loginInfo);

    ArrayList<String> gameNames = new ArrayList<>();
    gameNames.add("game1");
    gameNames.add("game2");
    gameNames.add("game3");

    for (String gameName : gameNames) {
      String createGameInfo = String.format("create %s", gameName);
      chessClient.eval(createGameInfo);
    }

    String listGamesMessage = chessClient.eval("list");

    assertFalse(listGamesMessage.contains("error"));
    assertFalse(listGamesMessage.contains("create <GAME NAME>: create a new game"));
  }

  @Test
  public void listingGamesWithoutLoggingIn() {
    String listGamesMessage = chessClient.eval("list");
    assertEquals("error: cannot list games if not logged in", listGamesMessage);
  }
}
