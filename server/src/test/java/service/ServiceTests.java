package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.auth.MemoryAuthDAO;
import dataaccess.game.MemoryGameDAO;
import dataaccess.user.MemoryUserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.exceptions.AlreadyTakenException;
import service.exceptions.InvalidInputException;
import service.exceptions.UnauthorizedUserException;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    private static MemoryAuthDAO authDAO;
    private static MemoryGameDAO gameDAO;
    private static MemoryUserDAO userDAO;

    private static AdminService adminService;
    private static GameService gameService;
    private static UserService userService;

    private static UserData existingUser;
    private static UserData newUser;

    private String badAuthToken;
    private String testGameName;
    private String existingAuthToken;

    @BeforeAll
    public static void init() {
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        userDAO = new MemoryUserDAO();

        adminService = new AdminService(authDAO, gameDAO, userDAO);
        AuthService authService = new AuthService(authDAO);
        gameService = new GameService(authDAO, gameDAO, userDAO, authService);
        userService = new UserService(authDAO, userDAO, authService);


        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");
        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");
    }

    @BeforeEach
    void setUp() throws DataAccessException, InvalidInputException, AlreadyTakenException {
        adminService.clearApplication();

        AuthData authData = userService.register(existingUser);
        existingAuthToken = authData.authToken();

        badAuthToken = "abc123def456";

        testGameName = "TestGame";
    }

    @Test
    public void registeringWithMissingUserDataThrowsInvalidInputException() {
        var registerRequest = new UserData(newUser.username(), newUser.password(), null);
        assertThrows(InvalidInputException.class, () -> userService.register(registerRequest));
    }

    @Test
    public void registeringWithExistingUsernameThrowsUsernameTakenException() {
        var registerRequest = new UserData(existingUser.username(), newUser.password(), newUser.email());
        assertThrows(AlreadyTakenException.class, () -> userService.register(registerRequest));
    }

    @Test
    public void registeringUserReturnsCorrectAuthenticationData()
            throws DataAccessException, InvalidInputException, AlreadyTakenException {
        var registerResult = userService.register(newUser);

        assertEquals(newUser.username(), registerResult.username());
        assertNotNull(registerResult.authToken());
    }

    @Test
    public void loggingInWithoutRegisteredAccountThrowsUserNotRegisteredException() {
        assertThrows(UnauthorizedUserException.class, () -> userService.login(newUser));
    }

    @Test
    public void loggingInWithIncorrectPasswordThrowsUnauthorizedUserException() {
        var loginRequest = new UserData(existingUser.username(), newUser.password(), null);

        assertThrows(UnauthorizedUserException.class, () -> userService.login(loginRequest));
    }

    @Test
    public void loggingInReturnsCorrectAuthenticationData() throws UnauthorizedUserException, DataAccessException {
        var loginResult = userService.login(existingUser);

        assertEquals(existingUser.username(), loginResult.username());
        assertNotNull(loginResult.authToken());
    }

    @Test
    public void loggingOutWithInvalidAuthTokenThrowsUnauthorizedUserException() {
        assertNotEquals(badAuthToken, existingAuthToken);

        assertThrows(UnauthorizedUserException.class, () -> userService.logout(badAuthToken));
    }

    @Test
    public void loggingOutWithValidAuthTokenDeletesAuthData() throws UnauthorizedUserException, DataAccessException {
        userService.logout(existingAuthToken);
        assertNull(authDAO.getAuth(existingAuthToken));
    }

    @Test
    public void listingGamesWithoutValidAuthTokenThrowsUnauthorizedUserException() {
        assertNotEquals(badAuthToken, existingAuthToken);

        assertThrows(UnauthorizedUserException.class, () -> gameService.listGames(badAuthToken));
    }

    @Test
    public void listingGamesWithValidAuthTokenWorks() throws UnauthorizedUserException, DataAccessException {
        ArrayList<GameData> games = new ArrayList<>();
        games.add(new GameData(null, null, null, "game 1", new ChessGame()));
        games.add(new GameData(null, null, null, "game 2", new ChessGame()));
        games.add(new GameData(null, null, null, "game 3", new ChessGame()));

        for (GameData game : games) {
            gameService.createGame(existingAuthToken, game.gameName());
        }

        ArrayList<GameData> allGames = gameService.listGames(existingAuthToken);
        assertEquals(allGames.size(), games.size());
    }

    @Test
    public void creatingGameWithInvalidAuthTokenThrowsUnauthorizedUserException() {
        assertThrows(UnauthorizedUserException.class, () -> gameService.createGame(badAuthToken, testGameName));
    }

    @Test
    public void creatingGameWithValidAuthTokenWorks() throws UnauthorizedUserException, DataAccessException {
        int gameID = gameService.createGame(existingAuthToken, testGameName);
        GameData gameData = gameDAO.getGame(gameID);
        assertNotNull(gameData);
    }

    @Test
    public void joiningGameWithBadIDThrowsInvalidInputException()
            throws UnauthorizedUserException, DataAccessException {
        int gameID = gameService.createGame(existingAuthToken, testGameName);
        GameData gameData = gameDAO.getGame(gameID);

        int badGameID = 15;
        assertThrows(InvalidInputException.class,
                () -> gameService.joinGame(existingAuthToken, ChessGame.TeamColor.BLACK, badGameID));
    }

    @Test
    public void joiningGameWithTakenTeamColorThrowsAlreadyTakenException()
            throws UnauthorizedUserException, DataAccessException, InvalidInputException, AlreadyTakenException {
        int gameID = gameService.createGame(existingAuthToken, testGameName);
        GameData gameData = gameDAO.getGame(gameID);

        gameService.joinGame(existingAuthToken, ChessGame.TeamColor.BLACK, gameID);
        assertThrows(AlreadyTakenException.class,
                () -> gameService.joinGame(existingAuthToken, ChessGame.TeamColor.BLACK, gameID));
    }

    @Test
    public void joiningGameWithValidInputsWorks()
            throws UnauthorizedUserException, DataAccessException, InvalidInputException, AlreadyTakenException {
        int gameID = gameService.createGame(existingAuthToken, testGameName);
        GameData gameData = gameDAO.getGame(gameID);

        userService.register(newUser);
        String newUserAuthToken = userService.login(newUser).authToken();


        gameService.joinGame(existingAuthToken, ChessGame.TeamColor.BLACK, gameID);
        gameService.joinGame(newUserAuthToken, ChessGame.TeamColor.WHITE, gameID);

        GameData expectedGame = new GameData(gameID, newUser.username(), existingUser.username(), testGameName,
                gameData.game());
        assertEquals(expectedGame, gameDAO.getGame(gameID));
    }

    @Test
    public void clearingApplicationDeletesAllDataObjects()
            throws InvalidInputException, AlreadyTakenException, DataAccessException, UnauthorizedUserException {
        ArrayList<UserData> users = new ArrayList<>();
        users.add(new UserData("username5000", "p455w0rd", "email@email.com"));
        users.add(new UserData("username6000", "5tr0ng3rp455w0rd", "betteremail@betteremail.com"));
        users.add(new UserData("username7000", "5tr0ng35tp455w0rd", "bestemail@bestemail.com"));

        ArrayList<AuthData> auths = new ArrayList<>();

        for (UserData user : users) {
            AuthData auth = userService.register(user);
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
            int gameID = gameService.createGame(existingAuthToken, game.gameName());
            gameIDs.add(gameID);
            assertNotNull(gameDAO.getGame(gameID));
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
