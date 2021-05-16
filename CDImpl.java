import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CDImpl extends UnicastRemoteObject implements CD {

    protected CDImpl() throws RemoteException {
        super();
    }

    @Override
    public String cd(String command) throws RemoteException {
        Impl impl = new Impl();
        return impl.cd(command);
    }
    
}
