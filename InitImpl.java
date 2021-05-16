import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class InitImpl extends UnicastRemoteObject implements Init{

    protected InitImpl() throws RemoteException {
        super();
    }

    @Override
    public void initiate(String command, Impl impl) throws IOException {
        impl.initiate(command);
    }
    
}
