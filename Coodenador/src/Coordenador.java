import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Coordenador extends UnicastRemoteObject implements Remote {
    volatile static List<Processor> processorList=null;
    static int indexReplicaExtra = -1;
    static ConcurrentHashMap<Integer,Integer> backUpMap=new ConcurrentHashMap<>();;//server;onde está o backup
    static BalanceadorInterface balanceador = null;

    String url=null;



    public Coordenador(String url) throws RemoteException {
        this();
        this.url = url;
    }

    protected Coordenador() throws RemoteException {
        Registry storage = LocateRegistry.getRegistry("localhost", 2022);
        try {
            balanceador = (BalanceadorInterface)storage.lookup("filelist");
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }

        //rotina para mandar heartbeats para os processadores
        Runnable proccessorsHeartbeats = new Runnable() {

            public void run() {
                MulticastPublisher mp = new MulticastPublisher();
                try {
                    mp.sendMulticastMessage("h;"+processorList.size());
                } catch (
                        IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        ScheduledExecutorService executorProcessorHeartbeats = Executors.newScheduledThreadPool(1);
        executorProcessorHeartbeats.scheduleAtFixedRate(proccessorsHeartbeats, 0, 15, TimeUnit.SECONDS);

        //rotina para saber saber se há processadores com falhas
        Runnable checkProccessors = new Runnable() {
            public void run() {
                //percorrer a lista de processadores
                Iterator<Processor> iterator = processorList.iterator();
                int i = 0;
                //simple iteration
                while(iterator.hasNext()){

                    Processor p = (Processor) iterator.next();
                    //saber se algum falhou
                    if (p.hasFailed()){
                        //tirar da lista e do balanceador
                        if (processorList.size()==1){
                            System.out.println("O unico processador falhou");
                            System.exit(1);
                            //Throw an error
                        }

                        recuperarPedidos(p.url,i);
                        //reatribuirPlanoBackup();

                        //

                        int replica = backUpMap.get(i);

                        int listSize = processorList.size();

                        if(indexReplicaExtra != -1){//existe replica extra
                            if (processorList.size() == i){//verifica se o P que falhou foi o replica extra do 1º Processador

                                backUpMap.remove(i);
                            }else{
                                try {
                                    setReplicas(replica,indexReplicaExtra);
                                } catch (MalformedURLException | RemoteException | NotBoundException e) {
                                    throw new RuntimeException(e);
                                }

                            }
                        }else{ //replica extra não existe
                            try {
                                setReplicaExtra(replica);
                            } catch (MalformedURLException | NotBoundException | RemoteException e) {
                                throw new RuntimeException(e);
                            }
                            backUpMap.remove(i);
                        }

                        if(i==1) backUpMap.remove(0);//se for o 1º P, perde-se a replica extra

                        processorList.removeIf(Processor::hasFailed);//remove da lista o processador com o url
                    }
                    i++;
                }
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(checkProccessors, 0, 15, TimeUnit.SECONDS);


    }

    private void setReplicas(int processor1Index, int processor2Index) throws MalformedURLException, NotBoundException, RemoteException {
        //"Criar" a "ligação" aos processadores
        String processor1Url = processorList.get(processor1Index).url;
        ProcessorInterface processor1 = (ProcessorInterface) Naming.lookup(processor1Url);

        String processor2Url = processorList.get(processor2Index).url;
        ProcessorInterface processor2 = (ProcessorInterface) Naming.lookup(processor2Url);

        //informar o (n-1)ºP que o seu backUp mudou, dizer qual é ([rmi] penultimo.setBackUpUrl(string url))
        processor1.setBackUpUrl(processor2Url);
        backUpMap.put(processor1Index,processor2Index);


        //o (n)º P adicionado, fica replica (n-1)ºP ([rmi] ultimo.setReplica(String url))
        processor2.setBackUpUrl(processor1Url);

        backUpMap.put(processor2Index,processor1Index);

    }

    private void setReplicaExtra(int processorIndex) throws MalformedURLException, NotBoundException, RemoteException {
        //"Criar" a "ligação" aos processadores
        String firstProcessorUrl = processorList.get(0).url;
        ProcessorInterface firstProcessor = (ProcessorInterface) Naming.lookup(firstProcessorUrl);

        String lastProcessorUrl = processorList.get(processorIndex).url;
        ProcessorInterface processor = (ProcessorInterface) Naming.lookup(firstProcessorUrl);

        //o 1ª P guarda a replica do nºP ([rmi] 1ºP.setReplicaSuplente(string urlNewP))
        backUpMap.put(processorIndex,0);

        firstProcessor.setReplicaUrlExtra(lastProcessorUrl);

        //informar o nº processador da replica ([rmi] ultimo.setReplica(string url))
        processor.setBackUpUrl(firstProcessorUrl);
    }

    private void recuperarPedidos(String url, int i) {
        String bestProcessor = balanceador.removeProcessor(url);
        String backUpProcessor = processorList.get(backUpMap.get(i)).url;
        ProcessorInterface replica = null;

        try {
            replica = (ProcessorInterface) Naming.lookup(bestProcessor);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            throw new RuntimeException(e);
        }


        //Redistribuir processos
        if(bestProcessor == backUpProcessor){//se o melhor processdor, for o mesmo onde está o backup
            //executar os pedidos
            replica.executeReplicaData();
        }else {
            //dizer para onde tem de enviar
            replica.sendExecuteReplicaData(backUpProcessor);
        }
    }

    public void updateProcessor(String address){
        for (Processor p : processorList){
            if(p.getUrl() == address)
                p.updateLastHeartBeat();
        }
    }

    public void addProcessor(String address) {
        processorList.add(new Processor(address));
        try {
            setReplica();
        } catch (MalformedURLException | NotBoundException | RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private void setReplica() throws MalformedURLException, NotBoundException, RemoteException {

        int listSize = processorList.size();
        if(listSize == 1){
            backUpMap.put(0,0);
        } else if (listSize == 2) {
            setReplicas(0,1);
        } else if (indexReplicaExtra == -1) { //(obrigatorio + extra)
            setReplicaExtra(listSize);
            indexReplicaExtra = listSize; //-1 é a posição da replica extra
        } else { //(obrigatorio)
            setReplicas(listSize-2,listSize-1);//=setReplicas(indexReplicaExtra,listSize-1);
            indexReplicaExtra = -1;
        }
    }

    public void sendProcessorList(String part) {

        try {
            balanceador = (BalanceadorInterface) Naming.lookup(part);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            throw new RuntimeException(e);
        }
        balanceador.receiveProcessorList(processorList,url);
    }
}
