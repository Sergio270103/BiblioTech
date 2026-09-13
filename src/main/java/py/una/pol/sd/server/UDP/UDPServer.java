package py.una.pol.sd.server.UDP;

import java.io.*;
import java.net.*;
import py.una.pol.sd.bd.BdServer;

public class UDPServer {
    public static void main(String[] args) {
        int puertoServidor = 9876;

        // 1. Hilo secundario: Escucha a los clientes (edutech)
        Thread listenerThread = new Thread(() -> {
            try (DatagramSocket serverSocket = new DatagramSocket(puertoServidor)) {
                System.out.println("\n[Servicio Escucha] Puerto UDP abierto: " + puertoServidor + ". Esperando peticiones...");
                byte[] receiveData = new byte[1024];
                
                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);

                    String datoRecibido = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
                    InetAddress IPAddress = receivePacket.getAddress();
                    int port = receivePacket.getPort();

                    if (datoRecibido.equalsIgnoreCase("CLOSE_SERVER")) {
                        break;
                    }

                    String respuestaJson = "{\"error\": \"Servicio no reconocido\"}";
                    if (datoRecibido.contains("\"accion\": \"listar_disponibles\"")) {
                        respuestaJson = BdServer.listarLibrosDisponibles();
                    }

                    byte[] sendData = respuestaJson.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    serverSocket.send(sendPacket);
                }
            } catch (Exception e) {
                System.err.println("Error en servidor UDP: " + e.getMessage());
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();

        // 2. Hilo principal: Menú interactivo de BiblioTech
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (true) {
                System.out.println("\n--- Menu BiblioTech (UDP) ---");
                System.out.println("1. Cargar un nuevo libro");
                System.out.println("Escriba 'Bye' para salir.");
                System.out.print("Elija una opcion: ");

                String input = stdIn.readLine();

                if (input == null || input.equalsIgnoreCase("Bye")) {
                    System.out.println("Cerrando servidor BiblioTech...");
                    break;
                }

                if (input.equals("1")) {
                    System.out.print("Ingrese ISBN: ");
                    String isbn = stdIn.readLine();
                    System.out.print("Ingrese Titulo: ");
                    String titulo = stdIn.readLine();
                    System.out.print("Ingrese Autor: ");
                    String autor = stdIn.readLine();
                    System.out.print("Ingrese Cantidad Total: ");
                    int cantidad = Integer.parseInt(stdIn.readLine());

                    String respuesta = BdServer.agregarLibro(isbn, titulo, autor, cantidad);
                    System.out.println("Resultado BD: " + respuesta);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}