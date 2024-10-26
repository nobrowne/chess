package server;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import dataaccess.SQLDataAccess;
import exception.ResponseException;
import model.UserData;
import service.AlreadyTakenException;
import service.InvalidInputException;
import service.Service;
import service.UnauthorizedUserException;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.Map;

public class Server {
    private final SQLDataAccess dataAccess = new SQLDataAccess();
    private final Service service = new Service(dataAccess);

    public Server() throws DataAccessException {
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.

        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearApplication);

        Spark.exception(DataAccessException.class, this::exceptionHandler);
        Spark.exception(AlreadyTakenException.class, this::exceptionHandler);
        Spark.exception(InvalidInputException.class, this::exceptionHandler);
        Spark.exception(UnauthorizedUserException.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void exceptionHandler(ResponseException ex, Request req, Response res) {
        res.status(ex.statusCode());
        res.body(new Gson().toJson(Map.of("message", ex.getMessage())));
    }

    public Object register(Request req, Response res)
            throws DataAccessException, InvalidInputException, AlreadyTakenException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        var result = service.register(user);

        res.status(200);
        return new Gson().toJson(result);
    }

    public Object login(Request req, Response res) throws DataAccessException, UnauthorizedUserException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        var result = service.login(user);

        res.status(200);
        return new Gson().toJson(result);
    }

    public Object logout(Request req, Response res) throws DataAccessException, UnauthorizedUserException {
        String authToken = req.headers("Authorization");
        service.logout(authToken);

        res.status(200);
        return "{}";
    }

    public Object listGames(Request req, Response res) throws DataAccessException, UnauthorizedUserException {
        String authToken = req.headers("Authorization");
        var result = service.listGames(authToken);

        res.status(200);
        return new Gson().toJson(Map.of("games", result));
    }

    public Object createGame(Request req, Response res) throws DataAccessException, UnauthorizedUserException {
        String authToken = req.headers("Authorization");
        String gameName = req.body();
        int result = service.createGame(authToken, gameName);

        res.status(200);
        return new Gson().toJson(Map.of("gameID", result));
    }

    public Object joinGame(Request req, Response res)
            throws DataAccessException, UnauthorizedUserException, InvalidInputException, AlreadyTakenException {
        String authToken = req.headers("Authorization");

        JsonObject body = new Gson().fromJson(req.body(), JsonObject.class);

        JsonElement playerColorElement = body.get("playerColor");
        if (playerColorElement == null || playerColorElement.getAsString().isEmpty()) {
            throw new InvalidInputException("error: missing or invalid playerColor");
        }
        ChessGame.TeamColor teamColor = ChessGame.TeamColor.valueOf(playerColorElement.getAsString());

        JsonElement gameIDElement = body.get("gameID");
        if (gameIDElement == null) {
            throw new InvalidInputException("error: invalid gameID");
        }
        int gameID = gameIDElement.getAsInt();

        service.joinGame(authToken, teamColor, gameID);

        res.status(200);
        return "{}";
    }

    public Object clearApplication(Request req, Response res) throws DataAccessException {
        service.clearApplication();

        res.status(200);
        return "{}";
    }
}
