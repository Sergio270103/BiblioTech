package py.una.pol.sd.bd;

import java.sql.*;

public class BdServer {
    // CAMBIAR CON TUS DATOS DE POSTGRESQL
    private static final String URL = "jdbc:postgresql://localhost:5432/BibliotechBD";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String consultarDisponibilidad(String isbn) {
        String queryLibro = "SELECT id, titulo, cantidad_total FROM libro WHERE isbn = ?";
        String queryReservas = "SELECT COUNT(*) FROM reserva WHERE id_libro = ? AND fecha_fin >= CURRENT_DATE";

        try (Connection conn = getConnection();
            PreparedStatement stmtLibro = conn.prepareStatement(queryLibro)) {

            stmtLibro.setString(1, isbn);
            ResultSet rsLibro = stmtLibro.executeQuery();

            if (rsLibro.next()) {
                int idLibro = rsLibro.getInt("id");
                String titulo = rsLibro.getString("titulo");
                int cantidadTotal = rsLibro.getInt("cantidad_total");

                try (PreparedStatement stmtReservas = conn.prepareStatement(queryReservas)) {
                    stmtReservas.setInt(1, idLibro);
                    ResultSet rsReservas = stmtReservas.executeQuery();
                    int reservasActivas = 0;
                    if (rsReservas.next()) {
                        reservasActivas = rsReservas.getInt(1);
                    }

                    int cantidadDisponible = cantidadTotal - reservasActivas;

                    return String.format("{\"isbn\": \"%s\", \"titulo\": \"%s\", \"cantidad_total\": %d, \"cantidad_disponible\": %d, \"reservas_activas\": %d}",
                            isbn, titulo, cantidadTotal, cantidadDisponible, reservasActivas);
                }
            } else {
                return "{\"error\": \"Libro no encontrado en la base de datos\"}";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "{\"error\": \"Error de conexion a la BD\"}";
        }
    }

    public static String listarLibrosDisponibles() {
        String queryLibros = "SELECT id, isbn, titulo, cantidad_total FROM libro";
        String queryReservas = "SELECT COUNT(*) FROM reserva WHERE id_libro = ? AND fecha_fin >= CURRENT_DATE";

        StringBuilder jsonResult = new StringBuilder("[");

        try (Connection conn = getConnection();
            PreparedStatement stmtLibros = conn.prepareStatement(queryLibros);
            ResultSet rsLibros = stmtLibros.executeQuery()) {

            boolean first = true;

            while (rsLibros.next()) {
                int idLibro = rsLibros.getInt("id");
                String isbn = rsLibros.getString("isbn");
                String titulo = rsLibros.getString("titulo");
                int cantidadTotal = rsLibros.getInt("cantidad_total");

                int reservasActivas = 0;
                try (PreparedStatement stmtReservas = conn.prepareStatement(queryReservas)) {
                    stmtReservas.setInt(1, idLibro);
                    ResultSet rsReservas = stmtReservas.executeQuery();
                    if (rsReservas.next()) {
                        reservasActivas = rsReservas.getInt(1);
                    }
                }

                int cantidadDisponible = cantidadTotal - reservasActivas;

                if (cantidadDisponible > 0) {
                    if (!first) {
                        jsonResult.append(", ");
                    }
                    jsonResult.append(String.format("{\"isbn\": \"%s\", \"titulo\": \"%s\", \"disponibles\": %d}", 
                            isbn, titulo, cantidadDisponible));
                    first = false;
                }
            }
            jsonResult.append("]");
            return jsonResult.toString();

        } catch (SQLException e) {
            e.printStackTrace();
            return "{\"error\": \"Error de conexion a la BD al listar libros\"}";
        }
    }

    public static String agregarLibro(String isbn, String titulo, String autor, int cantidad) {
        String queryInsert = "INSERT INTO libro (isbn, titulo, autor, cantidad_total) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(queryInsert)) {

            stmt.setString(1, isbn);
            stmt.setString(2, titulo);
            stmt.setString(3, autor);
            stmt.setInt(4, cantidad);

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                return "{\"mensaje\": \"Libro cargado exitosamente\"}";
            } else {
                return "{\"error\": \"No se pudo cargar el libro\"}";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("duplicate key value") || e.getMessage().contains("llave duplicada")) {
                return "{\"error\": \"El ISBN ya existe en la base de datos\"}";
            }
            return "{\"error\": \"Error de conexion al cargar el libro\"}";
        }
    }
}