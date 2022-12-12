import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Processor extends UnicastRemoteObject implements ProcessorInterface {
    static HashMap<UUID, Pedido> estadoPedido;

    public Processor(int _port) throws RemoteException {
        super();
        estadoPedido = new HashMap();

        Runnable sendHeartbeatRunnable = new Runnable() {
            public void run() {
                MulticastPublisher mp = new MulticastPublisher();
                try {
                    mp.multicast("rmi://localhost:"+_port+"/processor;"+getPedidosWaiting());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(sendHeartbeatRunnable, 0, 3, TimeUnit.SECONDS);
    }

    private int getPedidosWaiting() {
        return (int) estadoPedido.entrySet().stream().filter(Pedido -> Pedido.getValue().getStatus().contains("Waiting")).count();
    }

    @Override
    public UUID submetePedido(String path, UUID ficheiro) throws RemoteException, MalformedURLException, NotBoundException, FileNotFoundException {

        Pedido p = new Pedido(path,ficheiro,UUID.randomUUID());
        estadoPedido.put(p.getPedidoId(),p);
        p.start();
        return p.getPedidoId();
    }

    @Override
    public String getEstado(UUID idPedido) throws RemoteException{
        if(!estadoPedido.containsKey(idPedido)) {//se o pedido não existir
            System.out.println("O processo " + idPedido + " não existe");
            return "Z";
        }

        return estadoPedido.get(idPedido).getStatus();
    }
}