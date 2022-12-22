import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Objects;

public class MulticastReceiver extends Thread {

    protected byte[] buf = new byte[256];

    public void run() {
        MulticastSocket socket = null;
        InetAddress group = null;
        try {
            socket = new MulticastSocket(4446);
            group = InetAddress.getByName("230.0.0.0");
            socket.joinGroup(group);
            FileManager fileManager = new FileManager();

            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                if ("end".equals(received)) break;

// comando - c;termo;nOperation;fileId;bytes[];url
// eleicao - e;termo;nOperation;url
                String[] parts = received.split(";");
                String tipo = parts[0];

                if(Objects.equals(tipo, "e")){//mensagem enviada pelo controlador
                    //eleição
                    fileManager.election(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),parts[3]);
                }else if(Objects.equals(tipo, "c")){//mensagem enviada por outro processador
                    //comando
                    fileManager.leaderMessage(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),parts[3],parts[4].getBytes());
                }
            }
            socket.leaveGroup(group);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        socket.close();
    }
}