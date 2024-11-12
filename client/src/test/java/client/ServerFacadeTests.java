package client;

import static org.junit.jupiter.api.Assertions.*;

import exception.ResponseException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerFacade;

public class ServerFacadeTests {

  private static Server server;
  private static ServerFacade serverFacade;

  private static UserData existingUser;
  private static UserData newUser;

  private String existingAuthToken;

  @BeforeAll
  public static void init() {
    server = new Server();
    var port = server.run(0);
    System.out.println("Started test HTTP server on " + port);
    serverFacade = new ServerFacade("http://localhost:" + port);

    existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");
    newUser = new UserData("NewUser", "newUserPassword", "newuser@mail.com");
  }

  @AfterAll
  static void stopServer() {
    server.stop();
  }

  @BeforeEach
  public void setUp() throws ResponseException {
    serverFacade.clearApplication();

    AuthData authData = serverFacade.register(existingUser);
    existingAuthToken = authData.authToken();
  }

  @Test
  public void successfulRegistration() throws ResponseException {
    var registerResult = serverFacade.register(newUser);
    assertEquals(newUser.username(), registerResult.username());
  }

  @Test
  public void registeringWithExistingUsernameThrowsException() {
    var registerRequest =
        new UserData(existingUser.username(), newUser.password(), newUser.email());
    assertThrows(ResponseException.class, () -> serverFacade.register(registerRequest));
  }

  @Test
  public void registeringWithMissingUserDataThrowsException() {
    var registerRequest = new UserData(newUser.username(), newUser.password(), null);
    assertThrows(ResponseException.class, () -> serverFacade.register(registerRequest));
  }

  @Test
  public void successfulLogin() throws ResponseException {
    var loginResult = serverFacade.login(existingUser.username(), existingUser.password());
    assertEquals(existingUser.username(), loginResult.username());
    assertTrue(loginResult.authToken().length() > 10);
  }

  @Test
  public void loggingInWithoutRegisteredAccountThrowsException() {
    assertThrows(
        ResponseException.class, () -> serverFacade.login(newUser.username(), newUser.password()));
  }

  @Test
  public void loggingInWithIncorrectPasswordThrowsException() {
    assertThrows(
        ResponseException.class,
        () -> serverFacade.login(existingUser.username(), newUser.password()));
  }

  @Test
  public void successfulLogout() {
    assertDoesNotThrow(() -> serverFacade.logout(existingAuthToken));
  }

  @Test
  public void loggingOutWithInvalidAuthTokenThrowsException() {
    String badAuthToken = "badAuthToken";
    assertNotEquals(badAuthToken, existingAuthToken);
    assertThrows(ResponseException.class, () -> serverFacade.logout(badAuthToken));
  }

  @Test
  public void successfulGameCreation() throws ResponseException {
    String gameName = "best game ever";

    var createGameResponse = serverFacade.createGame(gameName, existingAuthToken);
    assertTrue(createGameResponse > 0);
  }
  
  @Test
  public void creatingGameWithInvalidAuthTokenThrowsException() {
    String gameName = "best game ever";
    String badAuthToken = "badAuthToken";
    
    assertThrows(
            ResponseException.class, () -> serverFacade.createGame(gameName, badAuthToken));
  }
}
