import java.rmi.*;
import java.rmi.registry.*;

public class Server {
    public static void main(String[] args) throws Exception {
        
        Impl obj = new Impl();
        Registry registry = LocateRegistry.getRegistry("192.168.130.3");
        // Registry registry = LocateRegistry.getRegistry(2150);
        registry.rebind("Fat32Reader", obj);

        System.out.println("Server Started");
    }
}
