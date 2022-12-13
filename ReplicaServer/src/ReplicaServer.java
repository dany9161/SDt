import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class ReplicaServer extends UnicastRemoteObject implements ReplicaServerInterface {

    HashMap<String, List<Pedido>> serverList;//talvez mudar para Lista de pedidos, e cada pedido guarda o seu processador

    protected ReplicaServer(int port) throws RemoteException{
        serverList = null;

        try {
            ReplicaManagerInterface r = (ReplicaManagerInterface) Naming.lookup("rmi://localhost:2030/replicaManager");
            r.addReplicaServer("rmi://localhost:"+port+"/replicaServer");
        } catch (NotBoundException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void addPedido(String url, Pedido p) {
        serverList.get(url).add(p);
    }

    @Override
    public void removePedido(String url, UUID uuid) {
        //remove (todos) pedido com o uuid
        for (HashMap.Entry<String, List<Pedido>> entry : serverList.entrySet()) {
            entry.getValue().removeIf(pedido -> pedido.getPedidoId() == uuid);
        }
    }

    @Override
    public void updatePedidoStatus(String url, UUID uuid, String status) {

        for(Pedido p : serverList.get(url)){
            if(p.getPedidoId() == uuid){
                switch (status) {
                    case "Done" -> p.setStatusDone();
                    case "Saving" -> p.setStatusSaving();
                    case "Running" -> p.setStatusRunning();
                }
            }
        }
    }

    @Override
    public List<Pedido> getProcessorStatus(String url) {
        List<Pedido> list = serverList.get(url);
        serverList.remove(url);
        return list;
    }
}
