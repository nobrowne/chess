package server;

import com.google.gson.Gson;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import service.Service;
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
        Spark.post("/user", this::registerUser);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    public Object registerUser(Request req, Response res) {
        UserData user = new Gson().fromJson(req.body(), UserData.class);
        AuthData auth = service.registerUser(user);

        res.type("application/json");
        res.status(200);
        return new Gson().toJson(auth);
    }
}
