
import java.rmi.*;

public interface CurrentDir extends Remote {
    public String getCurrentDir() throws Exception;
}
