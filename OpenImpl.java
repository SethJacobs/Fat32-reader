import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class OpenImpl extends UnicastRemoteObject implements Open{

    protected OpenImpl() throws RemoteException {
        super();
    }

    @Override
    public String open(String command) throws RemoteException{
        Impl impl = new Impl();
        return impl.open(command);
        
    }
}
