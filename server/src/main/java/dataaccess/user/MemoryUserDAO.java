package dataaccess.user;

import java.util.HashMap;
import model.UserData;

public class MemoryUserDAO implements UserDAO {
  private final HashMap<String, UserData> users = new HashMap<>();

  @Override
  public UserData getUser(String username) {
    return users.get(username);
  }

  @Override
  public void createUser(UserData user) {
    users.put(user.username(), user);
  }

  @Override
  public void clear() {
    users.clear();
  }
}
