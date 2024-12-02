package client;

import client.websocket.ServerMessageHandler;
import java.util.Scanner;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

public class Repl implements ServerMessageHandler {
  private final ChessClient client;

  public Repl(String serverUrl) {
    client = new ChessClient(serverUrl, this);
  }

  public void run() {
    System.out.println("Welcome to Ghetto Chess. Type 'help' to get started");

    Scanner scanner = new Scanner(System.in);
    String result = "";
    while (!result.equals("quit")) {
      printPrompt();
      String input = scanner.nextLine();

      try {
        result = client.eval(input);
        if (result.equals("quit")) {
          System.out.print("");
        } else {
          System.out.println(result);
        }
      } catch (Throwable ex) {
        String message = ex.toString();
        System.out.print(message);
      }
    }
  }

  private void printPrompt() {
    System.out.print("\n" + ">>> ");
  }

  @Override
  public void notify(ServerMessage serverMessage) {
    // Will change to switch statement based on type, but I'm just using this for testing rn.
    NotificationMessage notificationMessage = (NotificationMessage) serverMessage;
    System.out.println("Notification: " + notificationMessage.getNotification());
  }
}
