import java.rmi.*;

public interface Close extends Remote {
    public String close(String command) throws RemoteException;
}
