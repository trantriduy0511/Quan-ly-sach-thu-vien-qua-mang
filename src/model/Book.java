// Class cho sách

package model;

import java.io.Serializable;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String title;
    private String author;
    private boolean available;

    public Book(String id, String title, String author, boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.available = available;
    }

    // Getter và Setter
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return title + " - " + author + (available ? " [Available]" : " [Borrowed]");
    }
}

