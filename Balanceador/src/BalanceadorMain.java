import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BalanceadorMain {

    public static void main(String[] args) {
        Registry r;
        Balanceador b;

        try{
            r = LocateRegistry.createRegistry(2023);
            b = new Balanceador();
            r.rebind("balanceador",b);
        }catch(RemoteException a){
            a.printStackTrace();
        }catch(Exception e) {
            System.out.println("Registry main " + e.getMessage());
        }
        MulticastReceiver mr = new MulticastReceiver();
        mr.start();

        System.out.println("Balanceador ready");


    }
}
