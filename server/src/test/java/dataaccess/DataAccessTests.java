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

public class DataAccessTests {
    private static AuthDAO authDAO;
    private static GameDAO gameDAO;
    private static UserDAO userDAO;

    @BeforeAll
    public static void init() throws DataAccessException {
        authDAO = new SQLAuthDAO(); // new SQLAuthDAO(); new MemoryAuthDAO();
        gameDAO = new SQLGameDAO(); // new SQLGameDAO(); new MemoryGameDAO();
        userDAO = new SQLUserDAO(); // new SQLUserDAO(); new MemoryUserDAO();

        UserData user1 = new UserData("u1", "p1", "e1");
        UserData user2 = new UserData("u2", "p2", "e2");
        UserData user3 = new UserData("u3", "p3", "e3");

        AuthData auth1 = new AuthData("abc123", user1.username());
        AuthData auth2 = new AuthData("def456", user2.username());
        AuthData auth3 = new AuthData("ghi789", user3.username());

        GameData game1 = new GameData(1, null, null, "g1", new ChessGame());
        GameData game2 = new GameData(2, null, null, "g2", new ChessGame());
        GameData game3 = new GameData(3, null, null, "g3", new ChessGame());

        String fakeUsername = "fakeUser";
        String fakeAuthToken = "fakeAuth";
        int fakeGameID = 31415;
    }

    @BeforeEach
    void setUp() {

    }

}
