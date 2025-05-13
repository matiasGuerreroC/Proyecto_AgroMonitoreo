package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;
import server.ClimaAPIResponse; // Asegúrate de importar tu clase mapeadora
import common.ClimaCiudad;
import common.InterfazDeServer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerImpl implements InterfazDeServer {

    private final String API_KEY = "00699a1a425bce6e4a3daddf1b447487"; // <-- Reemplaza por tu clave
    private ArrayList<ClimaCiudad> historial = new ArrayList<>();
    private ArrayList<ClimaCiudad> bd_clima_copia = new ArrayList<>();
    
    public ServerImpl() throws RemoteException {
        ConectarBD();
        UnicastRemoteObject.exportObject(this, 0);        
    }
    
    private void guardarEnBaseDeDatos(ClimaCiudad clima) {
        String url = "jdbc:postgresql://ep-restless-feather-a4yytmir-pooler.us-east-1.aws.neon.tech/clima?user=neondb_owner&password=npg_dgtFaq29TzHK&sslmode=require";

        try (Connection con = DriverManager.getConnection(url)) {
            String sql = "INSERT INTO clima_ciudad (ciudad, temperatura, humedad, descripcion, fecha, hora) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, clima.getCiudad());
                ps.setDouble(2, clima.getTemperatura());
                ps.setInt(3, clima.getHumedad());
                ps.setString(4, clima.getDescripcion());
                ps.setDate(5, Date.valueOf(clima.getFechaConsulta()));
                ps.setTime(6, Time.valueOf(clima.getHoraConsulta()));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error al guardar en la base de datos PostgreSQL.");
        }
    }

    public void ConectarBD() {
        String url = "jdbc:postgresql://ep-restless-feather-a4yytmir-pooler.us-east-1.aws.neon.tech/clima?user=neondb_owner&password=npg_dgtFaq29TzHK&sslmode=require";
        Connection connection = null;
        Statement stmt = null;
        ResultSet resultados = null;

        try {
            connection = DriverManager.getConnection(url);
            stmt = connection.createStatement();

            // Crear tabla si no existe
            String createTableSQL = "CREATE TABLE IF NOT EXISTS clima_ciudad ("
                    + "id SERIAL PRIMARY KEY, "
                    + "ciudad VARCHAR(100), "
                    + "temperatura DOUBLE PRECISION, "
                    + "humedad INT, "
                    + "descripcion VARCHAR(255), "
                    + "fecha DATE, "
                    + "hora TIME)";
            stmt.executeUpdate(createTableSQL);
            System.out.println("✅ Tabla 'clima_ciudad' verificada/creada.");

            // Recuperar los datos existentes
            String sql = "SELECT * FROM clima_ciudad";
            resultados = stmt.executeQuery(sql);
            while (resultados.next()) {
                String ciudad = resultados.getString("ciudad");
                double temperatura = resultados.getDouble("temperatura");
                int humedad = resultados.getInt("humedad");
                String descripcion = resultados.getString("descripcion");
                String fecha = resultados.getDate("fecha").toString();
                String hora = resultados.getTime("hora").toString();

                ClimaCiudad newClimaCiudad = new ClimaCiudad(ciudad, temperatura, humedad, descripcion, fecha, hora);
                bd_clima_copia.add(newClimaCiudad);
            }

            System.out.println("✅ Conexión y carga desde PostgreSQL exitosa.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("❌ No se pudo conectar o consultar la base de datos en Neon.");
        }
    }

    @Override
    public ArrayList<ClimaCiudad> getHistorial() throws RemoteException {
        return bd_clima_copia;
    }

    @Override
    public ClimaCiudad consultarClima(String ciudad) throws RemoteException {
        try {
            String pais = "CL";  // Código de país para Chile
            String urlString = String.format(
                "https://api.openweathermap.org/data/2.5/weather?q=%s,%s&appid=%s&units=metric&lang=es",
                ciudad, pais, API_KEY);

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int status = conn.getResponseCode();
            if (status != 200) {
                if (status == 404) {
                    return null; // Ciudad no encontrada
                } else {
                    System.err.println("Error en la conexión: Código " + status);
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

            // 🔁 Aquí usamos Jackson para deserializar la respuesta JSON
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
            guardarEnBaseDeDatos(clima); // <-- Aquí se guarda automáticamente
            
            return clima;

        } catch (Exception e) {
            System.err.println("No se pudo obtener el clima de la API: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public ArrayList<String> generarAlertas(String ciudad) throws RemoteException {
        ArrayList<String> alertas = new ArrayList<>();

        ClimaCiudad clima = consultarClima(ciudad); // Usa el método actual
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
        }

        return alertas;
    }
}
