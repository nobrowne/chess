package dataaccess.user;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO {
    private Map<String, UserData> users = new HashMap<>();

    @Override
    public void clear() {
        users.clear();
    }
}
