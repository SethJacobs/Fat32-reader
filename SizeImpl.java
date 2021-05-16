import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class SizeImpl extends UnicastRemoteObject implements Size{

    protected SizeImpl() throws RemoteException {
        super();
    }

    @Override
    public String size(String command) throws RemoteException {
        Impl impl = new Impl();
        return impl.size(command);
    }
    
}
