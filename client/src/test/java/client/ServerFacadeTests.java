package client;

import static org.junit.jupiter.api.Assertions.*;

import exception.ResponseException;
import java.util.ArrayList;
import model.AuthData;
import model.GameData;
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
    var loginRequest = new LoginRequest(existingUser.username(), existingUser.password());
    var loginResult = serverFacade.login(loginRequest);

    assertEquals(existingUser.username(), loginResult.username());
    assertTrue(loginResult.authToken().length() > 10);
  }

  @Test
  public void loggingInWithoutRegisteredAccountThrowsException() {
    var loginRequest = new LoginRequest(newUser.username(), newUser.password());

    assertThrows(ResponseException.class, () -> serverFacade.login(loginRequest));
  }

  @Test
  public void loggingInWithIncorrectPasswordThrowsException() {
    var loginRequest = new LoginRequest(existingUser.username(), newUser.password());

    assertThrows(ResponseException.class, () -> serverFacade.login(loginRequest));
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
  public void successfulListGames() throws ResponseException {
    ArrayList<String> gameNames = new ArrayList<>();
    gameNames.add("game1");
    gameNames.add("game2");
    gameNames.add("game3");

    for (String gameName : gameNames) {
      serverFacade.createGame(gameName, existingAuthToken);
    }

    ArrayList<GameData> allGames = serverFacade.listGames(existingAuthToken);
    assertEquals(gameNames.size(), allGames.size());
  }

  @Test
  public void listingGamesWithInvalidAuthTokenThrowsException() {
    String badAuthToken = "badAuthToken";
    assertNotEquals(badAuthToken, existingAuthToken);

    assertThrows(ResponseException.class, () -> serverFacade.listGames(badAuthToken));
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

    assertThrows(ResponseException.class, () -> serverFacade.createGame(gameName, badAuthToken));
  }

  @Test
  public void successfulJoinGame() throws ResponseException {
    String gameName = "best game ever";
    int gameID = serverFacade.createGame(gameName, existingAuthToken);

    serverFacade.joinGame("white", gameID, existingAuthToken);

    var games = serverFacade.listGames(existingAuthToken);
    GameData game1 = games.getFirst();

    assertNotNull(game1.whiteUsername());
  }

  @Test
  public void joiningGameWithBadTeamColorThrowsException() throws ResponseException {
    String gameName = "best game ever";
    int gameID = serverFacade.createGame(gameName, existingAuthToken);

    assertThrows(
        ResponseException.class, () -> serverFacade.joinGame("green", gameID, existingAuthToken));
  }
}
