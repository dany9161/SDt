import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public interface ProcessorInterface extends Remote {

    UUID submetePedido(String path, UUID ficheiro) throws RemoteException, MalformedURLException, NotBoundException, FileNotFoundException;
    String getEstado(UUID idPedido) throws RemoteException;

    int getPedidosWaiting();
    void setBackUpUrl(String url);
    void setReplicaUrl(String url);
    void setReplicaUrlExtra(String url);
    void sendExtraReplicaData(String url) throws MalformedURLException, NotBoundException, RemoteException;

    void sendExecuteReplicaData(String url);

    void executeReplicaData(List<ReplicaInfo> _replicaDataExtra);
    void executeReplicaData();

    void setReplicaData(List<ReplicaInfo> replicaDataExtra);

    void addPedidoReplica(UUID uuid,String scriptPath,UUID ficheiro,String urlOrigem);
    void removePedidoReplica(UUID uuid,String urlOrigem);

}
