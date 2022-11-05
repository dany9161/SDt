import java.util.UUID;

public class Pedido {
    char status;
    String scriptPath;
    UUID fileUUID;

    public Pedido(String scriptPath, UUID fileUUID) {
        this.scriptPath = scriptPath;
        this.fileUUID = fileUUID;
        //Quando um processo é criado, fica em pending
        status='P';
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public UUID getFileUUID() {
        return fileUUID;
    }

    public void setStatusRunning(){
        this.status = 'R';
    }

    public void setStatusOver(){
        this.status = 'O';
    }
}
