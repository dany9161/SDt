import java.util.UUID;

public class ReplicaInfo {
    String scriptPath;
    UUID idPedido;
    UUID fileId;

    public ReplicaInfo(String scriptPath, UUID idPedido, UUID fileId) {
        this.scriptPath = scriptPath;
        this.idPedido = idPedido;
        this.fileId = fileId;
    }
}
