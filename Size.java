import java.rmi.*;

public interface Size extends Remote {
    public String size(String command) throws RemoteException;
}
