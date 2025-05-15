package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

import common.ClimaCiudad;

public class RunClient {
    public static void main(String[] args) {
        try {
            Client client = new Client();
            Scanner scanner = new Scanner(System.in);
            int opcion = 0;

            while (opcion != 4) {
                System.out.println("===== MENÚ CLIMA =====");
                System.out.println("1. Consultar clima por ciudad");
                System.out.println("2. Alerta Climatica por ciudad");
                System.out.println("3. Ver historial de consultas");
                System.out.println("4. Salir");
                System.out.print("Seleccione una opción: ");
                opcion = scanner.nextInt();
                scanner.nextLine(); // limpiar buffer
                String ciudad;

                switch (opcion) {
                    case 1:
                    	System.out.print("Ingrese la ciudad: ");
                    	ciudad = scanner.nextLine();

                    	ClimaCiudad clima = client.consultarClima(ciudad);

                    	if (clima == null) {
                    	    System.out.println("Ciudad no encontrada. Verifique el nombre e intente nuevamente.");
                    	} else {
                    	    System.out.println("Clima actual: " + clima);
                    	}
                        break;

                    case 2:
                    	System.out.print("Ingrese ciudad para revisar alertas climáticas: ");
                    	ciudad = scanner.nextLine();

                    	ArrayList<String> alertas = client.generarAlertas(ciudad);
                    	System.out.println("Alertas para " + ciudad + ":");
                    	for (String alerta : alertas) {
                    	    System.out.println("- " + alerta);
                    	}
                    	break;
                    case 3:
                        ArrayList<ClimaCiudad> historial = client.getHistorial();

                        if (historial.isEmpty()) {
                            System.out.println("No hay consultas registradas aún.");
                        } else {
                            System.out.println("===== HISTORIAL DE CONSULTAS =====");
                            for (ClimaCiudad registro : historial) {
                                System.out.println(registro);
                            }
                        }
                        break;
                    case 4:
                        System.out.println("Saliendo...");
                        
                        break;

                    default:
                        System.out.println("Opción inválida.");
                        break;
                }

                System.out.println();
            }

            scanner.close();
        } catch (RemoteException | NotBoundException e) {
            System.out.println("Error al conectar con el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
