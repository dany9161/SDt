import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.UUID;

public class Processor extends UnicastRemoteObject implements ProcessorInterface {
    static HashMap<UUID, Pedido> estadoPedido;

    public Processor() throws RemoteException {
        super();
        estadoPedido = new HashMap();
    }

    @Override
    public UUID submetePedido(String path, UUID ficheiro) throws RemoteException{
        UUID uuid = UUID.randomUUID();
        Pedido p = new Pedido(path,ficheiro);
        estadoPedido.put(uuid,p);
        return uuid;
    }

    @Override
    public char getEstado(UUID idPedido) throws RemoteException{
        if(!estadoPedido.containsKey(idPedido)) {//se o pedido não existir
            System.out.println("O processo " + idPedido + " não existe");
            return 'Z';
        }

        return estadoPedido.get(idPedido).status;
    }
}
