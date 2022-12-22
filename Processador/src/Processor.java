import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.*;

public class Processor extends UnicastRemoteObject implements ProcessorInterface {
    static HashMap<UUID, Pedido> estadoPedido;
    String replicaUrl = null; // endereço do processador a que pertence os dados
    List<ReplicaInfo> replicaData = null;
    String replicaUrlExtra = null; // endereço do processador a que pertence os dados
    List<ReplicaInfo> replicaDataExtra = null;
    String backUp = null; //endereço do processador onde está o backup deste processador
    int port;
    volatile String storageLeaderUrl = null;

    String url;//url do proprio
    volatile Boolean cActive = false;// guarda se o coordenador está ativo
    volatile int nPAtivos  = 0;//numero de processadore ativos na rede, atualizado em cada heartbeat que o coordenador envia
    volatile int nPCDead = 0;//numero de processadores que consideram o coordenador desativado
    volatile Time lastCHeartBeat;//quando foi o ultimo heartbeat recebido do controlador


    public Processor(int _port) throws IOException {
        super();

        port=_port;
        estadoPedido = new HashMap();
        MulticastPublisher mp = new MulticastPublisher();
        mp.multicastProcessHeartbeat("setup;rmi://localhost:"+_port+"/processor");
        url = "localhost:"+port+"/processor";

        Runnable sendHeartbeatRunnable = new Runnable() {
            public void run() {
                MulticastPublisher mp = new MulticastPublisher();
                try {
                    mp.multicastProcessHeartbeat("update;rmi://localhost:"+_port+"/processor;"+getPedidosWaiting());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(sendHeartbeatRunnable, 0, 3, TimeUnit.SECONDS);


        Runnable checkCoordenatorRunnable = new Runnable() {
            public void run() {
                if ((System.currentTimeMillis()- lastCHeartBeat.getTime()) > 30000 && cActive){//se o ultimo heartbeat recebido do coordenador tiver mais do que 30"
                    //enviar a mensagem que o controlador morreu
                    MulticastPublisher mp = new MulticastPublisher();
                    try {
                        mp.sendControlerDeadMessage("m;"+url);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    avaliaCMorreu();
                }
            }
        };

        ScheduledExecutorService checkCoordenatorExecutor = Executors.newScheduledThreadPool(1);
        checkCoordenatorExecutor.scheduleAtFixedRate(checkCoordenatorRunnable, 0, 3, TimeUnit.SECONDS);
    }



    public Processor() throws RemoteException {
        super();
    }

    void avaliaCMorreu(){
        nPCDead++;
        if (nPCDead >= (nPAtivos/2)){
            cActive = false;
            System.out.println("Controlador falhou");
        }
    }


    @Override
    public int getPedidosWaiting() {
        return (int) estadoPedido.entrySet().stream().filter(Pedido -> Pedido.getValue().getStatus().contains("Waiting")).count();
    }

    @Override
    public UUID submetePedido(String path, UUID ficheiro) throws RemoteException, MalformedURLException, NotBoundException {
        Pedido p;
        if(storageLeaderUrl == null) {
             p= new Pedido(path, ficheiro, UUID.randomUUID(), backUp, url);
        }else{
            p = new Pedido(path, ficheiro, UUID.randomUUID(), backUp, url,storageLeaderUrl);
        }
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

    @Override
    public void setBackUpUrl(String url) {
        backUp = url;
    }

    @Override
    public void setReplicaUrl(String url) {
        replicaUrl = url;
    }

    @Override
    public void setReplicaUrlExtra(String url) {
        replicaUrlExtra = url;
    }

    @Override
    public void sendExtraReplicaData(String url) throws MalformedURLException, NotBoundException, RemoteException {
        ProcessorInterface newReplica = (ProcessorInterface) Naming.lookup(url);
        newReplica.setReplicaData(replicaDataExtra);
    }

    @Override
    public void sendExecuteReplicaData(String url) {
        ProcessorInterface processor = null;
        try {
            processor = (ProcessorInterface) Naming.lookup(url);
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            throw new RuntimeException(e);
        }

        processor.executeReplicaData(replicaDataExtra);
    }

    @Override
    public void executeReplicaData(List<ReplicaInfo> _replicaDataExtra) {
        for (ReplicaInfo ri : _replicaDataExtra){
            Pedido p = null;
            try {
                p = new Pedido(ri.scriptPath,ri.fileId, UUID.randomUUID(),backUp,url);
            } catch (MalformedURLException | NotBoundException | RemoteException e) {
                throw new RuntimeException(e);
            }
            estadoPedido.put(p.getPedidoId(),p);
            p.start();
        }
    }

    @Override
    public void executeReplicaData() {
        for (ReplicaInfo ri : replicaDataExtra){
            Pedido p = null;
            try {
                p = new Pedido(ri.scriptPath,ri.fileId, UUID.randomUUID(),backUp,url);
            } catch (MalformedURLException | NotBoundException | RemoteException e) {
                throw new RuntimeException(e);
            }
            estadoPedido.put(p.getPedidoId(),p);
            p.start();
        }
    }

    @Override
    public void setReplicaData(List<ReplicaInfo> newReplicaData) {
        replicaData=newReplicaData;
    }

    @Override
    public void addPedidoReplica(UUID uuid, String scriptPath, UUID ficheiro,String urlOrigem) {
        ReplicaInfo ri = new ReplicaInfo(scriptPath,uuid,ficheiro);
        if (Objects.equals(urlOrigem, replicaUrl))//se for o mesmo endereço que a replica, não é a extra
            replicaData.add(ri);
        else
            replicaDataExtra.add(ri);
    }

    @Override
    public void removePedidoReplica(UUID uuid, String urlOrigem) {
        if (Objects.equals(urlOrigem, replicaUrl))
            replicaData.removeIf(ri -> ri.idPedido == uuid);
        else
            replicaDataExtra.removeIf(ri -> ri.idPedido == uuid);
    }


    public void refreshControlador(int part) {
        if (!cActive) System.out.println("O controlador voltou");
        cActive=true;
        lastCHeartBeat=new Time(System.currentTimeMillis());
        nPAtivos = part;
        nPCDead =0;
    }

    public void setStorageLeader(String part) {
        storageLeaderUrl = part;
    }
}