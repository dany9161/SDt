import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface FileManagerInterface extends Remote {
    UUID uploadFileToServer(byte[] mybyte, int length) throws RemoteException;
    void uploadResFile(byte[] mybyte, String filename,int length) throws RemoteException;
    String getFilePath(String fileName) throws RemoteException;
    public void voto();

    void commit(int term);
}
