import java.rmi.registry.*;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) throws Exception {
        
        Impl obj = new Impl();
        obj.initiate(args[0]);
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter the connecting IP: ");
        String IP = sc.nextLine();
        System.out.println("Please enter the connecting Port Number: ");
        int port = Integer.parseInt(sc.nextLine());
        Registry registry = LocateRegistry.getRegistry(IP, port);
        registry.rebind("Fat32Reader", obj);

        System.out.println("Server Started");
    }
}
