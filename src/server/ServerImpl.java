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

public class ServerImpl implements InterfazDeServer {

    private final String API_KEY = "00699a1a425bce6e4a3daddf1b447487"; 
    private ArrayList<ClimaCiudad> historial = new ArrayList<>();
    private ArrayList<ClimaCiudad> bd_clima_copia = new ArrayList<>();
    
    public ServerImpl() throws RemoteException {
        ConectarBD();
        UnicastRemoteObject.exportObject(this, 0);        
    }
    
    public void clienteConectado() throws RemoteException {
        System.out.println("\n================== AgroMonitoreo ==================");
        System.out.println("            El usuario ha iniciado sesión         ");
        System.out.println("===================================================\n");
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
        ResultSet resultados = null;
        ResultSet rs = null;

        try {
            // Verificamos si la base de datos 'clima' existe
            String url = "jdbc:mysql://localhost:3306/";
            String username = "root";
            String password_BD = "";

            connection = DriverManager.getConnection(url, username, password_BD);
            query = connection.createStatement();

            // Verificar si la base de datos 'clima' existe
            rs = query.executeQuery("SHOW DATABASES LIKE 'clima'");
            if (!rs.next()) {
                System.out.println("Base de datos 'clima' no existe. Creando...");
                query.executeUpdate("CREATE DATABASE clima");
            }

            // Ahora nos conectamos a la base de datos 'clima'
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/clima", username, password_BD);
            query = connection.createStatement();

            // Verificar si la tabla 'clima_ciudad' existe
            rs = query.executeQuery("SHOW TABLES LIKE 'clima_ciudad'");
            if (!rs.next()) {
                System.out.println("Tabla 'clima_ciudad' no existe. Creando...");
                String createTableSQL = "CREATE TABLE clima_ciudad ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "ciudad VARCHAR(100), "
                    + "temperatura DOUBLE, "
                    + "humedad INT, "
                    + "descripcion VARCHAR(255), "
                    + "fecha DATE, "
                    + "hora TIME)";
                query.executeUpdate(createTableSQL);
                System.out.println("Tabla 'clima_ciudad' creada exitosamente.");
            }

            // Verificar si la tabla 'alertas_climaticas' existe
            rs = query.executeQuery("SHOW TABLES LIKE 'alertas_climaticas'");
            if (!rs.next()) {
                System.out.println("Tabla 'alertas_climaticas' no existe. Creando...");
                String createAlertasTableSQL = "CREATE TABLE alertas_climaticas ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "ciudad VARCHAR(100), "
                    + "alerta TEXT, "
                    + "fecha DATE, "
                    + "hora TIME)";
                query.executeUpdate(createAlertasTableSQL);
                System.out.println("Tabla 'alertas_climaticas' creada exitosamente.");
            }

            // Verifica si la conexión fue exitosa
            if (connection != null) {
            	if (connection != null) {
            	    System.out.println("╔══════════════════════════════════════════════╗");
            	    System.out.println("║                                              ║");
            	    System.out.println("║ Conexión exitosa a la base de datos 'clima'  ║");
            	    System.out.println("║                                              ║");
            	    System.out.println("╚══════════════════════════════════════════════╝");
            	}
            }

            // Recuperar los datos existentes de clima_ciudad
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
        }

        return historialBD;
    }

    // Método para consultar el clima de una ciudad
    @Override
    public ClimaCiudad consultarClima(String ciudad) throws RemoteException {
        try {
            String pais = "CL";  // Código de país para Chile
            
            // Codificar la ciudad para URL (maneja espacios, ñ, acentos, etc)
            String ciudadCodificada = URLEncoder.encode(ciudad, StandardCharsets.UTF_8.toString());

            // Construir la URL con la ciudad codificada
            String urlString = String.format(
                "https://api.openweathermap.org/data/2.5/weather?q=%s,%s&appid=%s&units=metric&lang=es",
                ciudadCodificada, pais, API_KEY);

            URL url = URI.create(urlString).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int status = conn.getResponseCode();
            if (status != 200) {
                if (status == 404) {
                    imprimirCuadro("Ciudad no encontrada: " + ciudad);
                    return null; // Ciudad no encontrada
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

            // Crear objeto ClimaCiudad con el nombre original de la ciudad (sin codificar)
            ClimaCiudad clima = new ClimaCiudad(ciudad, temp, humedad, descripcion, fechaConsulta, horaConsulta);

            historial.add(clima);
            guardarEnBaseDeDatos(clima); // Guarda automáticamente

            imprimirCuadro("Clima de la ciudad '" + ciudad + "' obtenido y guardado exitosamente.");
            return clima;

        } catch (Exception e) {
            imprimirCuadro("No se pudo obtener el clima de la API.");
            return null;
        }
    }

    @Override
    public ArrayList<String> obtenerHistorialAlertas(String ciudad) throws RemoteException {
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
        }
        if (historialAlertas.isEmpty()) {
            imprimirCuadro("No hay alertas registradas para esta ciudad.");
            historialAlertas.add("No hay alertas registradas para esta ciudad.");
        }
        return historialAlertas;
    }
    
    @Override
    public ArrayList<String> generarAlertas(ClimaCiudad clima) throws RemoteException {
        ArrayList<String> alertas = new ArrayList<>();

        double temp = clima.getTemperatura();
        int humedad = clima.getHumedad();
        String descripcion = clima.getDescripcion().toLowerCase();

        if (temp > 35) {
            alertas.add("Alerta: Calor extremo");
        }
        if (temp < 0) {
            alertas.add("Alerta: Posible helada");
        }
        if (humedad < 30) {
            alertas.add("Alerta: Humedad baja - posible sequía");
        }
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
        // Registrar en la base de datos
        guardarAlertasEnBD(clima, alertas);

        return alertas;
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
    public int heartbeat() throws RemoteException {
        System.out.println("Heartbeat recibido por el servidor: OK");
        return 1; // Retorna 1 para indicar que el servidor está activo
    }
}
