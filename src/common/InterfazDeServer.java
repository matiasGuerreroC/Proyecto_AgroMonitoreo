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

    public int heartbeat() throws RemoteException;
}
