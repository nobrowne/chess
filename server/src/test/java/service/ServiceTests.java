package service;

import static org.junit.jupiter.api.Assertions.*;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.auth.MemoryAuthDAO;
import dataaccess.game.MemoryGameDAO;
import dataaccess.user.MemoryUserDAO;
import java.util.ArrayList;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import request.CreateGameRequest;
import request.LoginRequest;
import request.RegisterRequest;
import result.CreateGameResult;
import result.ListGamesResult;
import result.RegisterResult;
import service.exceptions.AlreadyTakenException;
import service.exceptions.InvalidInputException;
import service.exceptions.UnauthorizedUserException;

public class ServiceTests {
  private static MemoryAuthDAO authDAO;
  private static MemoryGameDAO gameDAO;
  private static MemoryUserDAO userDAO;

  private static AdminService adminService;
  private static GameService gameService;
  private static UserService userService;

  private static UserData existingUser;
  private static UserData newUser;

  private static String badAuthToken;
  private static String testGameName;

  private String existingAuthToken;

  @BeforeAll
  public static void init() {
    authDAO = new MemoryAuthDAO();
    gameDAO = new MemoryGameDAO();
    userDAO = new MemoryUserDAO();

    adminService = new AdminService(authDAO, gameDAO, userDAO);
    AuthService authService = new AuthService(authDAO);
    gameService = new GameService(authDAO, gameDAO, authService);
    userService = new UserService(authDAO, userDAO, authService);

    existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");
    newUser = new UserData("NewUser", "newUserPassword", "newUser@mail.com");

    badAuthToken = "badAuthToken";

    testGameName = "TestGameName";
  }

  @BeforeEach
  void setUp() throws DataAccessException, InvalidInputException, AlreadyTakenException {
    adminService.clearApplication();

    var registerRequest =
        new RegisterRequest(existingUser.username(), existingUser.password(), existingUser.email());
    RegisterResult registerResult = userService.register(registerRequest);

    AuthData authData = new AuthData(registerResult.username(), registerResult.authToken());
    existingAuthToken = authData.authToken();
  }

  @Test
  public void successfulRegistration()
      throws DataAccessException, InvalidInputException, AlreadyTakenException {
    var registerRequest =
        new RegisterRequest(newUser.username(), newUser.password(), newUser.email());

    var registerResult = userService.register(registerRequest);

    assertEquals(newUser.username(), registerResult.username());
    assertNotNull(registerResult.authToken());
  }

  @Test
  public void registeringWithMissingUserDataThrowsInvalidInputException() {
    var registerRequest = new RegisterRequest(newUser.username(), newUser.password(), null);

    assertThrows(InvalidInputException.class, () -> userService.register(registerRequest));
  }

  @Test
  public void registeringWithExistingUsernameThrowsAlreadyTakenException() {
    var registerRequest =
        new RegisterRequest(existingUser.username(), newUser.password(), newUser.email());

    assertThrows(AlreadyTakenException.class, () -> userService.register(registerRequest));
  }

  @Test
  public void successfulLogin() throws UnauthorizedUserException, DataAccessException {
    var loginRequest = new LoginRequest(existingUser.username(), existingUser.password());

    var loginResult = userService.login(loginRequest);

    assertEquals(existingUser.username(), loginResult.username());
    assertNotNull(loginResult.authToken());
  }

  @Test
  public void loggingInWithoutRegisteredAccountThrowsUnauthorizedUserException() {
    var loginRequest = new LoginRequest(newUser.username(), newUser.password());

    assertThrows(UnauthorizedUserException.class, () -> userService.login(loginRequest));
  }

  @Test
  public void loggingInWithIncorrectPasswordThrowsUnauthorizedUserException() {
    var loginRequest = new LoginRequest(existingUser.username(), newUser.password());

    assertThrows(UnauthorizedUserException.class, () -> userService.login(loginRequest));
  }

  @Test
  public void successfulLogout() throws UnauthorizedUserException, DataAccessException {
    userService.logout(existingAuthToken);
    assertNull(authDAO.getAuth(existingAuthToken));
  }

  @Test
  public void loggingOutWithInvalidAuthTokenThrowsUnauthorizedUserException() {
    assertNotEquals(badAuthToken, existingAuthToken);
    assertThrows(UnauthorizedUserException.class, () -> userService.logout(badAuthToken));
  }

  @Test
  public void listingGamesWithInvalidAuthTokenThrowsUnauthorizedUserException() {
    assertNotEquals(badAuthToken, existingAuthToken);
    assertThrows(UnauthorizedUserException.class, () -> gameService.listGames(badAuthToken));
  }

