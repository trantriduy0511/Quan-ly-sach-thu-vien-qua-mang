package model;

import java.io.Serializable;

public class BookCopy implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String copyId;
    private String bookId;
    private String status; // "AVAILABLE", "BORROWED", "LOST", "DAMAGED"
    private String location; // e.g., "Tầng 4 - Kệ 10 - Ngăn 7"
    private String notes;
    
    public BookCopy() {
    }
    
    public BookCopy(String copyId, String bookId, String status, String location, String notes) {
        this.copyId = copyId;
        this.bookId = bookId;
        this.status = status;
        this.location = location;
        this.notes = notes;
    }
    
    public String getCopyId() {
        return copyId;
    }
    
    public void setCopyId(String copyId) {
        this.copyId = copyId;
    }
    
    public String getBookId() {
        return bookId;
    }
    
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}





