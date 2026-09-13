package py.una.pol.sd.server.TCP;

import java.io.*;
import java.net.*;
import py.una.pol.sd.bd.BdServer;

public class TCPServer {
    public static void main(String[] args) {
        int puertoServidor = 4444;

        // 1. Hilo secundario: Escucha a los clientes (edutech)
        Thread listenerThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(puertoServidor)) {
                System.out.println("\n[Servicio Escucha TCP] Puerto abierto: " + puertoServidor + ". Esperando peticiones de edutech...");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    manejarCliente(clientSocket);
                }
            } catch (IOException e) {
                System.err.println("Error en el servidor TCP: " + e.getMessage());
            }
        });
        listenerThread.setDaemon(true); 
        listenerThread.start();

        // 2. Hilo principal: Menú interactivo de BiblioTech
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (true) {
                System.out.println("\n--- Menu BiblioTech (TCP) ---");
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

    private static void manejarCliente(Socket clientSocket) {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            
            out.println("Bienvenido a BiblioTech! Envie su peticion JSON.");
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equals("Bye")) {
                    out.println("Bye");
                    break;
                }

                String outputLine = "{\"error\": \"Servicio no reconocido\"}";
                
                if (inputLine.contains("\"accion\": \"listar_disponibles\"")) {
                    System.out.println("\n[Servicio Escucha TCP] Recibido de SGA: " + inputLine);
                    outputLine = BdServer.listarLibrosDisponibles();
                    System.out.println("[Servicio Escucha TCP] Envio exitoso.");
                    System.out.print("\n--- Menu BiblioTech (TCP) ---\n1. Cargar un nuevo libro\nEscriba 'Bye' para salir.\nElija una opcion: ");
                
                } else if (inputLine.contains("\"accion\": \"reservar_libro\"")) {
                    System.out.println("\n[Servicio Escucha TCP] Recibido de SGA: " + inputLine);
                    try {
                        String isbn = extraerValorJson(inputLine, "isbn");
                        String ci = extraerValorJson(inputLine, "ci");
                        String diasStr = extraerValorJson(inputLine, "dias");
                        int dias = Integer.parseInt(diasStr);

                        outputLine = BdServer.reservarLibro(isbn, ci, dias);
                    } catch (Exception e) {
                        outputLine = "{\"error\": \"Formato de solicitud de reserva invalido\"}";
                    }
                    System.out.println("[Servicio Escucha TCP] Envio exitoso.");
                    System.out.print("\n--- Menu BiblioTech (TCP) ---\n1. Cargar un nuevo libro\nEscriba 'Bye' para salir.\nElija una opcion: ");
                
                } else if (inputLine.contains("\"accion\": \"cancelar_reserva\"")) {
                    System.out.println("\n[Servicio Escucha TCP] Recibido de SGA: " + inputLine);
                    try {
                        String isbn = extraerValorJson(inputLine, "isbn");
                        String ci = extraerValorJson(inputLine, "ci");

                        outputLine = BdServer.cancelarReserva(isbn, ci);
                    } catch (Exception e) {
                        outputLine = "{\"error\": \"Formato de solicitud de cancelacion invalido\"}";
                    }
                    System.out.println("[Servicio Escucha TCP] Envio exitoso.");
                    System.out.print("\n--- Menu BiblioTech (TCP) ---\n1. Cargar un nuevo libro\nEscriba 'Bye' para salir.\nElija una opcion: ");
                }
                out.println(outputLine);
            }
        } catch (IOException e) {
            System.err.println("Error manejando cliente: " + e.getMessage());
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