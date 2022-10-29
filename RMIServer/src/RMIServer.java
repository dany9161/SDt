import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
    public static void main(String[] args) {

        Registry r = null;
        FileManager fileList;
        try{
            r = LocateRegistry.createRegistry(2022);
        }catch(RemoteException a){
            a.printStackTrace();
        }

        try{
            fileList = new FileManager();
            r.rebind("filelist", fileList );

            System.out.println("Place server ready");
        }catch(Exception e) {
            System.out.println("Place server main " + e.getMessage());
        }
    }
}
