import java.rmi.*;

public interface CD extends Remote {
    public String cd(String command) throws RemoteException;
}
