import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public interface BalanceadorInterface extends Remote {
    List<Object> submetepedido (String filePath, UUID ficheiro) throws RemoteException, MalformedURLException, NotBoundException;

    String removeProcessor(String url);
    String bestProcessor();
    void addProcessador (String address, int pedidos);
    void receiveProcessorList(List<Processor>processorsList,String controladorUrl);
}
