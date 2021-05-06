import java.rmi.*;

public interface Read extends Remote {
    public void read(String command, int start, int end) throws RemoteException;
}
