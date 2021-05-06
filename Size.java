import java.rmi.*;

public interface Size extends Remote {
    public void stat(String command) throws RemoteException;
}
