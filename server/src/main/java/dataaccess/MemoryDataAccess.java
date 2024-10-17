package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<String, AuthData> auths = new HashMap<>();
    private final HashMap<Integer, GameData> games = new HashMap<>();

    @Override
    public UserData getUser(UserData user) {
        return users.get(user.username());
    }

    @Override
    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    @Override
    public AuthData getAuth(AuthData auth) {
        return auths.get(auth.authToken());
    }

    @Override
    public void createAuth(AuthData auth) {
        auths.put(auth.authToken(), auth);
    }

    @Override
    public void deleteAuth(AuthData auth) {
        auths.remove(auth.authToken());
    }

    @Override
    public void clear() {
        users.clear();
        auths.clear();
        games.clear();
    }
}
