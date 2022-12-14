import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.UUID;

public class Pedido extends Thread{
    private String status;
    private String scriptPath;
    private UUID fileUUID;
    private UUID pedidoId;
    String storageLeaderUrl;

    private String backUpUrl; // endereço do processador que tem a replica do processador responsavel por este pedido
    private String processorUrl; // endereço do processador responsavel
    public Pedido(String _scriptPath, UUID _fileUUID,UUID _pedidoId,String _backUpUrl, String _processorUrl) throws MalformedURLException, NotBoundException, RemoteException {
        this(_scriptPath,  _fileUUID, _pedidoId, _backUpUrl, _processorUrl,"rmi://localhost:2022/filelist");
    }

    public Pedido(String _scriptPath, UUID _fileUUID,UUID _pedidoId,String _backUpUrl, String _processorUrl, String _storageLeaderUrl) throws MalformedURLException, NotBoundException, RemoteException {
        scriptPath = _scriptPath;
        fileUUID = _fileUUID;
        pedidoId=_pedidoId;
        //Quando um processo é criado, fica em pending
        status="Waiting";
        backUpUrl = _backUpUrl;
        processorUrl=_processorUrl;
        storageLeaderUrl = _storageLeaderUrl;
        ProcessorInterface backUpProcessor = (ProcessorInterface) Naming.lookup(backUpUrl);
        backUpProcessor.addPedidoReplica(pedidoId,scriptPath,fileUUID,processorUrl);
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public UUID getFileUUID() {
        return fileUUID;
    }

    public void setStatusRunning(){
        this.status = "Running";
    }
    public void setStatusSaving(){
        this.status = "Saving";
    }
    public void setStatusDone(){
        this.status = "Done";
    }

    public String getStatus() {return status;}

    public UUID getPedidoId() {return pedidoId;}

    @Override
    public void run() {
        //vai buscar o ficheiro
        //abre a conecção ao server
        FileManagerInterface server = null;
        String filePath;
        ProcessorInterface backUpProcessor;
        try {
            /*
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/

            backUpProcessor = (ProcessorInterface) Naming.lookup(backUpUrl);

            server = (FileManagerInterface) Naming.lookup(storageLeaderUrl);
            filePath = server.getFilePath(this.fileUUID.toString());

            //executa o script
            ProcessBuilder pb = new ProcessBuilder(this.scriptPath);
            Map<String, String> env = pb.environment();
            env.put("file", filePath);
            File resFile = new File(this.getPedidoId()+".txt");
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(resFile));

            pb.start();

            setStatusRunning(); // estadomudar
            //envia o ficheiro para o server
            setStatusSaving();

            //envia o ficheiro para o server
            // Creating an OutputStream
            byte [] mydata=new byte[(int) resFile.length()];
            FileInputStream in=new FileInputStream(resFile);

            in.read(mydata, 0, mydata.length);
            in.close();

            server.uploadResFile(mydata,getPedidoId()+".txt",(int) resFile.length());
            setStatusDone();
            backUpProcessor.removePedidoReplica(pedidoId,processorUrl);

        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}