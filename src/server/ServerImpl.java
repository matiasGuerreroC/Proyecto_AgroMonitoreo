package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        String url = "jdbc:mysql://localhost:3306/clima";
        String username = "root";
        String password_BD = "";
        PreparedStatement ps = null;
        

        try{
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

            		ps.executeUpdate();

            //con.close();
                
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error al guardar en la base de datos.");
        }
    }
    
    public void ConectarBD( ) {
    	String driver="com.mysql.jdbc.Driver";
    	Connection connection= null;
    	Statement query = null;
    	//PreparedStatement test = null;
    	ResultSet resultados = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
    	
    	try {
    		String url = "jdbc:mysql://localhost:3306/clima";
    		String username = "root";
    		String password_BD = "";
    		
    		connection = DriverManager.getConnection(url, username, password_BD);
    		
    		//todos los metodos
    		query = connection.createStatement();
    		String sql = "SELECT * FROM clima_ciudad";
    		//Insert para agregar datos a la bd, PreparedStatemen
    		
    		
    		resultados = query.executeQuery(sql);
    		while(resultados.next()) {
    			int id = resultados.getInt("id");
    			String ciudad = resultados.getString("ciudad");
    			double temperatura = resultados.getDouble("temperatura");
    			int humedad = resultados.getInt("humedad");
    			String descripcion = resultados.getString("descripcion");
    			String fecha = resultados.getString("fecha");
    			String hora = resultados.getString("hora");
    			
    			ClimaCiudad newClimaCiudad = new ClimaCiudad(ciudad, temperatura, humedad, descripcion, fecha, hora); 

    			bd_clima_copia.add(newClimaCiudad);
    			
    			//System.out.println("ID: " + id + ", Ciudad: " + ciudad + ", Temp: " + temperatura + "°C, Humedad: " + humedad + "%, Descripción: " + descripcion + ", Fecha: " + fecha + ", Hora: " + hora);

    	}
    		
    		
    		//connection.close();
    		} catch(SQLException e) {
    		e.printStackTrace();
    		System.out.println("No se pudo conectar a la base de datos");
    		
    		
    	}
    }
    
    @Override
    public ArrayList<ClimaCiudad> getHistorial() throws RemoteException {
        return null;
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
