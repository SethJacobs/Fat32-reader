import java.rmi.*;

public interface Read extends Remote {
    public String read(String command, int start, int end) throws RemoteException;
}
