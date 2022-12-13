import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {
    public static void main(String[] args) {
        Registry r;
        Coordenador c;

        try{
            r = LocateRegistry.createRegistry(2021);
            c = new Coordenador();
            r.rebind("coordenador",c);
        }catch(RemoteException a){
            a.printStackTrace();
        }catch(Exception e) {
            System.out.println("Registry main " + e.getMessage());
        }
    }
}
