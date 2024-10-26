package dataaccess.game;

import dataaccess.DataAccessException;
import model.GameData;

import java.util.ArrayList;

public class SQLGameDAO implements GameDAO {
    @Override
    public GameData getGame(Integer gameID) throws DataAccessException {
        return null;
    }

    @Override
    public ArrayList<GameData> listGames() throws DataAccessException {
        return null;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {

    }

    @Override
    public void updateGame(GameData game) {

    }

    @Override
    public void clear() throws DataAccessException {

    }
}
