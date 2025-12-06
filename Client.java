import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String COORDINATOR = "localhost";
    private static final int COORD_PORT = 4000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(COORDINATOR, COORD_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to Coordinator. Commands:");
            System.out.println("  search <keyword>");
            System.out.println("  lease <book_id>");
            System.out.println("  return <book_id>");
            System.out.println("  stats");
            System.out.println("  quit");

            // Thread to read responses from server
            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.equals("END_RESULTS")) {
                            System.out.println("────────────────────────────────────");
                        } else if (line.startsWith("RESULT|")) {
                            String[] p = line.split("\\|");
                            System.out.printf("Lib:%s | ID:%s | %s - %s [Leased: %s]%n",
                                    p[1], p[2], p[3], p[4], p[5]);
                        } else {
                            System.out.println(line);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            }).start();

            // Main loop: read user input
            while (true) {
                System.out.print("> ");
                String cmd = scanner.nextLine().trim();
                if (cmd.equalsIgnoreCase("quit")) break;
                out.println(cmd);
            }

        } catch (IOException e) {
            System.out.println("Cannot connect to coordinator. Is it running?");
        }
    }
}
