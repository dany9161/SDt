import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIProcessor {

    public static void main(String[] args) {

        Registry r;
        Processor p;
        try{
            r = LocateRegistry.createRegistry(2024);
            p = new Processor();
            r.rebind("processor",p);
        }catch(RemoteException a){
            a.printStackTrace();
        }catch(Exception e) {
            System.out.println("Registry main " + e.getMessage());
        }

        System.out.println("Processor ready");
    }
}
