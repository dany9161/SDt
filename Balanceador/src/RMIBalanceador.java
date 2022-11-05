import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIBalanceador {

    public static void main(String[] args) {

        Registry r = null;
        Balanceador b = null;

        try{
            r = LocateRegistry.createRegistry(2023);
            b = new Balanceador();
            b.addProcessador(2024,new Processador("rmi://localhost:2024/processor"));
        }catch(RemoteException a){
            a.printStackTrace();
        }

        try{
            assert r != null;
            r.rebind("balanceador",b);

            System.out.println("Balanceador ready");
        }catch(Exception e) {
            System.out.println("Registry main " + e.getMessage());
        }
    }
}
