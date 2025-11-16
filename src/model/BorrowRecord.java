package model;

import java.io.Serializable;
import java.sql.Date;

public class BorrowRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String recordId;
    private String userId;
    private String bookId;
    private String copyId;
    private Date borrowDate;
    private Date dueDate;
    private Date returnDate;
    private String status; // "BORROWING", "RETURNED", "LOST", "DAMAGED"
    private double fine;
    private String username;
    private String bookTitle;
    
    public BorrowRecord() {
    }
    
    public String getRecordId() {
        return recordId;
    }
    
    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getBookId() {
        return bookId;
    }
    
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    
    public String getCopyId() {
        return copyId;
    }
    
    public void setCopyId(String copyId) {
        this.copyId = copyId;
    }
    
    public Date getBorrowDate() {
        return borrowDate;
    }
    
    public void setBorrowDate(Date borrowDate) {
        this.borrowDate = borrowDate;
    }
    
    public Date getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
    
    public Date getReturnDate() {
        return returnDate;
    }
    
    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public double getFine() {
        return fine;
    }
    
    public void setFine(double fine) {
        this.fine = fine;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    // For backward compatibility
    public int getId() {
        try {
            if (recordId != null && recordId.startsWith("record_")) {
                return Integer.parseInt(recordId.substring(7), 16) % Integer.MAX_VALUE;
            }
        } catch (Exception e) {}
        return recordId != null ? recordId.hashCode() : 0;
    }
}
