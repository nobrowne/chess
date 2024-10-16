package dataaccess.user;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO {
    private Map<String, UserData> user = new HashMap<>();

    @Override
    public void createUserData(UserData userData) {
        user.put(userData.username, userData);
    }

    @Override
    public UserData getUserData(UserData userData) {
        return null;
    }

    @Override
    public void deleteUserData() {

    }
}
