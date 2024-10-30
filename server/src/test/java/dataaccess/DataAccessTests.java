package dataaccess;

import chess.ChessGame;
import dataaccess.auth.AuthDAO;
import dataaccess.auth.SQLAuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.game.SQLGameDAO;
import dataaccess.user.SQLUserDAO;
import dataaccess.user.UserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    private static GameData game1;
    private static GameData game2;
    private static GameData game3;

    private static String fakeUsername;
    private static String fakeAuthToken;
    private static int fakeGameID;

    @BeforeAll
    public static void init() throws DataAccessException {
        // Initialize DAO objects
        authDAO = new SQLAuthDAO(); // new SQLAuthDAO(); new MemoryAuthDAO();
        gameDAO = new SQLGameDAO(); // new SQLGameDAO(); new MemoryGameDAO();
        userDAO = new SQLUserDAO(); // new SQLUserDAO(); new MemoryUserDAO();

        // Initialize the static fields for users
        user1 = new UserData("u1", "p1", "e1");
        user2 = new UserData("u2", "p2", "e2");
        user3 = new UserData("u3", "p3", "e3");

        // Initialize the static fields for auths
        auth1 = new AuthData("abc123", user1.username());
        auth2 = new AuthData("def456", user2.username());
        auth3 = new AuthData("ghi789", user3.username());

        // Initialize the static fields for games
        game1 = new GameData(1, null, null, "g1", new ChessGame());
        game2 = new GameData(2, null, null, "g2", new ChessGame());
        game3 = new GameData(3, null, null, "g3", new ChessGame());

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
    }

    @Test
    public void addingUserIsSuccessful() throws DataAccessException {
        userDAO.createUser(user1);
        UserData user = userDAO.getUser(user1.username());

        System.out.printf(user.toString());

        assertEquals(user1.username(), user.username());
    }

    @Test
    public void addingUserWithMissingFieldThrowsDataAccessException() {
        UserData incompleteUser = new UserData(user1.username(), user1.password(), null);
        assertThrows(DataAccessException.class, () -> userDAO.createUser(incompleteUser));
    }

    @Test
    public void addingUserWithDuplicateUsernameThrowsDataAccessException() throws DataAccessException {
        userDAO.createUser(user1);

        UserData sameUsernameUser = new UserData(user1.username(), "password", "email@email.com");
        assertThrows(DataAccessException.class, () -> userDAO.createUser(sameUsernameUser));
    }

}
