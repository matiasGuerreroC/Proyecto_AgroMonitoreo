package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import common.ClimaCiudad;
import common.InterfazDeServer;

public class Client {
    private InterfazDeServer stub;
    private final String host = "localhost";
    private final int primaryPort = 1009;
    private final int backupPort = 1010;
    private boolean connectedToPrimary = true;
    private boolean running = true;

    public Client() throws RemoteException, NotBoundException {
        conectarServidorPrincipal();
        startHeartbeat(); // Inicia monitoreo de servidor
    }

    private void startHeartbeat() {
        new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                    stub.heartbeat(); // Llamada liviana
                } catch (RemoteException e) {
                    if (connectedToPrimary) {
                        System.err.println("Servidor principal caído. Cambiando a servidor de respaldo...");
                        cambiarAServidorRespaldo();
                    } else {
                        System.err.println("Servidor de respaldo también cayó. Terminando ejecución...");
                        System.exit(1);
                    }
                } catch (InterruptedException e) {
                    System.err.println("⛔ Heartbeat interrumpido: " + e.getMessage());
                }
            }
        }).start();
    }

    private void conectarServidorPrincipal() {
        stub = conectar(host, primaryPort, "server");
        if (stub == null) {
            System.err.println("No se pudo conectar al servidor principal. Intentando respaldo...");
            cambiarAServidorRespaldo();
        } else {
            connectedToPrimary = true;
            System.out.println("Conectado al servidor principal");
        }
    }

    private void cambiarAServidorRespaldo() {
        stub = conectar(host, backupPort, "server");
        if (stub == null) {
            System.err.println("No se pudo conectar al servidor de respaldo.");
            System.exit(1);
        } else {
            connectedToPrimary = false;
            System.out.println("Conectado al servidor de respaldo");
        }
    }

    private InterfazDeServer conectar(String host, int port, String nombre) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            return (InterfazDeServer) registry.lookup(nombre);
        } catch (Exception e) {
            System.err.println("❌ Error conectando a " + nombre + ": " + e);
            return null;
        }
    }

    // ---- Métodos de cliente que llaman al servidor ---- //

    public void clienteConectado() throws RemoteException {
        stub.clienteConectado();
    }

    public ArrayList<ClimaCiudad> getHistorial() throws RemoteException {
        return stub.getHistorial();
    }

    public ClimaCiudad consultarClima(String ciudad) throws RemoteException {
        return stub.consultarClima(ciudad);
    }

    public ArrayList<String> generarAlertas(ClimaCiudad clima) throws RemoteException {
        return stub.generarAlertas(clima);
    }

    public ArrayList<String> obtenerHistorialAlertas(String ciudad) throws RemoteException {
        return stub.obtenerHistorialAlertas(ciudad);
    }
}
