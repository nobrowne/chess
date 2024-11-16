package client;

import java.util.Scanner;

public class Repl {
  private final ChessClient client;

  public Repl(String serverUrl) {
    client = new ChessClient(serverUrl);
  }

  public void run() {
    System.out.println("Welcome to Ghetto Chess");
    System.out.print(client.help());

    Scanner scanner = new Scanner(System.in);
    String result = "";
    while (!result.equals("quit")) {
      printPrompt();
      String line = scanner.nextLine();

      try {
        result = client.eval(line);
        System.out.print(result);
      } catch (Throwable ex) {
        String message = ex.toString();
        System.out.print(message);
      }
    }
    System.out.println();
  }

  private void printPrompt() {
    System.out.print("\n" + ">>> ");
  }
}
