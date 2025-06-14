package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import common.InterfazDeServer;

public class RunServer {
    public static void main(String[] args) {
        try {
            ServerImpl serverImpl = new ServerImpl();

            // Exportar el objeto remoto
            InterfazDeServer stub = (InterfazDeServer) UnicastRemoteObject.exportObject(serverImpl, 0);

            // Crear el registro RMI en el puerto 1009
            Registry registry = LocateRegistry.createRegistry(1009);

            // Registrar el objeto exportado
            registry.rebind("server", stub);

            // Mensaje bonito de inicio
            System.out.println("╔══════════════════════════════════════════════╗");
            System.out.println("║                                              ║");
            System.out.println("║      Servidor de Clima PRINCIPAL Activo      ║");
            System.out.println("║             Puerto en uso: 1009              ║");
            System.out.println("║                                              ║");
            System.out.println("╚══════════════════════════════════════════════╝");
        } catch (RemoteException e) {
            System.err.println("Error al iniciar el servidor principal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
