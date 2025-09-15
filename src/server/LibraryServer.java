package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class LibraryServer {
    public static void main(String[] args) {
        try {
            // Start registry programmatically on 1099
            LocateRegistry.createRegistry(1099);
            LibraryImpl impl = new LibraryImpl();
            Naming.rebind("rmi://localhost:1099/LibraryService", impl);
            System.out.println("Library RMI Server is running on rmi://localhost:1099/LibraryService");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
