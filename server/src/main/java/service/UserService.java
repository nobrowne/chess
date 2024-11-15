package service;

import dataaccess.DataAccessException;
import dataaccess.auth.AuthDAO;
import dataaccess.user.UserDAO;
import java.util.UUID;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import request.LoginRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;
import service.exceptions.AlreadyTakenException;
import service.exceptions.InvalidInputException;
import service.exceptions.UnauthorizedUserException;

public class UserService {
  private final AuthDAO authDAO;
  private final UserDAO userDAO;
  private final AuthService authService;

  public UserService(AuthDAO authDAO, UserDAO userDAO, AuthService authService) {
    this.authDAO = authDAO;
    this.userDAO = userDAO;
    this.authService = authService;
  }

  public RegisterResult register(RegisterRequest registerRequest)
      throws DataAccessException, InvalidInputException, AlreadyTakenException {
    String username = registerRequest.username();
    String password = registerRequest.password();
    String email = registerRequest.email();

    if (username == null
        || username.isBlank()
        || password == null
        || password.isBlank()
        || email == null
        || email.isBlank()) {
      throw new InvalidInputException("error: username, password, and email must all be filled");
    }

    if (userDAO.getUser(username) != null) {
      throw new AlreadyTakenException("error: username already taken");
    }

    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
    userDAO.createUser(new UserData(username, hashedPassword, email));

    String authToken = generateAuthToken();
    authDAO.createAuth(new AuthData(username, authToken));

    return new RegisterResult(username, authToken);
  }

  public LoginResult login(LoginRequest loginRequest)
      throws DataAccessException, UnauthorizedUserException, InvalidInputException {
    String username = loginRequest.username();
    String password = loginRequest.password();

    if (username == null || username.isBlank() || password == null || password.isBlank()) {
      throw new InvalidInputException("error: username and password must be filled");
    }

    UserData user = userDAO.getUser(username);

    if (user == null) {
      throw new UnauthorizedUserException("error: user has not registered an account yet");
    }

    if (!BCrypt.checkpw(password, user.password())) {
      throw new UnauthorizedUserException("error: invalid password");
    }

    String authToken = generateAuthToken();
    authDAO.createAuth(new AuthData(username, authToken));

    return new LoginResult(username, authToken);
  }

  public void logout(String authToken) throws DataAccessException, UnauthorizedUserException {
    authService.validateAuthToken(authToken);
    authDAO.deleteAuth(authToken);
  }

  private String generateAuthToken() throws DataAccessException {
    String authToken;

    do {
      authToken = UUID.randomUUID().toString();
    } while (authDAO.getAuth(authToken) != null);

    return authToken;
  }
}
