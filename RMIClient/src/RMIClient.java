import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;

public class RMIClient {

    public static void main(String[] args) throws IOException {
        //OutputStream obj = new FileOutputStream("myfile23.txt"); abre e cria (se não existir)
        UUID uuid;
        //FileManagerInterface l2;
        String fileName = "myfile.txt";
        try{
            Registry myreg = LocateRegistry.getRegistry("localhost", 2022);
            FileManagerInterface inter = (FileManagerInterface)myreg.lookup("filelist");
            //FileManagerInterface l  = (FileManagerInterface) Naming.lookup("rmi://localhost:2022/filelist");
            File objF = new File(fileName);
            // Creating an OutputStream
            byte [] mydata=new byte[(int) objF.length()];
            FileInputStream in=new FileInputStream(objF);
            System.out.println("uploading to server...");
            in.read(mydata, 0, mydata.length);
            in.close();

            //uuid = l.uploadFileToServer(mydata, fileName,(int) objF.length());
            uuid = inter.uploadFileToServer(mydata, fileName,(int) objF.length());

            if (uuid != null)
                System.out.println(uuid);
            else
                System.out.println("Não inseriu");

        } catch(RemoteException e) {
            System.out.println(e.getMessage());
        }catch(Exception e) {e.printStackTrace();}
    }
}
