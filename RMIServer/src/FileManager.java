import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FileManager extends UnicastRemoteObject implements FileManagerInterface, Serializable {
    private static final long serialVersionUID = 1L;

    static private File objF;
    String url;//url do proprio

    private final ScheduledExecutorService elctionTimeout = Executors.newScheduledThreadPool(1);//executor para a elections

    volatile int term;
    volatile int nOperations;
    boolean isLeader = false;

    int nVotos;
    int nServers;
    boolean hasVotedInTerm = false;
    int commitOperations=0;

    FileOutputStream buffer;
    byte[] mydataBuffer;

    protected FileManager(String _url) throws RemoteException {
        this();
        url=_url;
    }
    protected FileManager() throws RemoteException {
        objF = new File("./files/");
        //deleteDirectory(objF);
        objF.mkdirs();
        if(objF.exists())
            System.out.println("Pasta pronta");
        startElectionTimeout();
    }


    @Override
    public UUID uploadFileToServer(byte[] mydata, int length) throws RemoteException {
        UUID uuid;
        uuid= UUID.nameUUIDFromBytes(mydata);

        if (saveFile(mydata,"./files/"+uuid+".txt"))
            return uuid;
        else
            return null;
    }

    @Override
    public void uploadResFile(byte[] mybyte, String filename, int length) throws RemoteException {
        saveFile(mybyte, filename);
    }

    private boolean saveFile(byte[] mydata, String filename){
        try {
            File serverpathfile = new File(filename);
            buffer=new FileOutputStream(serverpathfile);
            mydataBuffer = mydata;


//c;termo;nOperation;fileId;bytes[]
            if (isLeader) {
                nServers = commitOperations;//atualiza o numero de servidores na rede
                commitOperations =1;
                nOperations++;
                MulticastPublisher mp = new MulticastPublisher();
                mp.sendMulticastMessage("c;" + term + ";" + nOperations + ";" + filename + ";" + Arrays.toString(mydata)+";"+url);
            }else{
                buffer.write(mydata);
                buffer.flush();
                buffer.close();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public String getFilePath(String fileName) throws RemoteException {
        File f = new File("./files/"+fileName+".txt");
        return f.getAbsolutePath();
    }

    @Override
    public void voto() {
        if (++nVotos > nServers/2) {
            isLeader = true;
            nOperations = 1;
        }
    }

    @Override
    public void commit(int _term) {
        commitOperations++;

        if(commitOperations > nServers/2) {//se for a mioria guarda os ficheiros
            try {
                buffer.write(mydataBuffer);
                buffer.flush();
                buffer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void startElectionTimeout() {
        final Runnable setupElectionRunner = new Runnable() {
            public void run() {
                nVotos=1;
                term++;
                MulticastPublisher mp = new MulticastPublisher();
                try {
                    mp.sendMulticastMessage("e;" + term + ";" + nOperations + ";" + url);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        elctionTimeout.schedule(setupElectionRunner, (int)Math.floor(Math.random()*(3000-1000+1)+3000), TimeUnit.MILLISECONDS);
    }

    private void stopElectionTimeout(){
        elctionTimeout.shutdownNow();
    }

    private void resetElectionTimeout(){
        stopElectionTimeout();
        startElectionTimeout();
    }

    //c;termo;nOperation;fileId;bytes[]
    public void leaderMessage(int _term, int nOperation, String filename,byte[] mydata){
        if(term > _term){return;}

        resetElectionTimeout();
        term = _term;
        isLeader = false;
        nOperations=nOperation;
        if(saveFile(mydata,filename)){//se foi guardado, diz ao lider que pode fazer commit
            FileManagerInterface fileManager;
            try {
                fileManager = (FileManagerInterface) Naming.lookup(url);
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                throw new RuntimeException(e);
            }
            fileManager.commit(term);
        }
    }

    //e;termo;nOperation;url
    public void election(int _term, int nOperation, String url){
        if (hasVotedInTerm){return;}
        if(_term > term && nOperation >= nOperations) {
            hasVotedInTerm = true;
            FileManagerInterface fileManager;
            try {
                fileManager = (FileManagerInterface) Naming.lookup(url);
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                throw new RuntimeException(e);
            }
            fileManager.voto();
        }
    }
}
