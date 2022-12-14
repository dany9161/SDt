import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClientMain {

    public static void main(String[] args) throws IOException {
        UUID fileUuid;
        String fileName = "myfile.txt";

        Thread t = (new Thread() {
            public void run() {
                try {
                    //RMIProcessor.main(new String[]{"2024"});
                    ProcessorMain.main(new String[]{"2025"});
                    ProcessorMain.main(new String[]{"2026"});
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                //RMIBalanceador.main(new String[0]);
                RMIServer.main(new String[]{"2022"});
            }
        });
        t.start();
        try {
            Thread.sleep(1000); // garante que todos os serviços estão disponíveis antes
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try{
            //Sprint 1
            Registry storage = LocateRegistry.getRegistry("localhost", 2022);
            FileManagerInterface fileServer = (FileManagerInterface)storage.lookup("filelist");

            File objF = new File(fileName);
            // Creating an OutputStream
            byte [] mydata=new byte[(int) objF.length()];
            FileInputStream in=new FileInputStream(objF);

            in.read(mydata, 0, mydata.length);
            in.close();

            System.out.println("uploading to server...");
            fileUuid = fileServer.uploadFileToServer(mydata, (int) objF.length());

            if (fileUuid != null)
                System.out.println("File UUID: "+fileUuid);
            else
                System.out.println("Não inseriu");

            //Sprint 2
            String urlBalanceador2023 = "rmi://localhost:2023/balanceador";
            BalanceadorInterface balanceador2023  = (BalanceadorInterface) Naming.lookup(urlBalanceador2023);
            System.out.println("Encontrei o balanceador");

            //submete pedido para o BALANCEADOR
            //recebe o uuid do pedido e url do PROCESSADOR
            File script = new File("script.bat");
            String urlProcessador=null;
            UUID pedidoId=null;

            for (int i =0;i<10;i++) {
                List<Object> dados = balanceador2023.submetepedido(script.getAbsolutePath(), fileUuid);
                urlProcessador = (String) dados.get(0);
                pedidoId = (UUID) dados.get(1);

            }
            System.out.println("Consegui por os pedidos");

            //CLIENTE pergunta pelo pedido ao PROCESSADOR
            //CLIENTE recebe o estado do pedido
            ProcessorInterface processor = (ProcessorInterface) Naming.lookup(urlProcessador);
            System.out.println("Encontrei o processador");
            String status = processor.getEstado(pedidoId);
            while(!Objects.equals(status, "Done")) {
                System.out.println("Estado do pedido " + pedidoId + " no servidor " + urlProcessador + " é " + status);
                status = processor.getEstado(pedidoId);
                Thread.sleep(1000);
            }
            System.out.println("Estado do pedido " + pedidoId + " no servidor " + urlProcessador + " é " + status);

        } catch(RemoteException e) {
            System.out.println(e.getMessage());
        }catch(Exception e) {e.printStackTrace();}


    }
}
