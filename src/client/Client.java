package client;

import util.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private boolean connected;
    
    public Client() {
        connected = false;
    }
    
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            connected = true;
            return true;
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            return false;
        }
    }
    
    public Message sendRequest(Message request) {
        if (!connected) {
            if (!connect()) {
                Message error = new Message();
                error.setSuccess(false);
                error.setMessage("Cannot connect to server");
                return error;
            }
        }
        
        try {
            output.writeObject(request);
            output.flush();
            return (Message) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            connected = false;
            Message error = new Message();
            error.setSuccess(false);
            error.setMessage("Connection error: " + e.getMessage());
            return error;
        }
    }
    
    public void disconnect() {
        try {
            if (output != null) output.close();
            if (input != null) input.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            connected = false;
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}





