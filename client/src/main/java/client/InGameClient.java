package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import client.websocket.WebSocketFacade;
import exception.ResponseException;
import java.util.Arrays;
import java.util.Scanner;
import serverfacade.ServerFacade;
import ui.BoardDrawer;

public class InGameClient implements ClientInterface {
  protected final ChessClient chessClient;
  protected final ServerFacade serverFacade;

  public InGameClient(ChessClient chessClient, ServerFacade serverFacade) {
    this.chessClient = chessClient;
    this.serverFacade = serverFacade;
  }

  @Override
  public String eval(String input) {
    try {
      var tokens = input.toLowerCase().split(" ");
      var command = (tokens.length > 0) ? tokens[0] : "help";
      var params = Arrays.copyOfRange(tokens, 1, tokens.length);

      return switch (command) {
        case "highlight" -> highlightLegalMoves(params);
        case "redraw" -> redrawBoard();
        case "leave" -> leaveGame();
        default -> help();
      };
    } catch (ResponseException ex) {
      return ex.getMessage();
    }
  }

  protected String highlightLegalMoves(String... params) throws ResponseException {
    int gameID = chessClient.getCurrentInternalGameID();
    ChessGame game = chessClient.getGame(gameID).game();

    ChessPosition startPosition = makeSurePieceIsNotNull(game, ChessPosition.fromString(params[0]));

    ChessGame.TeamColor teamColor = chessClient.getCurrentTeamColor();
    boolean isWhitePerspective = teamColor == null || teamColor.equals(ChessGame.TeamColor.WHITE);
    BoardDrawer.drawBoard(game, isWhitePerspective, startPosition);

    return "Here are the moves the selected piece can make";
  }

  protected String leaveGame() throws ResponseException {
    WebSocketFacade ws = chessClient.getWebSocketFacade();
    ws.leaveGame(chessClient.getAuthToken(), chessClient.getCurrentInternalGameID());
    chessClient.setCurrentClient(new PostLoginClient(chessClient, serverFacade));

    return "You have left the game";
  }

  protected String redrawBoard() throws ResponseException {
    int gameID = chessClient.getCurrentInternalGameID();
    ChessGame game = chessClient.getGame(gameID).game();
    ChessGame.TeamColor teamColor = chessClient.getCurrentTeamColor();
    boolean isWhitePerspective = teamColor == null || teamColor.equals(ChessGame.TeamColor.WHITE);
    BoardDrawer.drawBoard(game, isWhitePerspective, null);

    return "Here is your redrawn board";
  }

  @Override
  public String help() {
    return """
        While you observe a game, here are your options:

        - highlight <piece position> : highlight legal moves
        - redraw: redraw the chess board
        - leave: remove yourself from the game
        - help: see possible commands

        While you play a game, here are some additional options:

        - move <start position> <end position>: make a move (example: a7a8)
        - resign: forfeit the game, but must type 'leave' to leave it
        """;
  }
  
  protected ChessPosition makeSurePieceIsNotNull(ChessGame game, ChessPosition position) {
    ChessBoard board = game.getBoard();
    ChessPiece piece = board.getPiece(position);
    
    Scanner scanner = new Scanner(System.in);
    boolean firstPrompt = true;
    
    while (piece == null) {
      if (firstPrompt) {
        System.out.println("Please pick a position with a piece currently on it");
        firstPrompt = false;
      }
      
      System.out.print(">>> ");
      String response = scanner.nextLine().trim().toLowerCase();
      
      try {
        position = ChessPosition.fromString(response);
        piece = board.getPiece(position);
        
        if (piece == null) {
          System.out.println("There is no piece at that position. Please pick another position.");
        }
      } catch (IllegalArgumentException e) {
        System.out.println("Invalid position. Please try again.");
      }
    }
    
    return position;
  }
}
