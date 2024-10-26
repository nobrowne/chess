package dataaccess.monolith;

import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;

public interface DataAccess {
    UserData getUser(String username) throws DataAccessException;

    void createUser(UserData user) throws DataAccessException;

    AuthData getAuth(String authToken) throws DataAccessException;

    void createAuth(AuthData auth) throws DataAccessException;

    void deleteAuth(String authToken) throws DataAccessException;

    GameData getGame(Integer gameID) throws DataAccessException;

    ArrayList<GameData> listGames() throws DataAccessException;

    void createGame(GameData game) throws DataAccessException;

    void updateGame(GameData game);

    void clearApplication() throws DataAccessException;
}