  @Test
  public void successfulListGames() throws UnauthorizedUserException, DataAccessException {
    ArrayList<GameData> games = new ArrayList<>();
    games.add(new GameData(null, null, null, "game 1", new ChessGame()));
    games.add(new GameData(null, null, null, "game 2", new ChessGame()));
    games.add(new GameData(null, null, null, "game 3", new ChessGame()));

    for (GameData game : games) {
      var createGameRequest = new CreateGameRequest(game.gameName());
      gameService.createGame(existingAuthToken, createGameRequest);
    }

    ListGamesResult listGamesResult = gameService.listGames(existingAuthToken);
    assertEquals(listGamesResult.games().size(), games.size());
  }

  @Test
  public void successfulCreateGame() throws UnauthorizedUserException, DataAccessException {
    var createGameRequest = new CreateGameRequest(testGameName);
    CreateGameResult createGameResult =
        gameService.createGame(existingAuthToken, createGameRequest);

    GameData gameData = gameDAO.getGame(createGameResult.gameID());

    assertNotNull(gameData);
  }

  @Test
  public void creatingGameWithInvalidAuthTokenThrowsUnauthorizedUserException() {
    var createGameRequest = new CreateGameRequest(testGameName);

    assertThrows(
        UnauthorizedUserException.class,
        () -> gameService.createGame(badAuthToken, createGameRequest));
  }

  @Test
  public void successfulJoinGame()
      throws UnauthorizedUserException,
          DataAccessException,
          InvalidInputException,
          AlreadyTakenException {
    var createGameRequest = new CreateGameRequest(testGameName);
    CreateGameResult createGameResult =
        gameService.createGame(existingAuthToken, createGameRequest);

    int gameID = createGameResult.gameID();

    gameService.joinGame(existingAuthToken, ChessGame.TeamColor.BLACK, gameID);

    GameData gameData = gameDAO.getGame(gameID);
    GameData expectedGame =
        new GameData(gameID, null, existingUser.username(), testGameName, gameData.game());

    assertEquals(expectedGame, gameDAO.getGame(gameID));
  }

  @Test
  public void joiningGameWithBadIDThrowsInvalidInputException() {
    int badGameID = 15;

    assertThrows(
        InvalidInputException.class,
        () -> gameService.joinGame(existingAuthToken, ChessGame.TeamColor.BLACK, badGameID));
  }

  @Test
  public void joiningGameWithTakenTeamColorThrowsAlreadyTakenException()
      throws UnauthorizedUserException,
          DataAccessException,
          InvalidInputException,
          AlreadyTakenException {
    var createGameRequest = new CreateGameRequest(testGameName);
    CreateGameResult createGameResult =
        gameService.createGame(existingAuthToken, createGameRequest);

    int gameID = createGameResult.gameID();

    gameService.joinGame(existingAuthToken, ChessGame.TeamColor.BLACK, gameID);

    assertThrows(
        AlreadyTakenException.class,
        () -> gameService.joinGame(existingAuthToken, ChessGame.TeamColor.BLACK, gameID));
  }

  @Test
  public void successfulClearApplication()
      throws InvalidInputException,
          AlreadyTakenException,
          DataAccessException,
          UnauthorizedUserException {
    ArrayList<UserData> users = new ArrayList<>();
    users.add(new UserData("username5000", "p455w0rd", "email@email.com"));
    users.add(new UserData("username6000", "5tr0ng3rp455w0rd", "betteremail@betteremail.com"));
    users.add(new UserData("username7000", "5tr0ng35tp455w0rd", "bestemail@bestemail.com"));

    ArrayList<AuthData> auths = new ArrayList<>();

    for (UserData user : users) {
      var registerRequest = new RegisterRequest(user.username(), user.password(), user.email());

      RegisterResult registerResult = userService.register(registerRequest);
      AuthData auth = new AuthData(registerResult.username(), registerResult.authToken());
      auths.add(auth);

      assertNotNull(userDAO.getUser(user.username()));
      assertNotNull(authDAO.getAuth(auth.authToken()));
    }

    ArrayList<GameData> games = new ArrayList<>();
    games.add(new GameData(null, null, null, "game 1", new ChessGame()));
    games.add(new GameData(null, null, null, "game 2", new ChessGame()));
    games.add(new GameData(null, null, null, "game 3", new ChessGame()));

    ArrayList<Integer> gameIDs = new ArrayList<>();
    for (GameData game : games) {
      var createGameRequest = new CreateGameRequest(game.gameName());
      CreateGameResult createGameResult =
          gameService.createGame(existingAuthToken, createGameRequest);

      gameIDs.add(createGameResult.gameID());

      assertNotNull(gameDAO.getGame(createGameResult.gameID()));
    }

    adminService.clearApplication();

    for (UserData user : users) {
      assertNull(userDAO.getUser(user.username()));
    }

    for (AuthData auth : auths) {
      assertNull(authDAO.getAuth(auth.authToken()));
    }

    for (int gameID : gameIDs) {
      assertNull(gameDAO.getGame(gameID));
    }
  }
}
