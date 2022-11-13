import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Processor extends UnicastRemoteObject implements ProcessorInterface {
    static HashMap<UUID, Pedido> estadoPedido;

    public Processor() throws RemoteException {
        super();
        estadoPedido = new HashMap();
    }

    @Override
    public UUID submetePedido(String path, UUID ficheiro) throws RemoteException, MalformedURLException, NotBoundException, FileNotFoundException {
        Pedido p = new Pedido(path,ficheiro,UUID.randomUUID());
        estadoPedido.put(p.getPedidoId(),p);

        //vai buscar o ficheiro
            //abre a conecção ao server
        FileManagerInterface server = (FileManagerInterface) Naming.lookup("rmi://localhost:2022/filelist");
        String filePath = server.getFilePath(ficheiro.toString());

        //executa o script
        ProcessBuilder pb = new ProcessBuilder(path);
        Map<String, String> env = pb.environment();
        env.put("file", filePath);
        File resFile = new File(p.getPedidoId()+".txt");
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(resFile));
        try {
            pb.start();


            //envia o ficheiro para o server

            // Creating an OutputStream
            byte [] mydata=new byte[(int) resFile.length()];
            FileInputStream in=new FileInputStream(resFile);


            in.read(mydata, 0, mydata.length);
            in.close();

            server.uploadResFile(mydata,p.getPedidoId()+".txt",(int) resFile.length());
        } catch (IOException e) {
            e.printStackTrace();

        }



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
