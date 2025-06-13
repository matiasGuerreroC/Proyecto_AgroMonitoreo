package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import common.InterfazDeServer;

public class RunServerRespaldo {
    public static void main(String[] args) {
        try {
            InterfazDeServer backupServer = new ServerImpl();

            // Crear el registro RMI en el puerto 1010 (distinto al principal)
            Registry registry = LocateRegistry.createRegistry(1010);

            // Registrar el servidor de respaldo con un nombre único
            registry.bind("server", backupServer);

            // Mensaje de confirmación
            System.out.println("╔══════════════════════════════════════════════╗");
            System.out.println("║                                              ║");
            System.out.println("║     Servidor de Clima de RESPALDO Activo     ║");
            System.out.println("║             Puerto en uso: 1010              ║");
            System.out.println("║                                              ║");
            System.out.println("╚══════════════════════════════════════════════╝");
        } catch (Exception e) {
            System.err.println("Error al iniciar el servidor de respaldo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}