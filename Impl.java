import java.rmi.*;
import java.rmi.server.*;

public class Impl extends UnicastRemoteObject implements Info, LS, Stat, CD, Open, Close, Read, Size {

    protected Impl() throws RemoteException {
        super();
    }

    @Override
    public void read(String command, int start, int end) throws RemoteException {
        
    }

    @Override
    public void close(String command) throws RemoteException {
        
    }

    @Override
    public void open(String command) throws RemoteException {
        
    }

    @Override
    public void cd(String command) throws RemoteException {
        
    }

    @Override
    public void stat(String command) throws RemoteException {
        
    }

    @Override
    public void ls(String command) throws RemoteException {
        
    }

    @Override
    public void info() throws RemoteException {
        
    }

    
    
}
