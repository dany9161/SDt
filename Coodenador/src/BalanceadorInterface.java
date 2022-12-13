import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public interface BalanceadorInterface extends Remote {
    List<Object> submetepedido (String filePath, UUID ficheiro) throws RemoteException, MalformedURLException, NotBoundException;

    void removeProcessor(String url);
}
