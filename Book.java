import java.io.Serializable;
import java.util.Arrays;

public class Book implements Serializable {
    private final int id;
    private final String title;
    private final String author;
    private final String[] keywords;
    private boolean leased;
    private final int libraryId;  // Which library owns this book

    // Constructor
    public Book(int id, String title, String author, String[] keywords, int libraryId) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.keywords = keywords;
        this.libraryId = libraryId;
        this.leased = false;  // Books start available
    }

    public Book(String author, int id, String[] keywords, String title) {
        this.author = author;
        this.id = id;
        this.keywords = keywords;
        this.title = title;
        this.libraryId = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String[] getKeywords() { return keywords; }
    public boolean isLeased() { return leased; }
    public void setLeased(boolean leased) { this.leased = leased; }
    public int getLibraryId() { return libraryId; }

    @Override
    public String toString() {
        return "Book{id=" + id + ", title='" + title + "', author='" + author + "', leased=" + leased + "}";
    }
}
