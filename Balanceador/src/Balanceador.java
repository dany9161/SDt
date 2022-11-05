import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Balanceador extends UnicastRemoteObject implements BalanceadorInterface{
    static HashMap<Integer,Processador> processadores;

    public Balanceador() throws RemoteException {
        super();
        processadores = new HashMap<>();
    }

    @Override
    public List<Object> submetepedido(String filePath, UUID ficheiro) throws MalformedURLException, NotBoundException, RemoteException {
        int processador = getBestProcessador();
        String pUrl = processadores.get(processador).url;
        ProcessorInterface processor2024  = (ProcessorInterface) Naming.lookup(pUrl);
        //submete e recebe o uuid do pedido
        UUID pedidoId = processor2024.submetePedido(filePath,ficheiro);

        return Arrays.asList(processadores.get(processador).url, pedidoId);
    }

    public void addProcessador (int port, Processador p){
        processadores.put(port,p);
    }

    //encontrar o processador com mais recursos
    public int getBestProcessador(){
        //algoritmo para achar processador com mais recursos
        return 2024;
    }
}
