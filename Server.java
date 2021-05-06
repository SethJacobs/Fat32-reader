import java.rmi.*;
import java.rmi.server.*;

public class Server {
    public static void main(String[] args) throws Exception {
        
        Impl obj = new Impl();

        Naming.rebind("Shell", obj);

        System.out.println("Server Started");
    }
}
