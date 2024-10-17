package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.UserData;

public class Service {
    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData registerUser(UserData user) {
        return null;
    }
}
