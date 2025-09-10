 // Khởi động server, bind đối tượng RMI

package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class LibraryServer {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099); // Khởi tạo RMI registry
            LibraryImpl library = new LibraryImpl();
            Naming.rebind("rmi://localhost:1099/LibraryService", library);
            System.out.println("Library Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
