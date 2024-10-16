package server.handlers;

import com.google.gson.Gson;
import server.dto.request.RegisterRequest;
import server.dto.response.RegisterResponse;
import service.UserService;
import spark.Request;
import spark.Response;

public class UserHandler {
    private final UserService userService;
    private final Gson serializer;

    public UserHandler(UserService userService) {
        this.userService = userService;
        this.serializer = new Gson();
    }

    public Object register(Request req, Response res) {
        res.type("application/json");

        try {
            RegisterRequest registerRequest = serializer.fromJson(req.body(), RegisterRequest.class);
            RegisterResponse registerResponse = userService.register(registerRequest);
        }
    }
}
