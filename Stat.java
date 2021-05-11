import java.rmi.*;

public interface Stat extends Remote {
    public String stat(String command) throws RemoteException;
}
