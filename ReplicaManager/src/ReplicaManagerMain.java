import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ReplicaManagerMain {
    public static void main(String[] args) {
        Thread t = (new Thread() {
            public void run() {
                ReplicaServerMain.main(new String[]{"2031"});
                ReplicaServerMain.main(new String[]{"2032"});
            }
        });
        t.start();
        try {
            Thread.sleep(1000); // garante que todos os serviços estão disponíveis antes
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Registry r;
        ReplicaManager o;
        try{
            r = LocateRegistry.createRegistry(2030);
            o = new ReplicaManager();
            r.rebind("replicaManager",o);
            System.out.println("Replica Manager ready");
        }catch(RemoteException a){
            a.printStackTrace();
        }catch(Exception e) {
            System.out.println("Registry main " + e.getMessage());
        }
    }
}
