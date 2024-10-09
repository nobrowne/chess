package dataaccess.game;

import model.GameData;

import java.util.HashMap;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {
    private Map<Integer, GameData> games = new HashMap<>();

    @Override
    public void clear() {
        games.clear();
    }
}
