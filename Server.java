import java.rmi.*;

public class Server {
    public static void main(String[] args) throws Exception {
        
        Impl obj = new Impl();

        Naming.rebind("rmi://localhost:2150/Fat32Reader", obj);

        System.out.println("Server Started");
    }
}
