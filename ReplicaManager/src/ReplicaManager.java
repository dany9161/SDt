import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ReplicaManager extends UnicastRemoteObject implements ReplicaManagerInterface {
    static List<String> replicaServerList;

    protected ReplicaManager() throws RemoteException {
        replicaServerList = null;
    }

    @Override
    public void addReplicaServer(String serverAddress) throws RemoteException {
        replicaServerList.add(serverAddress);
    }

    @Override
    public void addPedido(String urlProcessor, Pedido p) {
        //percorrer replicas e adicionar pedido
        ReplicaServerInterface replicaServer;
        for(String url : replicaServerList){
            try {
                replicaServer = (ReplicaServerInterface) Naming.lookup(url);
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            replicaServer.addPedido(url,p);
        }
    }



    @Override
    public void removePedido(UUID p) {
        ReplicaServerInterface replicaServer;
        for(String url : replicaServerList){
            try {
                replicaServer = (ReplicaServerInterface) Naming.lookup(url);
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            replicaServer.removePedido(url,p);
        }
    }

    @Override
    public List<Pedido> getProcessorStatus(String urlProcessor) {
        ReplicaServerInterface replicaServer;
        List<Pedido> res=null;
        for(String url : replicaServerList){
            try {
                replicaServer = (ReplicaServerInterface) Naming.lookup(url);
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            res = replicaServer.getProcessorStatus(url);
            if (res != null) return res;//percorre enquanto não encontrar uma cópia
        }
        return null;
    }
}
