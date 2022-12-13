import java.rmi.RemoteException;

public interface ReplicaManagerInterface {
    //adiciona um server de replica ao registo do manager
    void addReplicaServer(String serverAddress) throws RemoteException;

    void addPedido(String urlProcessor,Pedido p);
}
