package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTests {
    private MemoryDataAccess dataAccess;
    private Service service;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new MemoryDataAccess();
        service = new Service(dataAccess);
        service.clearApplication();
    }

    @Test
    public void registeringWithMissingUserDataThrowsInvalidInputException() {
        var user = new UserData("username5000", "p455w0rd", null);
        assertThrows(InvalidInputException.class, () -> {
            service.register(user);
        });
    }

    @Test
    public void registeringWithExistingUsernameThrowsUsernameTakenException() {
        var user = new UserData("username5000", "p455w0rd", "email@email.com");
        dataAccess.createUser(user);

        var newUser = new UserData("username5000", "5tr0ng3rp455w0rd", "betteremail@betteremail.com");
        assertThrows(UsernameTakenException.class, () -> {
            service.register(newUser);
        });
    }

    @Test
    public void registeringUserReturnsCorrectAuthenticationData() throws DataAccessException, InvalidInputException, UsernameTakenException {
        var user = new UserData("username5000", "p455w0rd", "email@email.com");
        var result = service.register(user);

        assertEquals(user.username(), result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void loggingInWithoutRegisteredAccountThrowsUserNotRegisteredException() {
        var user = new UserData("username5000", "p455w0rd", "email@email.com");
        assertThrows(UserNotRegisteredException.class, () -> {
            service.login(user);
        });
    }

    @Test
    public void loggingInWithIncorrectPasswordThrowsUnauthorizedUserException() {
        var user = new UserData("username5000", "p455w0rd", "email@email.com");
        dataAccess.createUser(user);

        var loginInfo = new UserData("username5000", "password", null);
        assertThrows(UnauthorizedUserException.class, () -> {
            service.login(loginInfo);
        });
    }

    @Test
    public void loggingInReturnsCorrectAuthenticationData() throws UserNotRegisteredException, UnauthorizedUserException, DataAccessException {
        var user = new UserData("username5000", "p455w0rd", "email@email.com");
        dataAccess.createUser(user);

        var result = service.login(user);
        assertEquals(user.username(), result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void loggingOutWithInvalidAuthTokenThrowsUnauthorizedUserException() throws UnauthorizedUserException, UserNotRegisteredException, DataAccessException {
        var user = new UserData("username5000", "p455w0rd", "email@email.com");
        dataAccess.createUser(user);

        String fakeAuthToken = "abc123def456";

        assertThrows(UnauthorizedUserException.class, () -> {
            service.logout(fakeAuthToken);
        });
    }

    @Test
    public void loggingOutWithValidAuthTokenDeletesAuthData() throws UnauthorizedUserException, UserNotRegisteredException, DataAccessException {
        var user = new UserData("username5000", "p455w0rd", "email@email.com");
        dataAccess.createUser(user);

        AuthData loginResult = service.login(user);
        String authToken = loginResult.authToken();

        service.logout(authToken);
        assertNull(dataAccess.getAuth(authToken));
    }

    @Test
    public void clearingApplicationDeletesAllDataObjects() throws InvalidInputException, UsernameTakenException, DataAccessException {
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

        // Eventually add games and assert they're not null

        service.clearApplication();

        for (UserData user : users) {
            assertNull(dataAccess.getUser(user.username()));
        }

        for (AuthData auth : auths) {
            assertNull(dataAccess.getAuth(auth.authToken()));
        }

        // Eventually assert that games are null
    }
}
