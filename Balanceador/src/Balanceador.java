import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Balanceador extends UnicastRemoteObject implements BalanceadorInterface{
    static ConcurrentHashMap<String, Integer> processadores = new ConcurrentHashMap<>();
    volatile  String controladorUrl=null;

    public Balanceador(String _url) throws IOException {
        super();
        MulticastPublisher mp = new MulticastPublisher();
        mp.sendMulticastMessage("b;"+_url);
    }

    public Balanceador() throws RemoteException {

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
    public String removeProcessor(String url)  {
        processadores.remove(url);
        return getBestProcessador();
    }

    @Override
    public String bestProcessor() {
        return getBestProcessador();
    }

    @Override
    public void addProcessador (String address, int pedidos){
        processadores.put(address,pedidos);
    }

    @Override
    public void receiveProcessorList(List<Processor> processorsList, String controladorUrl) {
        this.controladorUrl = controladorUrl;
        processorsList.forEach(processador -> {
            ProcessorInterface processor=null;
            try {
                processor= (ProcessorInterface) Naming.lookup(processador.url);
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                throw new RuntimeException(e);
            }

            int pedidos = processor.getPedidosWaiting();
            processadores.put(processador.url,pedidos);
        });
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
