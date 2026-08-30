UDPServer (corregido):
package py.una.server.udp;
import java.net.*;

public class UDPServer {
    public static void main(String[] args) {
        int puertoServidor = 9876;
        try (DatagramSocket serverSocket = new DatagramSocket(puertoServidor)) {
            System.out.println("BiblioTech (UDP): Puerto " + puertoServidor + " abierto. Esperando peticiones...");
            byte[] receiveData = new byte[1024];
            
            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                
                String datoRecibido = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                
                System.out.println("Peticion recibida de " + IPAddress + ":" + port + " -> " + datoRecibido);
                
                // ✅ NUEVO: Detectar mensaje de cierre
                if (datoRecibido.equalsIgnoreCase("CLOSE_SERVER")) {
                    System.out.println("Solicitud de cierre recibida. Cerrando servidor...");
                    break; // Sale del bucle y cierra el servidor
                }
                
                String respuestaJson = "{\"error\": \"Servicio no reconocido\"}";
                if (datoRecibido.contains("978-0132143011")) {
                    respuestaJson = "{\"isbn\": \"978-0132143011\", \"titulo\": \"Sistemas Distribuidos: Conceptos y Diseños\", \"cantidad_disponible\": 4, \"formato_digital\": true}";
                }
                
                byte[] sendData = respuestaJson.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);
            }
        } catch (Exception e) {
            System.err.println("Error en el servidor UDP: " + e.getMessage());
        }
        System.out.println("Servidor UDP cerrado.");
    }
}