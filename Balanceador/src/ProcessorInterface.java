import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface ProcessorInterface extends Remote {
    UUID submetePedido(String path, UUID ficheiro) throws RemoteException;
    char getEstado(UUID idPedido) throws RemoteException;
}
