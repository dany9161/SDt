import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIBalanceador {

    public static void main(String[] args) {
        Registry r;
        Balanceador b;

        try{
            r = LocateRegistry.createRegistry(2023);
            b = new Balanceador();
            b.addProcessador(2024,new Processador("rmi://localhost:2024/processor"));
            r.rebind("balanceador",b);
        }catch(RemoteException a){
            a.printStackTrace();
        }catch(Exception e) {
            System.out.println("Registry main " + e.getMessage());
        }
        System.out.println("Balanceador ready");
    }
}
