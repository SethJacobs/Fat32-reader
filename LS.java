import java.rmi.*;

public interface LS extends Remote {
    public void ls(String command) throws RemoteException;
}
