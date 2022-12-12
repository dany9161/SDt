import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
    public static void main(String[] args) {
//int port = Integer.parseInt(args[0]);
        Registry r;
        FileManager fileList;
        try{
            r = LocateRegistry.createRegistry(2022);
            fileList = new FileManager();
            r.rebind("filelist", fileList);
        }catch(RemoteException a){
            a.printStackTrace();
        }catch(Exception e) {
            System.out.println("Place server main " + e.getMessage());
        }
        System.out.println("Place server ready");
    }
}
