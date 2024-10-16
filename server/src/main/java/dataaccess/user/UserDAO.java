package dataaccess.user;

import model.UserData;

public interface UserDAO {
    void createUserData(UserData userData);

    UserData getUserData(UserData userData);

    void deleteUserData();
}
