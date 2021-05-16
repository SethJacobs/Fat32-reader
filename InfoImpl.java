import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class InfoImpl extends UnicastRemoteObject implements Info {

    protected InfoImpl() throws RemoteException {
        super();
    }

    @Override
    public String info() throws RemoteException {
        Impl impl = new Impl();
        return impl.info();
    }
    
}
