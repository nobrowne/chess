package dataaccess.auth;

import java.util.HashMap;
import model.AuthData;

public class MemoryAuthDAO implements AuthDAO {
  private final HashMap<String, AuthData> auths = new HashMap<>();

  @Override
  public AuthData getAuth(String authToken) {
    return auths.get(authToken);
  }

  @Override
  public void createAuth(AuthData auth) {
    auths.put(auth.authToken(), auth);
  }

  @Override
  public void deleteAuth(String authToken) {
    auths.remove(authToken);
  }

  @Override
  public void clear() {
    auths.clear();
  }
}
