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

    public static String reservarLibro(String isbn, String ciEstudiante, int diasReserva) {
        String queryLibro = "SELECT id, cantidad_total FROM libro WHERE isbn = ?";
        String queryEstudiante = "SELECT cedula FROM estudiante WHERE CAST(cedula AS VARCHAR) = ?";
        String queryReservasActivas = "SELECT COUNT(*) FROM reserva WHERE id_libro = ? AND fecha_fin >= CURRENT_DATE";
        String queryInsertReserva = "INSERT INTO reserva (id_libro, cedula_estudiante, fecha_inicio, fecha_fin) VALUES (?, ?, CURRENT_DATE, CURRENT_DATE + ? * INTERVAL '1 day')";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1. Validar existencia del libro y obtener su ID
            int idLibro = -1;
            int cantidadTotal = 0;
            try (PreparedStatement stmtLibro = conn.prepareStatement(queryLibro)) {
                stmtLibro.setString(1, isbn);
                ResultSet rs = stmtLibro.executeQuery();
                if (rs.next()) {
                    idLibro = rs.getInt("id");
                    cantidadTotal = rs.getInt("cantidad_total");
                } else {
                    conn.rollback();
                    return "{\"error\": \"El libro con el ISBN especificado no existe\"}";
                }
            }

            // 2. Validar estudiante por su cédula
            String cedulaEstudiante = null;
            try (PreparedStatement stmtEst = conn.prepareStatement(queryEstudiante)) {
                stmtEst.setString(1, ciEstudiante);
                ResultSet rs = stmtEst.executeQuery();
                if (rs.next()) {
                    cedulaEstudiante = rs.getString("cedula");
                } else {
                    conn.rollback();
                    return "{\"error\": \"El estudiante con la CI/cedula especificada no esta registrado\"}";
                }
            }

            // 3. Contar reservas activas
            int reservasActivas = 0;
            try (PreparedStatement stmtRes = conn.prepareStatement(queryReservasActivas)) {
                stmtRes.setInt(1, idLibro);
                ResultSet rs = stmtRes.executeQuery();
                if (rs.next()) {
                    reservasActivas = rs.getInt(1);
                }
            }

            // 4. Validar disponibilidad
            if (cantidadTotal - reservasActivas <= 0) {
                conn.rollback();
                return "{\"error\": \"No hay ejemplares disponibles para reserva\"}";
            }

            // 5. Insertar reserva
            try (PreparedStatement stmtInsert = conn.prepareStatement(queryInsertReserva)) {
                stmtInsert.setInt(1, idLibro);
                stmtInsert.setString(2, cedulaEstudiante);
                stmtInsert.setInt(3, diasReserva);
                stmtInsert.executeUpdate();
            }

            conn.commit();
            return "{\"mensaje\": \"Reserva realizada exitosamente por " + diasReserva + " dias\"}";

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("[Error BD Reserva]: " + e.getMessage());
            return "{\"error\": \"Error en BD al procesar la reserva: " + e.getMessage().replace("\"", "'") + "\"}";
        } finally {
            if (conn != null) {
                try { 
                    conn.setAutoCommit(true); 
                    conn.close(); 
                } catch (SQLException e) { 
                    e.printStackTrace(); 
                }
            }
        }
    }

    public static String cancelarReserva(String isbn, String ciEstudiante) {
        String queryLibro = "SELECT id FROM libro WHERE isbn = ?";
        String queryEstudiante = "SELECT cedula FROM estudiante WHERE CAST(cedula AS VARCHAR) = ?";
        String queryBuscarReserva = "SELECT id FROM reserva WHERE id_libro = ? AND cedula_estudiante = ? AND fecha_fin >= CURRENT_DATE LIMIT 1";
        String queryDeleteReserva = "DELETE FROM reserva WHERE id = ?";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1. Verificar libro
            int idLibro = -1;
            try (PreparedStatement stmtLibro = conn.prepareStatement(queryLibro)) {
                stmtLibro.setString(1, isbn);
                ResultSet rs = stmtLibro.executeQuery();
                if (rs.next()) {
                    idLibro = rs.getInt("id");
                } else {
                    conn.rollback();
                    return "{\"error\": \"El libro con el ISBN especificado no existe\"}";
                }
            }

            // 2. Verificar estudiante
            String cedulaEstudiante = null;
            try (PreparedStatement stmtEst = conn.prepareStatement(queryEstudiante)) {
                stmtEst.setString(1, ciEstudiante);
                ResultSet rs = stmtEst.executeQuery();
                if (rs.next()) {
                    cedulaEstudiante = rs.getString("cedula");
                } else {
                    conn.rollback();
                    return "{\"error\": \"El estudiante no esta registrado\"}";
                }
            }

            // 3. Buscar la reserva activa
            int idReserva = -1;
            try (PreparedStatement stmtReserva = conn.prepareStatement(queryBuscarReserva)) {
                stmtReserva.setInt(1, idLibro);
                stmtReserva.setString(2, cedulaEstudiante);
                ResultSet rs = stmtReserva.executeQuery();
                if (rs.next()) {
                    idReserva = rs.getInt("id");
                } else {
                    conn.rollback();
                    return "{\"error\": \"No se encontro una reserva activa para este libro y estudiante\"}";
                }
            }

            // 4. Eliminar la reserva
            try (PreparedStatement stmtDelete = conn.prepareStatement(queryDeleteReserva)) {
                stmtDelete.setInt(1, idReserva);
                stmtDelete.executeUpdate();
            }

            conn.commit();
            return "{\"mensaje\": \"Reserva cancelada exitosamente\"}";

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("[Error BD Cancelar Reserva]: " + e.getMessage());
            return "{\"error\": \"Error en BD al cancelar la reserva: " + e.getMessage().replace("\"", "'") + "\"}";
        } finally {
            if (conn != null) {
                try { 
                    conn.setAutoCommit(true); 
                    conn.close(); 
                } catch (SQLException e) { 
                    e.printStackTrace(); 
                }
            }
        }
    }
}