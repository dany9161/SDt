import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.UUID;

public class FileManager extends UnicastRemoteObject implements FileManagerInterface, Serializable {
    private static final long serialVersionUID = 1L;

    private File objF;

    protected FileManager() throws RemoteException {
        objF = new File("./files/");
        //deleteDirectory(objF);
        objF.mkdirs();
        if(objF.exists())
            System.out.println("Pasta pronta");
    }


    @Override
    public UUID uploadFileToServer(byte[] mydata, int length) throws RemoteException {
        UUID uuid;
        uuid= UUID.nameUUIDFromBytes(mydata);

        if (saveFile(mydata,"./files/"+uuid+".txt",length))
            return uuid;
        else
            return null;
        // retornada o id do ficheiro
    }

    @Override
    public void uploadResFile(byte[] mybyte, String filename, int length) throws RemoteException {
        saveFile(mybyte, filename, length);
    } //envia os parametros do save file

    private boolean saveFile(byte[] mydata, String filename,int length){
        try {
            File serverpathfile = new File(filename);
            FileOutputStream out=new FileOutputStream(serverpathfile);

            out.write(mydata);
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    //Guarda o ficheiro

    @Override
    public String getFilePath(String fileName) throws RemoteException {
        File f = new File("./files/"+fileName+".txt");
        return f.getAbsolutePath();
    }
}
