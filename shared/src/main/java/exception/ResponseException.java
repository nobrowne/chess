package exception;

public class ResponseException extends Exception {
  private final int statusCode;

  public ResponseException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  public int statusCode() {
    return statusCode;
  }
}
