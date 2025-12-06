import java.io.*;
import java.net.*;
import java.util.*;

public class CoordinatorServer {
    private static final int PORT = 4000;
    private static final List<String> LIBRARIES = Arrays.asList(
        "localhost:5001", "localhost:5002", "localhost:5003"
    );

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Coordinator running on port " + PORT);

            while (true) {
                Socket client = serverSocket.accept();
                new Thread(() -> handleClient(client)).start();
            }
        }
    }

    private static void handleClient(Socket client) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
             PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

            String command = in.readLine();
            if (command == null) return;

            if (command.startsWith("search ")) {
                String keyword = command.substring(7);
                Set<String> results = new HashSet<>();

                for (String lib : LIBRARIES) {
                    try (Socket libSocket = new Socket()) {
                        libSocket.connect(new InetSocketAddress(lib.split(":")[0],
                                Integer.parseInt(lib.split(":")[1])), 1000);
                        PrintWriter libOut = new PrintWriter(libSocket.getOutputStream(), true);
                        BufferedReader libIn = new BufferedReader(new InputStreamReader(libSocket.getInputStream()));

                        libOut.println("SEARCH|" + keyword);
                        String line;
                        while ((line = libIn.readLine()) != null && !line.equals("END_RESULTS")) {
                            results.add(line);
                        }
                    } catch (Exception ignored) {}
                }

                for (String r : results) out.println(r);
                out.println("END_RESULTS");

            } else if (command.startsWith("lease ") || command.startsWith("return ")) {
                // Simple forward: try all libraries until one accepts
                int bookId = Integer.parseInt(command.split(" ")[1]);
                String type = command.startsWith("lease ") ? "LEASE" : "RETURN";

                for (String lib : LIBRARIES) {
                    try (Socket s = new Socket(lib.split(":")[0], Integer.parseInt(lib.split(":")[1]));
                         PrintWriter libOut = new PrintWriter(s.getOutputStream(), true);
                         BufferedReader libIn = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

                        libOut.println(type + "|" + bookId);
                        String response = libIn.readLine();
                        if (response != null && response.startsWith("STATUS|OK")) {
                            out.println("Book " + bookId + " " + (type.equals("LEASE") ? "leased" : "returned") + " successfully.");
                            return;
                        } else if (response != null && response.equals("STATUS|NOT_FOUND")) {
                        }
                    } catch (Exception ignored) {}
                }
                out.println("Book not found or already leased.");

            } else if (command.equals("stats")) {
                out.println("Statistics not fully implemented yet — but coordinator works!");
            }

        } catch (Exception e) {
        }
    }
}
