package websocket.messages;

import chess.ChessGame;
import java.util.Objects;

public class LoadGameMessage extends ServerMessage {
  public ChessGame game;
  public boolean isWhitePerspective;

  public LoadGameMessage(ServerMessageType type, ChessGame game, boolean isWhitePerspective) {
    super(type);
    this.game = game;
    this.isWhitePerspective = isWhitePerspective;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LoadGameMessage that)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    return Objects.equals(game, that.game);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), game);
  }
}
