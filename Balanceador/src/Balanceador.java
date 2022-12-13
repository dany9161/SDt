import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Balanceador extends UnicastRemoteObject implements BalanceadorInterface{
    static HashMap<String, Integer> processadores;

    public Balanceador() throws RemoteException {
        super();
        processadores = new HashMap<>();
    }

    @Override
    public List<Object> submetepedido(String filePath, UUID ficheiro) throws MalformedURLException, NotBoundException, RemoteException {
        String processador = getBestProcessador();
        //String pUrl = processadores.get(processador);
        ProcessorInterface processor = (ProcessorInterface) Naming.lookup(processador);
        //submete e recebe o uuid do pedido
        UUID pedidoId = null;
        try {
            pedidoId = processor.submetePedido(filePath,ficheiro);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return Arrays.asList(processador, pedidoId);
    }

    @Override
    public void removeProcessor(String url)  {
        processadores.remove(url);
        //passar os pedidos para o processador com mais recursos
        ReplicaManagerInterface r = null;
        try {
            r = (ReplicaManagerInterface) Naming.lookup("rmi://localhost:2030/replicaManager");

            List<Pedido> list = r.getProcessorStatus(url);
            for (Pedido p : list) {
                submetepedido(p.getScriptPath(), p.getFileUUID());
            }
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

@Override
    public void addProcessador (String address, int pedidos){
        processadores.put(address,pedidos);
    }

    public void updateProcessor(String address, int pedidos){addProcessador (address,pedidos);}
    //encontrar o processador com mais recursos
    //devolver o endereço
    public String getBestProcessador(){
        //devolve o endereço do processador que tem menos pedidos em espera
        Map.Entry<String, Integer> min = Collections.min(processadores.entrySet(),
                Comparator.comparing(Map.Entry::getValue));
        return min.getKey();
    }
}
