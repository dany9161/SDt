import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public interface ReplicaManagerInterface {
    //adiciona um server de replica ao registo do manager
    void addReplicaServer(String serverAddress) throws RemoteException;

    void addPedido(String urlProcessor,Pedido p);

    void removePedido(UUID p);

    List<Pedido> getProcessorStatus(String urlProcessor);
}
