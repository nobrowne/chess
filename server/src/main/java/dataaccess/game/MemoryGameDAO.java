package dataaccess.game;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    private final HashMap<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public GameData getGame(Integer gameID) {
        return games.get(gameID);
    }

    @Override
    public ArrayList<GameData> listGames() {
        return new ArrayList<>(games.values());
    }

    @Override
    public int createGame(String gameName, ChessGame game) {
        int gameID = nextGameID++;
        GameData newGame = new GameData(gameID, null, null, gameName, game);

        games.put(gameID, newGame);

        return gameID;
    }

    @Override
    public void updateGame(GameData game) {
        games.put(game.gameID(), game);
    }

    @Override
    public void clear() {
        games.clear();
        nextGameID = 1;
    }
}
