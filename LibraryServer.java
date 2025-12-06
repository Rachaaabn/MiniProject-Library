import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class LibraryServer {
    private static final int PORT = 5001;        // Change to 5002, 5003 for other libraries
    private static final int LIBRARY_ID = 1;

    private final List<Book> books = new ArrayList<>();
    private final Map<String, Integer> searchCount = new HashMap<>();

    public LibraryServer() {
        books.add(new Book(101, "Java Concurrency in Practice", "Brian Goetz",
                new String[]{"java", "concurrency", "threads"}, LIBRARY_ID));
        books.add(new Book(102, "Effective Java", "Joshua Bloch",
                new String[]{"java", "best practices"}, LIBRARY_ID));
        books.add(new Book(103, "Clean Code", "Robert Martin",
                new String[]{"refactoring", "java"}, LIBRARY_ID));
    }

    public static void main(String[] args) throws IOException {
        LibraryServer server = new LibraryServer();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Library " + LIBRARY_ID + " running on port " + PORT);

            while (true) {
                Socket client = serverSocket.accept();
                new Thread(() -> handleConnection(client, server)).start();
            }
        }
    }

    private static void handleConnection(Socket socket, LibraryServer server) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String request = in.readLine();
            if (request == null) return;

            if (request.startsWith("SEARCH|")) {
                String keyword = request.substring(7).toLowerCase();
                server.searchCount.merge(keyword, 1, Integer::sum);

                List<Book> matches = server.books.stream()
                    .filter(b -> b.getTitle().toLowerCase().contains(keyword) ||
                                 b.getAuthor().toLowerCase().contains(keyword) ||
                                 Arrays.stream(b.getKeywords()).anyMatch(k -> k.toLowerCase().contains(keyword)))
                    .collect(Collectors.toList());

                for (Book b : matches) {
                    out.println(String.format("RESULT|%d|%d|%s|%s|%s",
                            LIBRARY_ID, b.getId(), b.getTitle(), b.getAuthor(), b.isLeased()));
                }
                out.println("END_RESULTS");

            } else if (request.startsWith("LEASE|") || request.startsWith("RETURN|")) {
                String[] parts = request.split("\\|");
                int bookId = Integer.parseInt(parts[1]);
                Book book = server.findBookById(bookId);

                if (book == null) {
                    out.println("STATUS|NOT_FOUND");
                } else if (request.startsWith("LEASE|") && book.isLeased()) {
                    out.println("STATUS|ALREADY_LEASED");
                } else {
                    book.setLeased(request.startsWith("LEASE|"));
                    out.println("STATUS|OK");
                }

            } else if (request.equals("STATS")) {
                int total = server.searchCount.values().stream().mapToInt(Integer::intValue).sum();
                String top = server.searchCount.entrySet().stream()
                        .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("none");
                int count = server.searchCount.getOrDefault(top, 0);
                out.println("STATS|" + total + "|" + top + "|" + count);
            }

        } catch (Exception e) {
            // Silent ignore — coordinator may close connection
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private Book findBookById(int id) {
        return books.stream().filter(b -> b.getId() == id).findFirst().orElse(null);
    }

    public List<Book> getBooks() {
        return books;
    }

    public Map<String, Integer> getSearchCount() {
        return searchCount;
    }
}
