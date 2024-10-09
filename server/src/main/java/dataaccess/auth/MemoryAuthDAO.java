package dataaccess.auth;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO {
    private Map<String, AuthData> auths = new HashMap<>();

    @Override
    public void clear() {
        auths.clear();
    }
}
