package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import exception.ResponseException;
import model.UserData;
import service.*;
import spark.Request;
import spark.Response;
import spark.Spark;

public class Server {
    private final MemoryDataAccess dataAccess = new MemoryDataAccess();
    private final Service service = new Service(dataAccess);

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/db", this::clearApplication);

        Spark.exception(UsernameTakenException.class, this::exceptionHandler);
        Spark.exception(InvalidInputException.class, this::exceptionHandler);

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
        res.status(ex.StatusCode());
    }

    public Object register(Request req, Response res) throws DataAccessException, UsernameTakenException, InvalidInputException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        var result = service.register(user);

        res.type("application/json");
        res.status(200);
        return new Gson().toJson(result);
    }

    public Object login(Request req, Response res) throws UserNotRegisteredException, DataAccessException, InvalidPasswordException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        var result = service.login(user);

        res.type("application/json");
        res.status(200);
        return new Gson().toJson(result);
    }

    public Object clearApplication(Request req, Response res) throws DataAccessException {
        service.clearApplication();

        res.type("application/json");
        res.status(200);
        return "";
    }
}
