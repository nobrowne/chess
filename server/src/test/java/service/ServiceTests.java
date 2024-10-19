package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    private static MemoryDataAccess dataAccess;
    private static Service service;
    private static UserData existingUser;
    private static UserData newUser;

    private String badAuthToken;
    private String testGameName;
    private String existingAuthToken;

    @BeforeAll
    public static void init() {
        dataAccess = new MemoryDataAccess();
        service = new Service(dataAccess);

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");
        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");
    }

    @BeforeEach
    void setUp() throws DataAccessException, InvalidInputException, UsernameTakenException {
        service.clearApplication();

        AuthData authData = service.register(existingUser);
        existingAuthToken = authData.authToken();

        badAuthToken = "abc123def456";

        testGameName = "TestGame";
    }

    @Test
    public void registeringWithMissingUserDataThrowsInvalidInputException() {
        var registerRequest = new UserData(newUser.username(), newUser.password(), null);
        assertThrows(InvalidInputException.class, () -> service.register(registerRequest));
    }

    @Test
    public void registeringWithExistingUsernameThrowsUsernameTakenException() {
        var registerRequest = new UserData(existingUser.username(), newUser.password(), newUser.email());
        assertThrows(UsernameTakenException.class, () -> service.register(registerRequest));
    }

    @Test
    public void registeringUserReturnsCorrectAuthenticationData() throws DataAccessException, InvalidInputException, UsernameTakenException {
        var registerResult = service.register(newUser);

        assertEquals(newUser.username(), registerResult.username());
        assertNotNull(registerResult.authToken());
    }

    @Test
    public void loggingInWithoutRegisteredAccountThrowsUserNotRegisteredException() {
        assertThrows(UserNotRegisteredException.class, () -> service.login(newUser));
    }

    @Test
    public void loggingInWithIncorrectPasswordThrowsUnauthorizedUserException() {
        var loginRequest = new UserData(existingUser.username(), newUser.password(), null);

        assertThrows(UnauthorizedUserException.class, () -> service.login(loginRequest));
    }

    @Test
    public void loggingInReturnsCorrectAuthenticationData() throws UserNotRegisteredException, UnauthorizedUserException, DataAccessException {
        var loginResult = service.login(existingUser);

        assertEquals(existingUser.username(), loginResult.username());
        assertNotNull(loginResult.authToken());
    }

    @Test
    public void loggingOutWithInvalidAuthTokenThrowsUnauthorizedUserException() {
        assertNotEquals(badAuthToken, existingAuthToken);

        assertThrows(UnauthorizedUserException.class, () -> service.logout(badAuthToken));
    }

    @Test
    public void loggingOutWithValidAuthTokenDeletesAuthData() throws UnauthorizedUserException, DataAccessException {
        service.logout(existingAuthToken);
        assertNull(dataAccess.getAuth(existingAuthToken));
    }

    @Test
    public void listingGamesWithoutValidAuthTokenThrowsUnauthorizedUserException() {
        assertNotEquals(badAuthToken, existingAuthToken);

        assertThrows(UnauthorizedUserException.class, () -> service.listGames(badAuthToken));
    }

    @Test
    public void listingGamesWithValidAuthTokenWorks() throws UnauthorizedUserException, DataAccessException {
        ArrayList<GameData> games = new ArrayList<>();
        games.add(new GameData(null, null, null, "game 1", new ChessGame()));
        games.add(new GameData(null, null, null, "game 2", new ChessGame()));
        games.add(new GameData(null, null, null, "game 3", new ChessGame()));

        for (GameData game : games) {
            service.createGame(existingAuthToken, game.gameName());
        }

        ArrayList<GameData> allGames = service.listGames(existingAuthToken);
        assertEquals(allGames.size(), games.size());
    }

    @Test
    public void creatingGameWithInvalidAuthTokenThrowsUnauthorizedUserException() {
        assertThrows(UnauthorizedUserException.class, () -> service.createGame(badAuthToken, testGameName));
    }

    @Test
    public void creatingGameWithValidAuthTokenWorks() throws UnauthorizedUserException, DataAccessException {
        int gameID = service.createGame(existingAuthToken, testGameName);
        GameData gameData = dataAccess.getGame(gameID);

        assertNotNull(gameData);
    }

    @Test
    public void clearingApplicationDeletesAllDataObjects() throws InvalidInputException, UsernameTakenException, DataAccessException, UnauthorizedUserException {
        ArrayList<UserData> users = new ArrayList<>();
        users.add(new UserData("username5000", "p455w0rd", "email@email.com"));
        users.add(new UserData("username6000", "5tr0ng3rp455w0rd", "betteremail@betteremail.com"));
        users.add(new UserData("username7000", "5tr0ng35tp455w0rd", "bestemail@bestemail.com"));

        ArrayList<AuthData> auths = new ArrayList<>();

        for (UserData user : users) {
            AuthData auth = service.register(user);
            auths.add(auth);

            assertNotNull(dataAccess.getUser(user.username()));
            assertNotNull(dataAccess.getAuth(auth.authToken()));
        }

        ArrayList<GameData> games = new ArrayList<>();
        games.add(new GameData(null, null, null, "game 1", new ChessGame()));
        games.add(new GameData(null, null, null, "game 2", new ChessGame()));
        games.add(new GameData(null, null, null, "game 3", new ChessGame()));

        ArrayList<Integer> gameIDs = new ArrayList<>();
        for (GameData game : games) {
            int gameID = service.createGame(existingAuthToken, game.gameName());
            gameIDs.add(gameID);
            assertNotNull(dataAccess.getGame(gameID));
        }

        service.clearApplication();

        for (UserData user : users) {
            assertNull(dataAccess.getUser(user.username()));
        }

        for (AuthData auth : auths) {
            assertNull(dataAccess.getAuth(auth.authToken()));
        }

        for (int gameID : gameIDs) {
            assertNull(dataAccess.getGame(gameID));
        }
    }
}
