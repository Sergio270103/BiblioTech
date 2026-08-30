package py.una.server.tcp;

import java.io.*;
import java.net.*;

public class TCPServer {

    public static void main(String[] args) throws Exception {

        int puertoServidor = 4444;
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(puertoServidor);
        } catch (IOException e) {
            System.err.println("No se puede abrir el puerto: " + puertoServidor + ".");
            System.exit(1);
        }

        System.out.println("BiblioTech: Puerto abierto: " + puertoServidor + ". Esperando al SGA...");
        
        Socket clientSocket = null;
        try {
            clientSocket = serverSocket.accept();
            System.out.println("SGA Conectado exitosamente.");
        } catch (IOException e) {
            System.err.println("Fallo el accept().");
            System.exit(1);
        }

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        out.println("Bienvenido a BiblioTech! Envie su peticion JSON.");
        
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println("Peticion recibida del SGA: " + inputLine);
            
            if (inputLine.equals("Bye")) {
                out.println("Bye");
                break;
            }

            String outputLine = "{\"error\": \"Servicio no reconocido\"}";
            if (inputLine.contains("978-0132143011")) {
                outputLine = "{\"isbn\": \"978-0132143011\", \"titulo\": \"Sistemas Distribuidos: Conceptos y Diseños\", \"cantidad_disponible\": 4, \"formato_digital\": true}";
            }
            
            out.println(outputLine);
        }

        out.close();
        in.close();
        clientSocket.close();
        serverSocket.close();
    }
}