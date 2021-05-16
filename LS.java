import java.rmi.*;

public interface LS extends Remote {
    public String ls(String command, Impl impl) throws RemoteException;
}
