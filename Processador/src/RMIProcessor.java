import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIProcessor {

    public static void main(String[] args) {

        Registry r = null;
        Processor p;
        try{
            r = LocateRegistry.createRegistry(2024);
        }catch(RemoteException a){
            a.printStackTrace();
        }

        try{
            p = new Processor();
            assert r != null;
            r.rebind("processor",p);

            System.out.println("Processor ready");
        }catch(Exception e) {
            System.out.println("Registry main " + e.getMessage());
        }
    }
}
