import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class StatImpl extends UnicastRemoteObject implements Stat{

    protected StatImpl() throws RemoteException {
        super();
    }

    @Override
    public String stat(String command) throws RemoteException {
        Impl impl = new Impl();
        return impl.stat(command);
    }
    
}
