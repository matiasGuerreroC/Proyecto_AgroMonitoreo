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
            String nombreCliente = "Cliente 2";  // Nombre fijo para identificar cliente 2
            client.clienteConectado(nombreCliente);  // Informar al servidor que cliente 2 se conectó
            
            Scanner scanner = new Scanner(System.in);
            int opcion = 0;

            // Mensaje de bienvenida
            System.out.println("╔════════════════════════════════════════════════════╗");
            System.out.println("║        ¡Bienvenid@ al Cliente 2 de                 ║");
            System.out.println("║               AgroMonitoreo :)!                    ║");
            System.out.println("║  Sistema de monitoreo climático para campos        ║");
            System.out.println("║        agrícolas distribuidos                      ║");
            System.out.println("╚════════════════════════════════════════════════════╝");
            System.out.println("Presione Enter para continuar...");
            scanner.nextLine();

            while (opcion != 5) {
                System.out.println("\n╔═════════════════════════════╗");
                System.out.println("║        MENÚ CLIMA           ║");
                System.out.println("╚═════════════════════════════╝");
                System.out.println("1. Consultar clima por ciudad");
                System.out.println("2. Alerta climática por ciudad");
                System.out.println("3. Ver historial");
                System.out.println("4. Gestionar ciudades favoritas");
                System.out.println("5. Salir");
                System.out.print("Seleccione una opción: ");

                String input = scanner.nextLine();
                try {
                    opcion = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Entrada inválida. Por favor, ingrese un número del 1 al 5.");
                    continue;
                }

                switch (opcion) {
                    case 1:
                        String respuestaConsulta;
                        do {
                            System.out.print("Ingrese la ciudad: ");
                            String ciudad = scanner.nextLine();
                            ClimaCiudad clima = client.consultarClima(ciudad, nombreCliente);
                            if (clima == null) {
                                System.out.println("Ciudad no encontrada. Verifique el nombre e intente nuevamente.");
                            } else {
                                System.out.println("\nCLIMA ACTUAL EN " + ciudad.toUpperCase());
                                System.out.println("-----------------------------");
                                System.out.println("Temperatura: " + clima.getTemperatura() + "°C");
                                System.out.println("Humedad: " + clima.getHumedad() + "%");
                                System.out.println("Descripción: " + clima.getDescripcion());
                                System.out.println("Fecha: " + clima.getFechaConsulta());
                                System.out.println("Hora: " + clima.getHoraConsulta());
                            }
                            System.out.print("¿Desea consultar otra ciudad? (s/n): ");
                            respuestaConsulta = scanner.nextLine();
                        } while (respuestaConsulta.equalsIgnoreCase("s"));
                        break;

                    case 2:
                        String respuestaAlerta;
                        do {
                            System.out.print("Ingrese ciudad para revisar alertas climáticas: ");
                            String ciudad = scanner.nextLine();
                            ClimaCiudad climaActual = client.consultarClima(ciudad, nombreCliente);
                            if (climaActual == null) {
                                System.out.println("Ciudad no encontrada. Verifique el nombre e intente nuevamente.");
                            } else {
                                System.out.println("\nCLIMA ACTUAL EN " + ciudad.toUpperCase());
                                System.out.println("-----------------------------");
                                System.out.println("Temperatura: " + climaActual.getTemperatura() + "°C");
                                System.out.println("Humedad: " + climaActual.getHumedad() + "%");
                                System.out.println("Descripción: " + climaActual.getDescripcion());
                                System.out.println("Fecha: " + climaActual.getFechaConsulta());
                                System.out.println("Hora: " + climaActual.getHoraConsulta());

                                ArrayList<String> alertas = client.generarAlertas(climaActual, nombreCliente);
                                System.out.println("\nALERTAS CLIMÁTICAS");
                                System.out.println("-----------------------------");
                                if (alertas.isEmpty()) {
                                    System.out.println("No hay alertas climáticas en este momento.");
                                } else {
                                    for (String alerta : alertas) {
                                        System.out.println("- " + alerta);
                                    }
                                }
                            }
                            System.out.print("¿Desea revisar otra ciudad? (s/n): ");
                            respuestaAlerta = scanner.nextLine();
                        } while (respuestaAlerta.equalsIgnoreCase("s"));
                        break;

                    case 3:
                        boolean verOtroHistorial = true;
                        while (verOtroHistorial) {
                            System.out.println("\nHISTORIAL DISPONIBLE");
                            System.out.println("-----------------------------");
                            System.out.println("1. Historial de consultas");
                            System.out.println("2. Historial de alertas climáticas");
                            System.out.print("Seleccione una opción: ");
                            int opcionHistorial;
                            try {
                                opcionHistorial = Integer.parseInt(scanner.nextLine());
                            } catch (NumberFormatException e) {
                                System.out.println("Opción inválida.");
                                continue;
                            }

                            switch (opcionHistorial) {
                                case 1:
                                    ArrayList<ClimaCiudad> historial = client.getHistorial(nombreCliente);
                                    if (historial.isEmpty()) {
                                        System.out.println("No hay consultas previas registradas.");
                                    } else {
                                        System.out.println("\nHISTORIAL DE CONSULTAS CLIMÁTICAS");
                                        System.out.println("-----------------------------");
                                        for (ClimaCiudad registro : historial) {
                                            System.out.println("- " + registro);
                                        }
                                    }
                                    break;

                                case 2:
                                    System.out.print("Ingrese ciudad para ver historial de alertas: ");
                                    String ciudad = scanner.nextLine();
                                    ArrayList<String> historialAlertas = client.obtenerHistorialAlertas(ciudad, nombreCliente);
                                    if (historialAlertas.isEmpty()) {
                                        System.out.println("No hay alertas previas registradas para " + ciudad + ".");
                                    } else {
                                        System.out.println("\nHISTORIAL DE ALERTAS CLIMÁTICAS PARA " + ciudad.toUpperCase());
                                        System.out.println("-----------------------------");
                                        for (String alerta : historialAlertas) {
                                            System.out.println("- " + alerta);
                                        }
                                    }
                                    break;

                                default:
                                    System.out.println("Opción no válida.");
                                    continue;
                            }

                            System.out.print("\n¿Desea ver otro historial? (s/n): ");
                            String respuestaHistorial = scanner.nextLine();
                            if (!respuestaHistorial.equalsIgnoreCase("s")) {
                                verOtroHistorial = false;
                            }
                        }
                        break;

                    case 4:
                        System.out.print("Ingrese su nombre usuario: ");
                        String usuario = scanner.nextLine();

                        ArrayList<String> nombresFavoritos = client.getNombresFavoritos(usuario, nombreCliente); // Nuevo método
                        for (String ciudadFav : nombresFavoritos) {
                            client.actualizarFavorito(usuario, ciudadFav, nombreCliente);
                        }

                        ArrayList<ClimaCiudad> favoritas = client.obtenerFavoritos(usuario, nombreCliente);
                        if (favoritas.isEmpty()) {
                            System.out.println("No tienes ciudades favoritas registradas.");
                        } else {
                            System.out.println("\nTUS CIUDADES FAVORITAS (actualizadas)");
                            System.out.println("-------------------------------------");
                            for (ClimaCiudad climaFav : favoritas) {
                                System.out.println("Ciudad: " + climaFav.getCiudad());
                                System.out.println("Temperatura: " + climaFav.getTemperatura() + "°C");
                                System.out.println("Humedad: " + climaFav.getHumedad() + "%");
                                System.out.println("Descripción: " + climaFav.getDescripcion());
                                System.out.println("Fecha: " + climaFav.getFechaConsulta());
                                System.out.println("Hora: " + climaFav.getHoraConsulta());
                                System.out.println("-------------------------------------");
                            }
                        }

                        boolean gestionar = true;
                        while (gestionar) {
                            System.out.println("\nOPCIONES DE FAVORITOS");
                            System.out.println("1. Agregar nueva ciudad favorita");
                            System.out.println("2. Eliminar ciudad favorita");
                            System.out.println("3. Volver al menú principal");
                            System.out.print("Seleccione una opción: ");
                            String inputFavoritos = scanner.nextLine();

                            switch (inputFavoritos) {
                                case "1":
                                    System.out.print("Ingrese el nombre de la ciudad a agregar: ");
                                    String ciudadAgregar = scanner.nextLine();
                                    boolean agregado = client.agregarFavorito(usuario, ciudadAgregar, nombreCliente);
                                    if (agregado) {
                                        System.out.println("Ciudad agregada correctamente a favoritos.");
                                    } else {
                                        System.out.println("La ciudad ya está en tus favoritos o hubo un error.");
                                    }
                                    break;

                                case "2":
                                    System.out.print("Ingrese el nombre de la ciudad a eliminar: ");
                                    String ciudadEliminar = scanner.nextLine();
                                    boolean eliminado = client.eliminarFavorito(usuario, ciudadEliminar, nombreCliente);
                                    if (eliminado) {
                                        System.out.println("Ciudad eliminada correctamente de favoritos.");
                                    } else {
                                        System.out.println("No se pudo eliminar la ciudad o no estaba en favoritos.");
                                    }
                                    break;

                                case "3":
                                    gestionar = false;
                                    break;

                                default:
                                    System.out.println("Opción no válida.");
                            }
                        }
                        break;

                    case 5:
                        System.out.println("\n╔════════════════════════════════════════════════╗");
                        System.out.println("║      Gracias por usar AgroMonitoreo.           ║");
                        System.out.println("║      Presione Enter para salir del programa.   ║");
                        System.out.println("╚════════════════════════════════════════════════╝");

                        client.clienteDesconectado(nombreCliente);
                        scanner.nextLine(); // Esperar que el usuario presione Enter
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Opción inválida. Por favor, ingrese un número del 1 al 5.");
                        break;
                }
            }

            scanner.close();
        } catch (RemoteException | NotBoundException e) {
            System.out.println("Error al conectar con el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
