import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ProcessorMain {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        Registry r;
        Processor p;
        try{
            r = LocateRegistry.createRegistry(port);
            p = new Processor(port);
            r.rebind("processor",p);
        }catch(RemoteException a){
            a.printStackTrace();
        }catch(Exception e) {
            System.out.println("Registry main " + e.getMessage());
        }

        System.out.println("Processor ready");
    }
}
