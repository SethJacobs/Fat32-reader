import java.rmi.*;

public interface Info extends Remote {
    public void info() throws RemoteException;
}
