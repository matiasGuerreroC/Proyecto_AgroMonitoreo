package server;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import common.InterfazDeServer;

public class RunServer {
    public static void main(String[] args) throws RemoteException, AlreadyBoundException {
        InterfazDeServer server = new ServerImpl();
        Registry registry = LocateRegistry.createRegistry(1009);
        registry.bind("server", server);

        // Mensaje de inicio
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║                                              ║");
        System.out.println("║   Servidor de Clima Iniciado correctamente   ║");
        System.out.println("║            Puerto en uso: 1009               ║");
        System.out.println("║                                              ║");
        System.out.println("╚══════════════════════════════════════════════╝");
    }
}
