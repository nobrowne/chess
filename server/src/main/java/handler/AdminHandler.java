package handler;

import dataaccess.DataAccessException;
import service.AuthService;
import spark.Response;
import spark.Request;

public class AdminHandler {
    private AuthService adminService;

    public AdminHandler(AuthService adminService) {
        this.adminService = adminService;
    }

    public Object handleClearApplicationRequest(Request request, Response response) {
        try {
            adminService.clear();
            response.status(200);
            return "";
        } catch (DataAccessException e) {
            response.status(500);
            response.type("application/json");
            // TODO: user gson to generate the json stuff
            return null;
        }
    }
}
