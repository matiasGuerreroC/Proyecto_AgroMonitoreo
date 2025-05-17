package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface InterfazDeServer extends Remote {
    public ArrayList<ClimaCiudad> getHistorial() throws RemoteException;
    ArrayList<String> obtenerHistorialAlertas(String ciudad) throws RemoteException;
    public ClimaCiudad consultarClima(String ciudad) throws RemoteException;
    public ArrayList<String> generarAlertas(String ciudad) throws RemoteException;
}
