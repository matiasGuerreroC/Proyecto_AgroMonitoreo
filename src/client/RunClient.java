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
                System.out.println("3. Ver historial");
                System.out.println("4. Salir");
                System.out.print("Seleccione una opción: ");

                String input = scanner.nextLine(); // lee como string

                try {
                    opcion = Integer.parseInt(input); // intenta convertir a número
                } catch (NumberFormatException e) {
                    System.out.println("Entrada inválida. Por favor, ingrese un número del 1 al 4.");
                    System.out.println();
                    continue; // vuelve a mostrar el menú
                }

                switch (opcion) {
                    case 1:
                        System.out.print("Ingrese la ciudad: ");
                        String ciudad = scanner.nextLine();
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

                        // Obtener el clima actual
                        ClimaCiudad climaActual = client.consultarClima(ciudad);

                        // Mostrar resumen del clima
                        System.out.println("\n--- Clima actual en " + ciudad + " ---");
                        System.out.println("Temperatura: " + climaActual.getTemperatura() + "°C");
                        System.out.println("Humedad: " + climaActual.getHumedad() + "%");
                        System.out.println("Descripción: " + climaActual.getDescripcion());
                        System.out.println("Fecha: " + climaActual.getFechaConsulta());
                        System.out.println("Hora: " + climaActual.getHoraConsulta());

                        // Mostrar alertas asociadas
                        ArrayList<String> alertas = client.generarAlertas(ciudad);
                        System.out.println("\n--- Alertas Climáticas ---");
                        for (String alerta : alertas) {
                            System.out.println("- " + alerta);
                        }
                        System.out.println(); // Línea en blanco final
                        break;

                    case 3:
                        System.out.println("¿Qué historial desea ver?");
                        System.out.println("1. Historial de consultas");
                        System.out.println("2. Historial de alertas climáticas");
                        System.out.print("Seleccione una opción: ");
                        int opcionHistorial = Integer.parseInt(scanner.nextLine());

                        switch (opcionHistorial) {
                            case 1:
                                ArrayList<ClimaCiudad> historial = client.getHistorial();
                                if (historial.isEmpty()) {
                                    System.out.println("No hay consultas previas registradas.");
                                } else {
                                    System.out.println("\n--- Historial de consultas climáticas ---");
                                    for (ClimaCiudad registro : historial) {
                                        System.out.println("- " + registro);
                                    }
                                }
                                break;
                            case 2:
                                System.out.print("Ingrese ciudad para ver historial de alertas: ");
                                ciudad = scanner.nextLine();
                                ArrayList<String> historialAlertas = client.obtenerHistorialAlertas(ciudad);
                                System.out.println("\n--- Historial de alertas climáticas para " + ciudad + " ---");
                                for (String alerta : historialAlertas) {
                                    System.out.println("- " + alerta);
                                }
                                break;
                            default:
                                System.out.println("Opción no válida.");
                        }
                        System.out.println(); // Línea en blanco final
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
