package py.una.pol.sd.server.UDP;

import java.io.*;
import java.net.*;
import py.una.pol.sd.bd.BdServer;

public class UDPServer {
    private static final int PUERTO_SERVIDOR = 9876;

    public static void main(String[] args) {

        // 1. Hilo secundario: Servicio de escucha UDP
        Thread listenerThread = new Thread(() -> {
            try (DatagramSocket serverSocket = new DatagramSocket(PUERTO_SERVIDOR)) {
                System.out.println("\n[Servicio Escucha UDP] Puerto abierto: " + PUERTO_SERVIDOR + ". Esperando peticiones...");
                
                while (!Thread.currentThread().isInterrupted()) {
                    DatagramPacket receivePacket = null;
                    InetAddress clientAddress = null;
                    int clientPort = -1;
                    String respuestaJson = "{\"error\": \"Error interno en servidor UDP\"}";

                    try {
                        byte[] receiveData = new byte[8192];
                        receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        
                        serverSocket.receive(receivePacket);

                        clientAddress = receivePacket.getAddress();
                        clientPort = receivePacket.getPort();

                        String datoRecibido = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8").trim();
                        System.out.println("\n[Servicio Escucha UDP] Recibido desde " + clientAddress + ":" + clientPort + " -> " + datoRecibido);

                        if (datoRecibido.equalsIgnoreCase("CLOSE_SERVER")) {
                            System.out.println("[Servicio Escucha UDP] Solicitud de cierre recibida.");
                            break;
                        }

                        if (datoRecibido.contains("\"accion\": \"listar_disponibles\"")) {
                            respuestaJson = BdServer.listarLibrosDisponibles();
                        } else if (datoRecibido.contains("\"accion\": \"reservar_libro\"")) {
                            try {
                                String isbn = extraerValorJson(datoRecibido, "isbn");
                                String ci = extraerValorJson(datoRecibido, "ci");
                                String diasStr = extraerValorJson(datoRecibido, "dias");
                                int dias = Integer.parseInt(diasStr);

                                respuestaJson = BdServer.reservarLibro(isbn, ci, dias);
                            } catch (Exception e) {
                                System.err.println("[Servicio Escucha UDP] Error extrayendo parametros: " + e.getMessage());
                                respuestaJson = "{\"error\": \"Formato de JSON invalido o campos faltantes\"}";
                            }
                        } else if (datoRecibido.contains("\"accion\": \"cancelar_reserva\"")) {
                            try {
                                String isbn = extraerValorJson(datoRecibido, "isbn");
                                String ci = extraerValorJson(datoRecibido, "ci");

                                respuestaJson = BdServer.cancelarReserva(isbn, ci);
                            } catch (Exception e) {
                                System.err.println("[Servicio Escucha UDP] Error extrayendo parametros: " + e.getMessage());
                                respuestaJson = "{\"error\": \"Formato de JSON invalido o campos faltantes\"}";
                            }
                        } else {
                            respuestaJson = "{\"error\": \"Servicio no reconocido\"}";
                        }

                    } catch (Exception e) {
                        System.err.println("[Servicio Escucha UDP] Excepcion procesando peticion: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        if (clientAddress != null && clientPort != -1) {
                            try {
                                byte[] sendData = respuestaJson.getBytes("UTF-8");
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                                serverSocket.send(sendPacket);
                                System.out.println("[Servicio Escucha UDP] Respuesta enviada a " + clientAddress + ":" + clientPort);
                            } catch (Exception e) {
                                System.err.println("[Servicio Escucha UDP] Error critico enviando respuesta: " + e.getMessage());
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                System.err.println("[Servicio Escucha UDP] No se pudo abrir el socket en el puerto " + PUERTO_SERVIDOR + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("[Servicio Escucha UDP] Error fatal: " + e.getMessage());
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();

        // 2. Hilo principal: Menú interactivo del Servidor
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

    private static String extraerValorJson(String json, String clave) {
        String patronClave = "\"" + clave + "\"";
        int idxClave = json.indexOf(patronClave);
        if (idxClave == -1) return "";

        int idxDosPuntos = json.indexOf(":", idxClave);
        if (idxDosPuntos == -1) return "";

        String sub = json.substring(idxDosPuntos + 1).trim();

        if (sub.startsWith("\"")) {
            sub = sub.substring(1);
            int idxFin = sub.indexOf("\"");
            return idxFin != -1 ? sub.substring(0, idxFin) : "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (char c : sub.toCharArray()) {
                if (Character.isDigit(c)) {
                    sb.append(c);
                } else {
                    break;
                }
            }
            return sb.toString();
        }
    }
}