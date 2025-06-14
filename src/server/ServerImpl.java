package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.ClimaCiudad;
import common.InterfazDeServer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerImpl implements InterfazDeServer {

    private final String API_KEY = "00699a1a425bce6e4a3daddf1b447487"; 
    private ArrayList<ClimaCiudad> historial = new ArrayList<>();
    private ArrayList<ClimaCiudad> bd_clima_copia = new ArrayList<>();
    private final Lock lock = new ReentrantLock(); // Lock para sincronizar el acceso a la base de datos
    
    public ServerImpl() throws RemoteException {
        ConectarBD();
    }
    
    @Override
    public synchronized void clienteConectado(String nombreCliente) throws RemoteException {
        System.out.println("\n================== AgroMonitoreo ==================");
        System.out.printf("         %s ha iniciado sesión%n", nombreCliente);
        System.out.println("===================================================\n");
    }
    
    @Override
    public synchronized void clienteDesconectado(String nombreCliente) throws RemoteException {
        System.out.println("\n================== AgroMonitoreo ==================");
        System.out.printf("         %s se ha desconectado%n", nombreCliente);
        System.out.println("===================================================\n");
    }

    private boolean requestMutex(String nombreCliente) {
        try {
            System.out.println("\n================== Zona Crítica ==================");
            System.out.printf("   %s está intentando acceder a la zona crítica%n", nombreCliente);
            System.out.println("==================================================\n");

            boolean acquired = lock.tryLock(60, TimeUnit.SECONDS);
            if (acquired) {
                System.out.println("\n================== Zona Crítica ==================");
                System.out.printf("   %s obtuvo el bloqueo MUTEX%n", nombreCliente);
                System.out.println("==================================================\n");
            }
            return acquired;
        } catch (InterruptedException e) {
            System.err.println("Error al adquirir mutex: " + e.getMessage());
            return false;
        }
    }

    private void releaseMutex(String nombreCliente) {
        lock.unlock();
        System.out.println("\n================== Zona Crítica ==================");
        System.out.printf("   %s ha liberado el bloqueo MUTEX%n", nombreCliente);
        System.out.println("==================================================\n");
    }

    // Método para guardar el clima en la base de datos
    public void guardarEnBaseDeDatos(ClimaCiudad clima) {
        String url = "jdbc:mysql://localhost:3306/clima";
        String username = "root";
        String password_BD = "";
        PreparedStatement ps = null;

        try {
            Connection con = DriverManager.getConnection(url, username, password_BD);
            ps = con.prepareStatement(
                "INSERT INTO clima_ciudad (ciudad, temperatura, humedad, descripcion, fecha, hora) VALUES (?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, clima.getCiudad());
            ps.setDouble(2, clima.getTemperatura());
            ps.setInt(3, clima.getHumedad());
            ps.setString(4, clima.getDescripcion());
            ps.setString(5, clima.getFechaConsulta());
            ps.setString(6, clima.getHoraConsulta());
            int filas = ps.executeUpdate();

            String mensaje;
            if (filas > 0) {
                mensaje = "Clima de la ciudad '" + clima.getCiudad() + "' guardado exitosamente en la base de datos.";
            } else {
                mensaje = "No se pudo guardar el clima de la ciudad '" + clima.getCiudad() + "' en la base de datos.";
            }
            imprimirCuadro(mensaje);

            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
            String mensaje = "Error al guardar en la base de datos el clima de la ciudad '" + clima.getCiudad() + "'.";
            imprimirCuadro(mensaje);
        }
    }
    
    // Método para conectar a la base de datos y crearla si no existe
    public void ConectarBD() {
        Connection connection = null;
        Statement query = null;
        ResultSet rs = null;
        ResultSet resultados = null;

        try {
            String url = "jdbc:mysql://localhost:3306/";
            String username = "root";
            String password_BD = "";

            connection = DriverManager.getConnection(url, username, password_BD);
            query = connection.createStatement();

            // Verificar si existe la base de datos 'clima'
            rs = query.executeQuery("SHOW DATABASES LIKE 'clima'");
            if (!rs.next()) {
                System.out.println("Base de datos 'clima' no existe. Creando...");
                query.executeUpdate("CREATE DATABASE clima");
            }

            // Conexión a la base de datos 'clima'
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/clima", username, password_BD);
            query = connection.createStatement();

            // Verificar y crear tabla 'clima_ciudad'
            rs = query.executeQuery("SHOW TABLES LIKE 'clima_ciudad'");
            if (!rs.next()) {
                System.out.println("Tabla 'clima_ciudad' no existe. Creando...");
                String createTableSQL = "CREATE TABLE clima_ciudad (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "ciudad VARCHAR(100), " +
                    "temperatura DOUBLE, " +
                    "humedad INT, " +
                    "descripcion VARCHAR(255), " +
                    "fecha DATE, " +
                    "hora TIME)";
                query.executeUpdate(createTableSQL);
                System.out.println("Tabla 'clima_ciudad' creada exitosamente.");
            }

            // Verificar y crear tabla 'alertas_climaticas'
            rs = query.executeQuery("SHOW TABLES LIKE 'alertas_climaticas'");
            if (!rs.next()) {
                System.out.println("Tabla 'alertas_climaticas' no existe. Creando...");
                String createTableSQL = "CREATE TABLE alertas_climaticas (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "ciudad VARCHAR(100), " +
                    "alerta TEXT, " +
                    "fecha DATE, " +
                    "hora TIME)";
                query.executeUpdate(createTableSQL);
                System.out.println("Tabla 'alertas_climaticas' creada exitosamente.");
            }

            // Verificar y crear tabla 'favoritos' actualizada con campos de clima
            rs = query.executeQuery("SHOW TABLES LIKE 'favoritos'");
            if (!rs.next()) {
                System.out.println("Tabla 'favoritos' no existe. Creando...");
                String createTableSQL = "CREATE TABLE favoritos (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "cliente VARCHAR(100), " +
                    "ciudad VARCHAR(100), " +
                    "temperatura DOUBLE, " +
                    "humedad INT, " +
                    "descripcion VARCHAR(255), " +
                    "fecha DATE, " +
                    "hora TIME)";
                query.executeUpdate(createTableSQL);
                System.out.println("Tabla 'favoritos' creada exitosamente.");
            }

            // Verificación final
            if (connection != null) {
                System.out.println("╔══════════════════════════════════════════════╗");
                System.out.println("║                                              ║");
                System.out.println("║ Conexión exitosa a la base de datos 'clima'  ║");
                System.out.println("║                                              ║");
                System.out.println("╚══════════════════════════════════════════════╝");
            }

            // Cargar registros existentes a memoria
            String sql = "SELECT * FROM clima_ciudad";
            resultados = query.executeQuery(sql);
            while (resultados.next()) {
                String ciudad = resultados.getString("ciudad");
                double temperatura = resultados.getDouble("temperatura");
                int humedad = resultados.getInt("humedad");
                String descripcion = resultados.getString("descripcion");
                String fecha = resultados.getString("fecha");
                String hora = resultados.getString("hora");

                ClimaCiudad newClimaCiudad = new ClimaCiudad(ciudad, temperatura, humedad, descripcion, fecha, hora);
                bd_clima_copia.add(newClimaCiudad);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("╔════════════════════════════════════════════╗");
            System.out.println("║  No se pudo conectar a la base de datos    ║");
            System.out.println("╚════════════════════════════════════════════╝");
        }
    }
    
    @Override
    public ArrayList<ClimaCiudad> getHistorial(String nombreCliente) throws RemoteException {
        String clientName = "Cliente Historial";
        if (!requestMutex(clientName)) throw new RemoteException("Zona crítica ocupada.");

        System.out.println("\n================== Zona Crítica ==================");
        System.out.printf("   %s está accediendo al historial climático%n", clientName);
        System.out.println("==================================================\n");

        ArrayList<ClimaCiudad> historialBD = new ArrayList<>();
        String url = "jdbc:mysql://localhost:3306/clima";
        String username = "root";
        String password_BD = "";

        try (Connection connection = DriverManager.getConnection(url, username, password_BD);
             Statement stmt = connection.createStatement();
             ResultSet resultados = stmt.executeQuery("SELECT * FROM clima_ciudad")) {

            while (resultados.next()) {
                String ciudad = resultados.getString("ciudad");
                double temperatura = resultados.getDouble("temperatura");
                int humedad = resultados.getInt("humedad");
                String descripcion = resultados.getString("descripcion");
                String fecha = resultados.getString("fecha");
                String hora = resultados.getString("hora");

                ClimaCiudad clima = new ClimaCiudad(ciudad, temperatura, humedad, descripcion, fecha, hora);
                historialBD.add(clima);
            }

        } catch (SQLException e) {
            System.out.println("╔══════════════════════════════════════════════╗");
            System.out.println("║   Error al recuperar historial desde la BD   ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            e.printStackTrace();
        } finally {
            releaseMutex(clientName);
        }

        return historialBD;
    }

    
    @Override
    public ClimaCiudad consultarClima(String ciudad, String nombreCliente) throws RemoteException {
        if (!requestMutex(nombreCliente)) throw new RemoteException("Zona crítica ocupada.");

        System.out.println("\n================== Zona Crítica ==================");
        System.out.printf("   %s está consultando el clima de: %s%n", nombreCliente, ciudad);
        System.out.println("==================================================\n");

        try {
            String pais = "CL";  // Código de país para Chile
            String ciudadCodificada = URLEncoder.encode(ciudad, StandardCharsets.UTF_8.toString());

            String urlString = String.format(
                "https://api.openweathermap.org/data/2.5/weather?q=%s,%s&appid=%s&units=metric&lang=es",
                ciudadCodificada, pais, API_KEY
            );

            URL url = URI.create(urlString).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int status = conn.getResponseCode();
            if (status != 200) {
                if (status == 404) {
                    imprimirCuadro("Ciudad no encontrada: " + ciudad);
                    return null;
                } else {
                    imprimirCuadro("Error en la conexión: Código " + status);
                    return null;
                }
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder jsonRespuesta = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                jsonRespuesta.append(inputLine);
            }
            in.close();
            conn.disconnect();

            ObjectMapper mapper = new ObjectMapper();
            ClimaAPIResponse data = mapper.readValue(jsonRespuesta.toString(), ClimaAPIResponse.class);

            double temp = data.main.temp;
            int humedad = data.main.humidity;
            String descripcion = data.weather.get(0).description;

            LocalDateTime ahora = LocalDateTime.now();
            String fechaConsulta = ahora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String horaConsulta = ahora.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            ClimaCiudad clima = new ClimaCiudad(ciudad, temp, humedad, descripcion, fechaConsulta, horaConsulta);

            historial.add(clima);
            guardarEnBaseDeDatos(clima);

            imprimirCuadro("Clima de la ciudad '" + ciudad + "' obtenido y guardado exitosamente.");
            return clima;

        } catch (Exception e) {
            imprimirCuadro("No se pudo obtener el clima de la API.");
            return null;
        } finally {
            releaseMutex(nombreCliente);
        }
    }

    @Override
    public ArrayList<String> obtenerHistorialAlertas(String ciudad, String nombreCliente) throws RemoteException {
        String clientName = "Cliente " + ciudad;
        if (!requestMutex(clientName)) throw new RemoteException("Zona crítica ocupada.");

        System.out.println("\n================== Zona Crítica ==================");
        System.out.printf("   %s está accediendo al historial de alertas de: %s%n", clientName, ciudad);
        System.out.println("==================================================\n");

        ArrayList<String> historialAlertas = new ArrayList<>();
        String DB_URL = "jdbc:mysql://localhost:3306/clima";
        String DB_USER = "root";
        String DB_PASSWORD = "";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT alerta, fecha, hora FROM alertas_climaticas WHERE ciudad = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, ciudad);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String alerta = rs.getString("alerta");
                        String fecha = rs.getString("fecha");
                        String hora = rs.getString("hora");
                        historialAlertas.add(fecha + " " + hora + " - " + alerta);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            imprimirCuadro("Error al recuperar el historial de alertas.");
            historialAlertas.add("Error al recuperar el historial de alertas.");
        } finally {
            releaseMutex(clientName);
        }

        if (historialAlertas.isEmpty()) {
            imprimirCuadro("No hay alertas registradas para esta ciudad.");
            historialAlertas.add("No hay alertas registradas para esta ciudad.");
        } else {
            imprimirCuadro("Historial de alertas obtenido exitosamente.");
        }

        return historialAlertas;
    }
    
    @Override
    public ArrayList<String> generarAlertas(ClimaCiudad clima, String nombreCliente) throws RemoteException {
        String clientName = "Cliente " + clima.getCiudad();
        if (!requestMutex(clientName)) throw new RemoteException("Zona crítica ocupada.");

        System.out.println("\n================== Zona Crítica ==================");
        System.out.printf("   %s está generando alertas climáticas%n", clientName);
        System.out.println("==================================================\n");

        ArrayList<String> alertas = new ArrayList<>();

        try {
            double temp = clima.getTemperatura();
            int humedad = clima.getHumedad();
            String descripcion = clima.getDescripcion().toLowerCase();

            if (temp > 35) alertas.add("Alerta: Calor extremo");
            if (temp < 0) alertas.add("Alerta: Posible helada");
            if (humedad < 30) alertas.add("Alerta: Humedad baja - posible sequía");
            if (descripcion.contains("lluvia") || descripcion.contains("tormenta")) {
                alertas.add("Alerta: Lluvia o tormenta detectada");
            }

            if (alertas.isEmpty()) {
                alertas.add("Sin alertas climáticas.");
                imprimirCuadro("Sin alertas climáticas.");
            } else {
                for (String alerta : alertas) {
                    imprimirCuadro(alerta);
                }
            }

            guardarAlertasEnBD(clima, alertas);
            imprimirCuadro("Proceso de generación de alertas finalizado.");
            return alertas;
        } finally {
            releaseMutex(clientName);
        }
    }

    public void guardarAlertasEnBD(ClimaCiudad clima, ArrayList<String> alertas) {
        String DB_URL = "jdbc:mysql://localhost:3306/clima";
        String DB_USER = "root";
        String DB_PASSWORD = "";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String insert = "INSERT INTO alertas_climaticas (ciudad, alerta, fecha, hora) VALUES (?, ?, CURDATE(), CURTIME())";
            try (PreparedStatement stmt = conn.prepareStatement(insert)) {
                for (String alerta : alertas) {
                    stmt.setString(1, clima.getCiudad());
                    stmt.setString(2, alerta);
                    int filas = stmt.executeUpdate();

                    String mensaje;
                    if (filas > 0) {
                        mensaje = "Alerta '" + alerta + "' para ciudad '" + clima.getCiudad() + "' guardada exitosamente en la BD.";
                    } else {
                        mensaje = "No se pudo guardar la alerta '" + alerta + "' para ciudad '" + clima.getCiudad() + "'.";
                    }

                    imprimirCuadro(mensaje);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            String mensaje = "Error al guardar las alertas en la base de datos para la ciudad '" + clima.getCiudad() + "'.";
            imprimirCuadro(mensaje);
        }
    }

    private void imprimirCuadro(String mensaje) {
        int largo = mensaje.length();
        String bordeSuperior = "╔" + "═".repeat(largo + 2) + "╗";
        String lineaMensaje = "║ " + mensaje + " ║";
        String bordeInferior = "╚" + "═".repeat(largo + 2) + "╝";

        System.out.println(bordeSuperior);
        System.out.println(lineaMensaje);
        System.out.println(bordeInferior);
    }

    @Override
    public boolean agregarFavorito(String cliente, String ciudad) throws RemoteException {
        String clientName = "Cliente " + cliente;
        if (!requestMutex(clientName)) throw new RemoteException("Zona crítica ocupada.");

        System.out.println("\n================== Zona Crítica ==================");
        System.out.printf("   %s está agregando '%s' como ciudad favorita%n", clientName, ciudad);
        System.out.println("==================================================\n");

        try {
            // Simula retención de zona crítica por 15 segundos
            Thread.sleep(15000);

            String DB_URL = "jdbc:mysql://localhost:3306/clima";
            try (Connection conn = DriverManager.getConnection(DB_URL, "root", "")) {
                String check = "SELECT * FROM favoritos WHERE cliente = ? AND ciudad = ?";
                try (PreparedStatement ps = conn.prepareStatement(check)) {
                    ps.setString(1, cliente);
                    ps.setString(2, ciudad);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        imprimirCuadro(ciudad + " ya estaba en los favoritos de " + cliente);
                        return false;
                    }
                }

                String insert = "INSERT INTO favoritos (cliente, ciudad) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insert)) {
                    boolean resultado = ps.executeUpdate() > 0;
                    if (resultado) {
                        imprimirCuadro("Se agregó '" + ciudad + "' a favoritos de " + cliente);
                    }
                    return resultado;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                imprimirCuadro("Error al agregar favorito a la base de datos.");
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            imprimirCuadro("Interrupción durante la simulación de zona crítica.");
            return false;
        } finally {
            releaseMutex(clientName);
        }
    }

    @Override
    public boolean actualizarFavorito(String cliente, String ciudad) throws RemoteException {
        String clientName = "Cliente " + cliente;
        if (!requestMutex(clientName)) throw new RemoteException("Zona crítica ocupada.");

        System.out.println("\n================== Zona Crítica ==================");
        System.out.printf("   %s está actualizando su favorito: '%s'%n", clientName, ciudad);
        System.out.println("==================================================\n");

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/clima", "root", "")) {
            // Obtiene clima actualizado
            ClimaCiudad clima = consultarClima(cliente, ciudad);
            if (clima == null) {
                imprimirCuadro("No se pudo obtener el clima para actualizar.");
                return false;
            }

            // Actualiza el registro
            String update = "UPDATE favoritos SET temperatura = ?, humedad = ?, descripcion = ?, fecha = ?, hora = ? WHERE cliente = ? AND ciudad = ?";
            try (PreparedStatement ps = conn.prepareStatement(update)) {
                ps.setDouble(1, clima.getTemperatura());
                ps.setInt(2, clima.getHumedad());
                ps.setString(3, clima.getDescripcion());
                ps.setString(4, clima.getFechaConsulta());
                ps.setString(5, clima.getHoraConsulta());
                ps.setString(6, cliente);
                ps.setString(7, ciudad);
                boolean actualizado = ps.executeUpdate() > 0;
                if (actualizado) {
                    imprimirCuadro("Favorito actualizado para " + cliente + ": " + ciudad);
                } else {
                    imprimirCuadro("No se encontró el favorito para actualizar.");
                }
                return actualizado;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            imprimirCuadro("Error al actualizar favorito en la base de datos.");
            return false;
        } finally {
            releaseMutex(clientName);
        }
    }

    @Override
    public boolean eliminarFavorito(String cliente, String ciudad) throws RemoteException {
        String clientName = "Cliente " + cliente;
        if (!requestMutex(clientName)) throw new RemoteException("Zona crítica ocupada.");

        System.out.println("\n================== Zona Crítica ==================");
        System.out.printf("   %s está eliminando '%s' de sus favoritos%n", clientName, ciudad);
        System.out.println("==================================================\n");

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/clima", "root", "")) {
            String delete = "DELETE FROM favoritos WHERE cliente = ? AND ciudad = ?";
            try (PreparedStatement ps = conn.prepareStatement(delete)) {
                ps.setString(1, cliente);
                ps.setString(2, ciudad);
                boolean eliminado = ps.executeUpdate() > 0;
                if (eliminado) {
                    imprimirCuadro("Ciudad '" + ciudad + "' eliminada de favoritos de " + cliente);
                } else {
                    imprimirCuadro("No se encontró la ciudad '" + ciudad + "' en los favoritos de " + cliente);
                }
                return eliminado;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            imprimirCuadro("Error al eliminar favorito de la base de datos.");
            return false;
        } finally {
            releaseMutex(clientName);
        }
    }

    @Override
    public ArrayList<ClimaCiudad> obtenerFavoritos(String cliente) throws RemoteException {
        String clientName = "Cliente " + cliente;
        if (!requestMutex(clientName)) throw new RemoteException("Zona crítica ocupada.");

        System.out.println("\n================== Zona Crítica ==================");
        System.out.printf("   %s está consultando su lista de favoritos%n", clientName);
        System.out.println("==================================================\n");

        ArrayList<ClimaCiudad> favoritos = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/clima", "root", "")) {
            String select = "SELECT ciudad, temperatura, humedad, descripcion, fecha, hora FROM favoritos WHERE cliente = ?";
            try (PreparedStatement ps = conn.prepareStatement(select)) {
                ps.setString(1, cliente);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    favoritos.add(new ClimaCiudad(
                        rs.getString("ciudad"),
                        rs.getDouble("temperatura"),
                        rs.getInt("humedad"),
                        rs.getString("descripcion"),
                        rs.getString("fecha"),
                        rs.getString("hora")
                    ));
                }
                if (favoritos.isEmpty()) {
                    imprimirCuadro(cliente + " no tiene ciudades favoritas registradas.");
                } else {
                    imprimirCuadro("Se recuperaron " + favoritos.size() + " ciudades favoritas para " + cliente);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            imprimirCuadro("Error al recuperar favoritos desde la base de datos.");
        } finally {
            releaseMutex(clientName);
        }

        return favoritos;
    }

    @Override
    public ArrayList<String> getNombresFavoritos(String cliente) throws RemoteException {
        String clientName = "Cliente " + cliente;
        if (!requestMutex(clientName)) throw new RemoteException("Zona crítica ocupada.");

        System.out.println("\n================== Zona Crítica ==================");
        System.out.printf("   %s está consultando los nombres de sus favoritos%n", clientName);
        System.out.println("==================================================\n");

        ArrayList<String> nombres = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/clima", "root", "")) {
            String sql = "SELECT ciudad FROM favoritos WHERE cliente = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, cliente);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    nombres.add(rs.getString("ciudad"));
                }
                if (nombres.isEmpty()) {
                    imprimirCuadro(cliente + " no tiene nombres de ciudades favoritas registrados.");
                } else {
                    imprimirCuadro(nombres.size() + " ciudades favoritas recuperadas para " + cliente);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            imprimirCuadro("Error al recuperar nombres de favoritos.");
        } finally {
            releaseMutex(clientName);
        }

        return nombres;
    }


    @Override
    public int heartbeat() throws RemoteException {
        // System.out.println("Heartbeat recibido por el servidor: OK");
        return 1; // Retorna 1 para indicar que el servidor está activo
    }
}
