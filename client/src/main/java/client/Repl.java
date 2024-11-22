package client;

import java.util.Scanner;

public class Repl {
  private final ChessClient client;

  public Repl(String serverUrl) {
    client = new ChessClient(serverUrl);
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
}
