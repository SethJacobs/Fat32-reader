import java.rmi.*;

public interface Init extends Remote {
    public void initiate(String command) throws Exception;
}
