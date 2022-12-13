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
            Balanceador b =new Balanceador();
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                if ("end".equals(received)) break;

//tipoMensagem;rmi://localhost:2024/processor[;0]
                String[] parts = received.split(";");
                String tipo = parts[0];
                String address = parts[1];

                if(Objects.equals(tipo, "setup")){
                    b.addProcessador(address,0);
                }else{//update
                    String nPedidosWating = parts[2];
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