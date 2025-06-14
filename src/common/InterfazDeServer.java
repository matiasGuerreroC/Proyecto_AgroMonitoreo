package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface InterfazDeServer extends Remote {
    // Obtiene todo el historial de consultas de un cliente
    public ArrayList<ClimaCiudad> getHistorial(String nombreCliente) throws RemoteException;

    // Obtiene el historial de alertas para una ciudad específica y un cliente
    public ArrayList<String> obtenerHistorialAlertas(String ciudad, String nombreCliente) throws RemoteException;

    // Consulta clima actual para ciudad y cliente
    public ClimaCiudad consultarClima(String ciudad, String nombreCliente) throws RemoteException;

    // Genera alertas para un clima dado, indicando el cliente
    public ArrayList<String> generarAlertas(ClimaCiudad clima, String nombreCliente) throws RemoteException;

    // Notifica que un cliente se ha conectado
    public void clienteConectado(String nombreCliente) throws RemoteException;

    // Notifica que un cliente se ha desconectado
    public void clienteDesconectado(String nombreCliente) throws RemoteException;

    // Heartbeat para detección de fallos
    public int heartbeat() throws RemoteException;

    // Gestión de favoritos
    public boolean agregarFavorito(String usuario, String ciudad, String nombreCliente) throws RemoteException;

    public boolean eliminarFavorito(String usuario, String ciudad, String nombreCliente) throws RemoteException;

    public ArrayList<ClimaCiudad> obtenerFavoritos(String usuario, String nombreCliente) throws RemoteException;

    public boolean actualizarFavorito(String usuario, String ciudad, String nombreCliente) throws RemoteException;

    public ArrayList<String> getNombresFavoritos(String usuario, String nombreCliente) throws RemoteException;
}

