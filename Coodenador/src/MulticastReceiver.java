import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastReceiver extends Thread {

    protected byte[] buf = new byte[256];

    public void run() {
        MulticastSocket socket = null;
        InetAddress group = null;
        try {
            socket = new MulticastSocket(4446);
            group = InetAddress.getByName("230.0.0.0");
            socket.joinGroup(group);
            Coordenador c = new Coordenador();

            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                if ("end".equals(received)) break;

//tipoMensagem;rmi://localhost:2024/processor[;0]
                String[] parts = received.split(";");
                String tipo = parts[0];

                if (tipo == "setup") {
                    c.addProcessor(parts[1]);
                }else if(tipo == "b"){
                    c.sendProcessorList(parts[1]);
                }else{//update
                    c.updateProcessor(parts[1]);
                }
            }
            socket.leaveGroup(group);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        socket.close();
    }
}