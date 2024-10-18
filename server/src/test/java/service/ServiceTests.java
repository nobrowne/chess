package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
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
    }

    @Test
    public void registeringWithMissingUserDataThrowsInvalidInputException() {
        var registerRequest = new UserData(newUser.username(), newUser.password(), null);
        assertThrows(InvalidInputException.class, () -> {
            service.register(registerRequest);
        });
    }

    @Test
    public void registeringWithExistingUsernameThrowsUsernameTakenException() {
        var registerRequest = new UserData(existingUser.username(), newUser.password(), newUser.email());
        assertThrows(UsernameTakenException.class, () -> {
            service.register(registerRequest);
        });
    }

    @Test
    public void registeringUserReturnsCorrectAuthenticationData() throws DataAccessException, InvalidInputException, UsernameTakenException {
        var registerResult = service.register(newUser);

        assertEquals(newUser.username(), registerResult.username());
        assertNotNull(registerResult.authToken());
    }

    @Test
    public void loggingInWithoutRegisteredAccountThrowsUserNotRegisteredException() {
        assertThrows(UserNotRegisteredException.class, () -> {
            service.login(newUser);
        });
    }

    @Test
    public void loggingInWithIncorrectPasswordThrowsUnauthorizedUserException() {
        var loginRequest = new UserData(existingUser.username(), newUser.password(), null);

        assertThrows(UnauthorizedUserException.class, () -> {
            service.login(loginRequest);
        });
    }

    @Test
    public void loggingInReturnsCorrectAuthenticationData() throws UserNotRegisteredException, UnauthorizedUserException, DataAccessException {
        var loginResult = service.login(existingUser);

        assertEquals(existingUser.username(), loginResult.username());
        assertNotNull(loginResult.authToken());
    }

    @Test
    public void loggingOutWithInvalidAuthTokenThrowsUnauthorizedUserException() throws UnauthorizedUserException, UserNotRegisteredException, DataAccessException {
        String fakeAuthToken = "abc123def456";

        assertNotEquals(fakeAuthToken, existingAuthToken);

        assertThrows(UnauthorizedUserException.class, () -> {
            service.logout(fakeAuthToken);
        });
    }

    @Test
    public void loggingOutWithValidAuthTokenDeletesAuthData() throws UnauthorizedUserException, UserNotRegisteredException, DataAccessException {
        service.logout(existingAuthToken);
        assertNull(dataAccess.getAuth(existingAuthToken));
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
