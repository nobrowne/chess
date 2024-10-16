package server.dto.response;

public record RegisterResponse(String username, String authToken, String message) {
}
