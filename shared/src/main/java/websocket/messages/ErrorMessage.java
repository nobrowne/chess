package websocket.messages;

import java.util.Objects;

public class ErrorMessage extends ServerMessage {
  public String errorMessage;

  public ErrorMessage(ServerMessageType type, String errorMessage) {
    super(type);
    this.errorMessage = errorMessage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ErrorMessage that)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    return Objects.equals(errorMessage, that.errorMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), errorMessage);
  }
}
