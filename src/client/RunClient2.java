package client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;

import common.ClimaCiudad;

public class RunClient2 {
    public static void main(String[] args) {
        try {
            Client client = new Client();
            client.clienteConectado();
            Scanner scanner = new Scanner(System.in);
            int opcion = 0;

            System.out.println("╔═══════════════════════════════════════════════╗");
            System.out.println("║   ¡Bienvenid@ a AgroMonitoreo [CLIENTE 2]!    ║");
            System.out.println("║   Sistema de monitoreo climático distribuido  ║");
            System.out.println("╚═══════════════════════════════════════════════╝");
            System.out.println("Presione Enter para continuar...");
            scanner.nextLine();

            while (opcion != 4) {
                System.out.println("\n╔══════════════════════╗");
                System.out.println("║      MENÚ CLIENTE 2  ║");
                System.out.println("╚══════════════════════╝");
                System.out.println("1. Consultar clima por ciudad");
                System.out.println("2. Alerta climática por ciudad");
                System.out.println("3. Ver historial");
                System.out.println("4. Salir");
                System.out.print("Seleccione una opción: ");

                String input = scanner.nextLine();
                try {
                    opcion = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Entrada inválida. Por favor, ingrese un número del 1 al 4.");
                    continue;
                }

                switch (opcion) {
                    case 1:
                        System.out.print("Ingrese la ciudad: ");
                        String ciudad = scanner.nextLine();
                        ClimaCiudad clima = client.consultarClima(ciudad);
                        if (clima != null) {
                            System.out.println("\nCLIMA ACTUAL EN " + ciudad.toUpperCase());
                            System.out.println("-----------------------------");
                            System.out.println("Temperatura: " + clima.getTemperatura() + "°C");
                            System.out.println("Humedad: " + clima.getHumedad() + "%");
                            System.out.println("Descripción: " + clima.getDescripcion());
                            System.out.println("Fecha: " + clima.getFechaConsulta());
                            System.out.println("Hora: " + clima.getHoraConsulta());
                        } else {
                            System.out.println("Ciudad no encontrada.");
                        }
                        break;

                    case 2:
                        System.out.print("Ingrese ciudad para revisar alertas climáticas: ");
                        ciudad = scanner.nextLine();
                        ClimaCiudad climaActual = client.consultarClima(ciudad);
                        if (climaActual != null) {
                            ArrayList<String> alertas = client.generarAlertas(climaActual);
                            System.out.println("\nALERTAS CLIMÁTICAS PARA " + ciudad.toUpperCase());
                            System.out.println("-----------------------------");
                            for (String alerta : alertas) {
                                System.out.println("- " + alerta);
                            }
                        } else {
                            System.out.println("Ciudad no encontrada.");
                        }
                        break;

                    case 3:
                        System.out.println("\nSeleccione historial a ver:");
                        System.out.println("1. Historial de consultas");
                        System.out.println("2. Historial de alertas");
                        System.out.print("Opción: ");
                        int h = Integer.parseInt(scanner.nextLine());

                        if (h == 1) {
                            ArrayList<ClimaCiudad> historial = client.getHistorial();
                            if (historial.isEmpty()) {
                                System.out.println("No hay consultas previas registradas.");
                            } else {
                                for (ClimaCiudad registro : historial) {
                                    System.out.println("- " + registro);
                                }
                            }
                        } else if (h == 2) {
                            System.out.print("Ingrese ciudad para ver historial de alertas: ");
                            String c = scanner.nextLine();
                            ArrayList<String> alertas = client.obtenerHistorialAlertas(c);
                            for (String alerta : alertas) {
                                System.out.println("- " + alerta);
                            }
                        } else {
                            System.out.println("Opción inválida.");
                        }
                        break;

                    case 4:
                        System.out.println("Saliendo de Cliente 2...");
                        break;

                    default:
                        System.out.println("Opción inválida.");
                        break;
                }
            }

            scanner.close();
        } catch (RemoteException | NotBoundException e) {
            System.out.println("Error al conectar con el servidor: " + e.getMessage());
        }
    }
}
