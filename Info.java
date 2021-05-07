import java.rmi.*;

public interface Info extends Remote {
    public String info() throws RemoteException;
}
