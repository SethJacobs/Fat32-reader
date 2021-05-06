import java.rmi.*;

public interface Open extends Remote {
    public void open(String command) throws RemoteException;
}
