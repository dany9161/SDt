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
            group = InetAddress.getByName("230.0.0.1");
            socket.joinGroup(group);
            Processor p = new Processor();

            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                if ("end".equals(received)) break;

//C heartbeat - h;nP
//P C morto - m;url
                String[] parts = received.split(";");
                String tipo = parts[0];

                if(tipo == "h"){//mensagem enviada pelo controlador
                    p.refreshControlador(Integer.parseInt(parts[1]));
                }else if(tipo == "m"){//mensagem enviada por outro processador
                    p.avaliaCMorreu();
                } else if (tipo.equals("c")) {
                    p.setStorageLeader(parts[5]);
                }
            }
            socket.leaveGroup(group);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        socket.close();
    }
}