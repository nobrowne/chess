package server;

import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", (request, response) -> "register");
        Spark.post("/session", (request, response) -> "login");
        Spark.delete("/session", (request, response) -> "logout");
        Spark.get("/game", (request, response) -> "list games");
        Spark.post("/game", (request, response) -> "create game");
        Spark.put("/game", (request, response) -> "join game");
        Spark.delete("/db", (request, response) -> "clear application");

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
