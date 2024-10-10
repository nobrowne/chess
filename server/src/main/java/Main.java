import dataaccess.auth.AuthDAO;
import dataaccess.auth.MemoryAuthDAO;
import dataaccess.auth.SQLAuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.game.MemoryGameDAO;
import dataaccess.game.SQLGameDAO;
import dataaccess.user.MemoryUserDAO;
import dataaccess.user.SQLUserDAO;
import dataaccess.user.UserDAO;
import server.Server;
import service.AuthService;
import service.GameService;
import service.UserService;

public class Main {
    public static void main(String[] args) {
        try {
            int port = 8080;

            if (args.length >= 1) {
                port = Integer.parseInt(args[0]);
            }

            AuthDAO authDAO = new MemoryAuthDAO();
            GameDAO gameDAO = new MemoryGameDAO();
            UserDAO userDAO = new MemoryUserDAO();

            if (args.length >= 2 && args[1].equals("sql")) {
                authDAO = new SQLAuthDAO();
                gameDAO = new SQLGameDAO();
                userDAO = new SQLUserDAO();
            }

            AuthService authService = new AuthService(authDAO, gameDAO, userDAO);
            GameService gameService = new GameService(authDAO, gameDAO);
            UserService userService = new UserService(authDAO, userDAO);

            Server server = new Server(authService, gameService, userService);
            port = server.run(port);

            System.out.printf("Server started on port %d with %s, %s, %s%n",
                              port, authDAO.getClass(), gameDAO.getClass(), userDAO.getClass());
            return;
        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }
        System.out.println("""
                To start the Chess Server, run:
                java Main <port> [sql]
                """);
    }
}