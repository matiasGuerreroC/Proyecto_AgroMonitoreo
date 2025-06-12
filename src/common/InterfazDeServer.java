package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface InterfazDeServer extends Remote {
    public ArrayList<ClimaCiudad> getHistorial() throws RemoteException;
    public ArrayList<String> obtenerHistorialAlertas(String ciudad) throws RemoteException;
    public ClimaCiudad consultarClima(String ciudad) throws RemoteException;
    public ArrayList<String> generarAlertas(ClimaCiudad clima) throws RemoteException;

    public void clienteConectado() throws RemoteException;

    // Heartbeat para detección de fallos
    public int heartbeat() throws RemoteException;

    public boolean agregarFavorito(String cliente, String ciudad) throws RemoteException;   // INSERT
    public boolean eliminarFavorito(String cliente, String ciudad) throws RemoteException; // DELETE
    public ArrayList<ClimaCiudad> obtenerFavoritos(String cliente) throws RemoteException; // SELECT
    public boolean actualizarFavorito(String cliente, String ciudad) throws RemoteException; // UPDATE

}
