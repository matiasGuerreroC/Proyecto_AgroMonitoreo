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
    private String nombreCliente;

    public Client(String nombreCliente) throws RemoteException, NotBoundException {
        this.nombreCliente = nombreCliente;
        conectarServidorPrincipal();
        startHeartbeat();
    }

    private void startHeartbeat() {
        new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                    stub.heartbeat(); // Llamada liviana
                } catch (RemoteException e) {
                    if (connectedToPrimary) {
                        System.err.println("\nServidor principal caído. Cambiando a servidor de respaldo...");
                        cambiarAServidorRespaldo();
                    } else {
                        System.err.println("\nServidor de respaldo también cayó. Terminando ejecución...");
                        System.exit(1);
                    }
                } catch (InterruptedException e) {
                    System.err.println("Heartbeat interrumpido: " + e.getMessage());
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
            try {
                stub.clienteConectado(nombreCliente);  // <-- AÑADE ESTA LÍNEA
            } catch (RemoteException e) {
                System.err.println("Error al notificar conexión al servidor de respaldo: " + e.getMessage());
            }
        }
    }

    private InterfazDeServer conectar(String host, int port, String nombre) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            return (InterfazDeServer) registry.lookup(nombre);
        } catch (Exception e) {
            System.err.println("Error conectando a " + nombre + ": " + e);
            return null;
        }
    }

    // ---- Métodos de cliente que llaman al servidor ---- //

    public void clienteConectado(String nombreCliente) throws RemoteException {
        stub.clienteConectado(nombreCliente);
    }
    
    public void clienteDesconectado(String nombreCliente) throws RemoteException {
        stub.clienteDesconectado(nombreCliente);
    }

    public ArrayList<ClimaCiudad> getHistorial(String nombreCliente) throws RemoteException {
        return stub.getHistorial(nombreCliente);
    }

    public ClimaCiudad consultarClima(String ciudad, String nombreCliente) throws RemoteException {
        return stub.consultarClima(ciudad, nombreCliente);
    }

    public ArrayList<String> generarAlertas(ClimaCiudad clima, String nombreCliente) throws RemoteException {
        return stub.generarAlertas(clima, nombreCliente);
    }

    public ArrayList<String> obtenerHistorialAlertas(String ciudad, String nombreCliente) throws RemoteException {
        return stub.obtenerHistorialAlertas(ciudad, nombreCliente);
    }

    public boolean agregarFavorito(String usuario, String ciudad, String nombreCliente) throws RemoteException {
        return stub.agregarFavorito(usuario, ciudad, nombreCliente);
    }

    public boolean eliminarFavorito(String usuario, String ciudad, String nombreCliente) throws RemoteException {
        return stub.eliminarFavorito(usuario, ciudad, nombreCliente);
    }

    public ArrayList<ClimaCiudad> obtenerFavoritos(String usuario, String nombreCliente) throws RemoteException {
        return stub.obtenerFavoritos(usuario, nombreCliente);
    }

    public boolean actualizarFavorito(String usuario, String ciudad, String nombreCliente) throws RemoteException {
        return stub.actualizarFavorito(usuario, ciudad, nombreCliente);
    }

    public ArrayList<String> getNombresFavoritos(String usuario, String nombreCliente) throws RemoteException {
        return stub.getNombresFavoritos(usuario, nombreCliente);
    }
}
