import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface ProcessorInterface extends Remote {
    UUID submetePedido(String path, UUID ficheiro) throws RemoteException, MalformedURLException, NotBoundException, FileNotFoundException;
    String getEstado(UUID idPedido) throws RemoteException;
}
