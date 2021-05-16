import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class LSImpl extends UnicastRemoteObject implements LS {

    protected LSImpl() throws RemoteException {
        super();
    }

    @Override
    public String ls(String command, Impl impl) throws RemoteException {
        return impl.ls(command);
    }
    
}
