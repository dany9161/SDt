import java.util.UUID;

public class Pedido {
    private String status;
    private String scriptPath;
    private UUID fileUUID;
    private UUID pedidoId;

    public Pedido(String _scriptPath, UUID _fileUUID,UUID _pedidoId) {
        scriptPath = _scriptPath;
        fileUUID = _fileUUID;
        pedidoId=_pedidoId;
        //Quando um processo é criado, fica em pending
        status="Waiting";
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
}
