import java.rmi.*;

public interface Stat extends Remote {
    public void stat(String command) throws RemoteException;
}
