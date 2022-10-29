import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface FileManagerInterface extends Remote {
    UUID uploadFileToServer(byte[] mybyte, String fileName, int length) throws RemoteException;
}
