import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class Coordenador extends UnicastRemoteObject implements Remote {
    static List<Processor> processorList;
    static BalanceadorInterface balanceador;
    // Creating a Predicate condition checking for 10
    public static Predicate<Processor> isP(String url) {
        return i -> (i.getUrl() == url);
    }
    protected Coordenador() throws RemoteException {
        processorList = null;
        Registry storage = LocateRegistry.getRegistry("localhost", 2022);
        try {
            balanceador = (BalanceadorInterface)storage.lookup("filelist");
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }

        //rotina para saber saber se há processadores com falhas
        Runnable checkProccessors = new Runnable() {
            public void run() {
                //percorrer a lista de processadores
                Iterator<Processor> iterator = processorList.iterator();

                //simple iteration
                while(iterator.hasNext()){
                    Processor p = (Processor) iterator.next();
                    //saber se algum falhou
                    if (p.hasFailed()){
                        //tirar da lista e do balanceador
                        balanceador.removeProcessor(p.url);
                        processorList.removeIf(Processor::hasFailed);//remove da lista o processador com o url
                    }
                }
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(checkProccessors, 0, 15, TimeUnit.SECONDS);
    }

    public void updateProcessor(String address){
        for (Processor p : processorList){
            if(p.getUrl() == address)
                p.updateLastHeartBeat();
        }
    }

    public void addProcessor(String address) {
        processorList.add(new Processor(address));
    }
}
