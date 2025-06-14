package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.InterfazDeServer;

public class RunServerRespaldo {
    public static void main(String[] args) {
        try {
            ServerImpl serverImpl = new ServerImpl();

            // Exportar el objeto remoto
            InterfazDeServer stub = (InterfazDeServer) UnicastRemoteObject.exportObject(serverImpl, 0);

            // Crear el registro RMI en el puerto 1010 (distinto al principal)
            Registry registry = LocateRegistry.createRegistry(1010);

            // Registrar el servidor de respaldo con un nombre único
            registry.rebind("server", stub);

            // Mensaje bonito de inicio
            System.out.println("╔══════════════════════════════════════════════╗");
            System.out.println("║                                              ║");
            System.out.println("║     Servidor de Clima de RESPALDO Activo     ║");
            System.out.println("║             Puerto en uso: 1010              ║");
            System.out.println("║                                              ║");
            System.out.println("╚══════════════════════════════════════════════╝");
        } catch (RemoteException e) {
            System.err.println("Error al iniciar el servidor de respaldo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
