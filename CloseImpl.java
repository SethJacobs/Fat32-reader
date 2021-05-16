import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CloseImpl extends UnicastRemoteObject implements Close{

    protected CloseImpl() throws RemoteException {
        super();
    }

    @Override
    public String close(String command) throws RemoteException {
        Impl impl = new Impl();
        return impl.close(command);
    }
    
}
