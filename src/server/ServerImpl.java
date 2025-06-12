package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
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
        UnicastRemoteObject.exportObject(this, 0);        
    }
    
    public void clienteConectado() throws RemoteException {
        System.out.println("\n================== AgroMonitoreo ==================");
        System.out.println("            El usuario ha iniciado sesión         ");
        System.out.println("===================================================\n");
    }

    private boolean requestMutex(String clientName) {
        try {
            System.out.println(clientName + " intentando acceder a zona crítica...");
            boolean acquired = lock.tryLock(60, TimeUnit.SECONDS);
            if (acquired) {
                System.out.println(clientName + " obtuvo el bloqueo.");
            }
            return acquired;
        } catch (InterruptedException e) {
            System.err.println("Error al adquirir mutex: " + e.getMessage());
            return false;
        }
    }

    private void releaseMutex(String clientName) {
        System.out.println(clientName + " liberó el bloqueo.");
        lock.unlock();
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
    public ArrayList<ClimaCiudad> getHistorial() throws RemoteException {
        String clientName = "Cliente Historial";
        if (!requestMutex(clientName)) throw new RemoteException("Zona crítica ocupada.");

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
            e.printStackTrace();
            System.out.println("╔══════════════════════════════════════════════╗");
            System.out.println("║  Error al recuperar historial desde la BD.   ║");
            System.out.println("╚══════════════════════════════════════════════╝");
        } finally {
            releaseMutex(clientName);
        }

        return historialBD;
    }

    // Método para consultar el clima de una ciudad
    @Override
    public ClimaCiudad consultarClima(String ciudad) throws RemoteException {
        String clientName = "Cliente " + ciudad;
        if (!requestMutex(clientName)) throw new RemoteException("Zona crítica ocupada.");

        try {
            String pais = "CL";  // Código de país para Chile

            // Codificar la ciudad para URL (maneja espacios, ñ, acentos, etc)
            String ciudadCodificada = URLEncoder.encode(ciudad, StandardCharsets.UTF_8.toString());

            // Construir la URL con la ciudad codificada
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

            // Deserializar la respuesta JSON con Jackson
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
            releaseMutex(clientName);
        }
    }

    @Override
    public ArrayList<String> obtenerHistorialAlertas(String ciudad) throws RemoteException {
        String clientName = "Cliente " + ciudad;
        if (!requestMutex(clientName)) throw new RemoteException("Zona crítica ocupada.");

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
        }

        return historialAlertas;
    }
    
    @Override
    public ArrayList<String> generarAlertas(ClimaCiudad clima) throws RemoteException {
        String clientName = "Cliente " + clima.getCiudad();
        if (!requestMutex(clientName)) throw new RemoteException("Zona crítica ocupada.");

        ArrayList<String> alertas = new ArrayList<>();

        try {
            double temp = clima.getTemperatura();
            int humedad = clima.getHumedad();
            String descripcion = clima.getDescripcion().toLowerCase();

            if (temp > 35) alertas.add("Alerta: Calor extremo");
            if (temp < 0) alertas.add("Alerta: Posible helada");
            if (humedad < 30) alertas.add("Alerta: Humedad baja - posible sequía");
            if (descripcion.contains("lluvia") || descripcion.contains("tormenta")) alertas.add("Alerta: Lluvia o tormenta detectada");

            if (alertas.isEmpty()) {
                alertas.add("Sin alertas climáticas.");
                imprimirCuadro("Sin alertas climáticas.");
            } else {
                for (String alerta : alertas) {
                    imprimirCuadro(alerta);
                }
            }

            guardarAlertasEnBD(clima, alertas);
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
                    if (rs.next()) return false;
                }

                String insert = "INSERT INTO favoritos (cliente, ciudad) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insert)) {
                    ps.setString(1, cliente);
                    ps.setString(2, ciudad);
                    return ps.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            releaseMutex(clientName);
        }
    }

    @Override
    public boolean actualizarFavorito(String cliente, String ciudad) throws RemoteException {
        String clientName = "Cliente " + cliente;
        if (!requestMutex(clientName)) throw new RemoteException("Zona crítica ocupada.");

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/clima", "root", "")) {
            // Obtiene clima actualizado
            ClimaCiudad clima = consultarClima(ciudad);
            if (clima == null) return false;

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
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            releaseMutex(clientName);
        }
    }

    @Override
    public boolean eliminarFavorito(String cliente, String ciudad) throws RemoteException {
        String clientName = "Cliente " + cliente;
        if (!requestMutex(clientName)) throw new RemoteException("Zona crítica ocupada.");

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/clima", "root", "")) {
            String delete = "DELETE FROM favoritos WHERE cliente = ? AND ciudad = ?";
            try (PreparedStatement ps = conn.prepareStatement(delete)) {
                ps.setString(1, cliente);
                ps.setString(2, ciudad);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            releaseMutex(clientName);
        }
    }

    @Override
    public ArrayList<ClimaCiudad> obtenerFavoritos(String cliente) throws RemoteException {
        String clientName = "Cliente " + cliente;
        if (!requestMutex(clientName)) throw new RemoteException("Zona crítica ocupada.");

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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseMutex(clientName);
        }
        return favoritos;
    }


    @Override
    public int heartbeat() throws RemoteException {
        // System.out.println("Heartbeat recibido por el servidor: OK");
        return 1; // Retorna 1 para indicar que el servidor está activo
    }
}
