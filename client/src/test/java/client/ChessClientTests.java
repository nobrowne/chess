package client;

import static org.junit.jupiter.api.Assertions.*;

import exception.ResponseException;
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

    assertEquals(
        "At least one of the required inputs is missing. Please provide username, password, and email",
        registrationMessage);
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

    assertEquals(
        "At least one of the required inputs is missing. Please provide username and password",
        loginMessage);
  }

  @Test
  public void loggingInWithIncorrectPassword() {
    String loginInfo = String.format("login %s %s", existingUser.username(), "badPassword");
    String loginMessage = chessClient.eval(loginInfo);

    assertEquals("error: invalid password", loginMessage);
  }
}
