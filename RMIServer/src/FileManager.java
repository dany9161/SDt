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
    private static HashMap<UUID,String> filesTable;

    private File objF;

    static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    protected FileManager() throws RemoteException {
        filesTable=new HashMap<>();
        objF = new File("./files/");
        deleteDirectory(objF);
        objF.mkdirs();
        if(objF.exists())
            System.out.println("Pasta pronta");
    }


    @Override
    public UUID uploadFileToServer(byte[] mydata, String fileName , int length) throws RemoteException {
        UUID uuid;
        try {
            File serverpathfile = new File("./files/"+fileName);
            FileOutputStream out=new FileOutputStream(serverpathfile);
            byte [] data=mydata;

            out.write(data);
            out.flush();
            out.close();

             uuid= UUID.nameUUIDFromBytes(mydata);

            filesTable.put(uuid,fileName);
            return uuid;

        } catch (IOException e) {

            e.printStackTrace();
        }

        System.out.println("Done writing data...");
        return null;
    }
}
