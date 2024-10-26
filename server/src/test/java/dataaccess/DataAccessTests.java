package dataaccess;

import chess.ChessGame;
import dataaccess.monolith.DataAccess;
import dataaccess.monolith.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class DataAccessTests {
    private static DataAccess dataAccess;

    @BeforeAll
    public static void init() {
        DataAccess dataAccess = new MemoryDataAccess(); // new SQLDataAccess(); new MemoryDataAccess();

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
