import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ReadImpl extends UnicastRemoteObject implements Read{

    protected ReadImpl() throws RemoteException {
        super();
    }

    @Override
    public String read(String command, int start, int end) throws RemoteException {
        Impl impl = new Impl();
        return impl.read(command, start, end);
    }
    
}
