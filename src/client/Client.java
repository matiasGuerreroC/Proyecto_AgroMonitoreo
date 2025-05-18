package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import common.ClimaCiudad;
import common.InterfazDeServer;

public class Client {
    private InterfazDeServer server;

    public Client() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("localhost", 1009);
        server = (InterfazDeServer) registry.lookup("server");
    }

    public void clienteConectado() throws RemoteException {
        server.clienteConectado();
    }

    public ArrayList<ClimaCiudad> getHistorial() throws RemoteException {
        return server.getHistorial();
    }

    public ClimaCiudad consultarClima(String ciudad) throws RemoteException {
        return server.consultarClima(ciudad);
    }
    
    public ArrayList<String> generarAlertas(ClimaCiudad clima) throws RemoteException {
        return new ArrayList<>(server.generarAlertas(clima));
    }

    public ArrayList<String> obtenerHistorialAlertas(String ciudad) throws RemoteException {
        return new ArrayList<>(server.obtenerHistorialAlertas(ciudad));
    }
}
package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import common.ClimaCiudad;
import common.InterfazDeServer;

public class Client {
    private InterfazDeServer server;

    public Client() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("localhost", 1009);
        server = (InterfazDeServer) registry.lookup("server");
    }

    public ArrayList<ClimaCiudad> getHistorial() throws RemoteException {
        return server.getHistorial();
    }

    public ClimaCiudad consultarClima(String ciudad) throws RemoteException {
        return server.consultarClima(ciudad);
    }
    
    public ArrayList<String> generarAlertas(ClimaCiudad clima) throws RemoteException {
        return new ArrayList<>(server.generarAlertas(clima));
    }

    public ArrayList<String> obtenerHistorialAlertas(String ciudad) throws RemoteException {
        return new ArrayList<>(server.obtenerHistorialAlertas(ciudad));
    }
}
