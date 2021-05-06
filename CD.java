import java.rmi.*;

public interface CD extends Remote {
    public void cd(String command) throws RemoteException;
}
