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
            Balanceador b =new Balanceador();
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());

                if ("end".equals(received)) {
                    break;
                }else{
//rmi://localhost:2024/processor;0
                    String[] parts = received.split(";");
                    String address = parts[0];
                    String nPedidosWating = parts[1];
                    b.updateProcessor(address,Integer.parseInt(nPedidosWating));
                    System.out.println("No processador "+address+" há "+nPedidosWating+ " pedidos à espera");
                }
            }
            socket.leaveGroup(group);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        socket.close();
    }
}