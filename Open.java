import java.rmi.*;

public interface Open extends Remote {
    public String open(String command) throws RemoteException;
}
