package dataaccess;

import model.AuthData;
import model.UserData;

public interface DataAccess {
    UserData getUser(UserData user);

    void createUser(UserData user);

    AuthData getAuth(AuthData auth);

    void createAuth(AuthData auth);

    void deleteAuth(AuthData auth);

    void clear();
}
