package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServiceTests {
    private MemoryDataAccess dataAccess;
    private Service service;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        service = new Service(dataAccess);
    }

    @Test
    public void register_with_existing_username_throws_usernameTakenException() throws UsernameTakenException, DataAccessException {
        var user = new UserData("username5000", "p455w0rd", "email@email.com");
        dataAccess.createUser(user);

        var newUser = new UserData("username5000", "5tr0ng3rp455w0rd", "betteremail@betteremail.com");
        assertThrows(UsernameTakenException.class, () -> {
            service.registerUser(newUser);
        });
    }
}
