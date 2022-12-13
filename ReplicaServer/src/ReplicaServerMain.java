import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ReplicaServerMain {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        Registry r;
        ReplicaServer o;
        try{
            r = LocateRegistry.createRegistry(port);
            o = new ReplicaServer(port);
            r.rebind("replicaManager",o);
            System.out.println("Replica Manager ready");
        }catch(RemoteException a){
            a.printStackTrace();
        }catch(Exception e) {
            System.out.println("Registry main " + e.getMessage());
        }
    }
}