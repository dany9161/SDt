import java.rmi.Remote;
import java.util.List;
import java.util.UUID;

public interface ReplicaServerInterface extends Remote {
    void addPedido(String url, Pedido p);
    void removePedido(String url, UUID uuid);

    void updatePedidoStatus(String url, UUID uuid, String status);

    List<Pedido> getProcessorStatus(String url);
}
