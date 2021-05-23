import java.rmi.*;
import java.rmi.registry.*;

public class Server {
    public static void main(String[] args) throws Exception {
        
        Impl obj = new Impl();
        obj.initiate(args[0]);
        // StatImpl statImpl = new StatImpl();
        // SizeImpl sizeImpl = new SizeImpl();
        // ReadImpl readImpl = new ReadImpl();
        // OpenImpl openImpl = new OpenImpl();
        // CloseImpl closeImpl = new CloseImpl();
        // LSImpl lsImpl = new LSImpl();
        // InfoImpl infoImpl = new InfoImpl();
        // CDImpl cdImpl = new CDImpl();
        // InitImpl initImpl = new InitImpl();
        


        Registry registry = LocateRegistry.getRegistry("192.168.130.3");
        // Registry registry = LocateRegistry.getRegistry(2150);
        // registry.rebind("Stat", statImpl);
        // registry.rebind("Size", sizeImpl);
        // registry.rebind("Read", readImpl);
        // registry.rebind("Open", openImpl);
        // registry.rebind("LS", lsImpl);
        // registry.rebind("Info", infoImpl);
        // registry.rebind("Close", closeImpl);
        // registry.rebind("CD", cdImpl);
        // registry.rebind("Init", initImpl);
        registry.rebind("Fat32Reader", obj);

        System.out.println("Server Started");
    }
}
