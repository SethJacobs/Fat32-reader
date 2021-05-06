import java.rmi.*;

public interface Close extends Remote {
    public void close(String command) throws RemoteException;
}
