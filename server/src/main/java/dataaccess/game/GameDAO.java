package dataaccess.game;

import dataaccess.DataAccessException;
import model.GameData;

import java.util.ArrayList;

public interface GameDAO {
    GameData getGame(Integer gameID) throws DataAccessException;

    ArrayList<GameData> listGames() throws DataAccessException;

    void createGame(GameData game) throws DataAccessException;

    void updateGame(GameData game);

    void clear() throws DataAccessException;
}
