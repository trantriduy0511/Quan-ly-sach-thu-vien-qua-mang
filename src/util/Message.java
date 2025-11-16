package util;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Authentication
    public static final String LOGIN = "LOGIN";
    public static final String LOGOUT = "LOGOUT";
    public static final String REGISTER = "REGISTER";
    public static final String FORCE_LOGOUT = "FORCE_LOGOUT";
    
    // Books
    public static final String GET_ALL_BOOKS = "GET_ALL_BOOKS";
    public static final String SEARCH_BOOKS = "SEARCH_BOOKS";
    public static final String GET_BOOK_BY_ID = "GET_BOOK_BY_ID";
    public static final String ADD_BOOK = "ADD_BOOK";
    public static final String UPDATE_BOOK = "UPDATE_BOOK";
    public static final String DELETE_BOOK = "DELETE_BOOK";
    
    // Book Copies
    public static final String GET_BOOK_COPIES = "GET_BOOK_COPIES";
    public static final String ADD_BOOK_COPY = "ADD_BOOK_COPY";
    public static final String DELETE_BOOK_COPY = "DELETE_BOOK_COPY";
    
    // Users
    public static final String GET_ALL_USERS = "GET_ALL_USERS";
    public static final String SEARCH_USERS = "SEARCH_USERS";
    public static final String GET_USER_BY_ID = "GET_USER_BY_ID";
    public static final String ADD_USER = "ADD_USER";
    public static final String UPDATE_USER = "UPDATE_USER";
    public static final String DELETE_USER = "DELETE_USER";
    public static final String LOCK_USER = "LOCK_USER";
    public static final String UNLOCK_USER = "UNLOCK_USER";
    public static final String RESET_PASSWORD = "RESET_PASSWORD";
    
    // Borrow/Return
    public static final String BORROW_BOOK = "BORROW_BOOK";
    public static final String RETURN_BOOK = "RETURN_BOOK";
    public static final String GET_USER_BORROW_RECORDS = "GET_USER_BORROW_RECORDS";
    public static final String GET_ALL_BORROW_RECORDS = "GET_ALL_BORROW_RECORDS";
    public static final String MARK_LOST = "MARK_LOST";
    public static final String MARK_DAMAGED = "MARK_DAMAGED";
    public static final String FORCE_RETURN = "FORCE_RETURN";
    public static final String RENEW_BOOK = "RENEW_BOOK";
    
    // Statistics/Dashboard
    public static final String GET_DASHBOARD_STATS = "GET_DASHBOARD_STATS";
    public static final String GET_BOOK_REPORT = "GET_BOOK_REPORT";
    public static final String GET_USER_REPORT = "GET_USER_REPORT";
    public static final String GET_BORROW_REPORT = "GET_BORROW_REPORT";
    public static final String GET_PENALTY_REPORT = "GET_PENALTY_REPORT";
    
    // Notifications
    public static final String GET_USER_NOTIFICATIONS = "GET_USER_NOTIFICATIONS";
    public static final String MARK_NOTIFICATION_READ = "MARK_NOTIFICATION_READ";
    
    // Account status check
    public static final String CHECK_USER_STATUS = "CHECK_USER_STATUS";
    
    // Settings
    public static final String GET_SETTINGS = "GET_SETTINGS";
    public static final String UPDATE_SETTINGS = "UPDATE_SETTINGS";
    
    private String type;
    private Object data;
    private boolean success;
    private String message;
    
    public Message() {
    }
    
    public Message(String type, Object data) {
        this.type = type;
        this.data = data;
        this.success = false;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
