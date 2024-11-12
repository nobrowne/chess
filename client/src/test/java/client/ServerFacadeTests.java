package client;

import exception.ResponseException;
import model.AuthData;
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
    AuthData authData = serverFacade.register(newUser);
    System.out.printf(authData.toString());
  }
}
