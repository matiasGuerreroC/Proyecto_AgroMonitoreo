package common;

import java.io.Serializable;

public class ClimaCiudad implements Serializable {
    private String ciudad;
    private double temperatura;
    private int humedad;
    private String descripcion;
    private String fechaConsulta; // Nuevo
    private String horaConsulta;  // Nuevo

    public ClimaCiudad(String ciudad, double temperatura, int humedad, String descripcion, String fechaConsulta, String horaConsulta) {
        this.ciudad = ciudad;
        this.temperatura = temperatura;
        this.humedad = humedad;
        this.descripcion = descripcion;
        this.fechaConsulta = fechaConsulta;
        this.horaConsulta = horaConsulta;
    }

    public String getCiudad() {
        return ciudad;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public int getHumedad() {
        return humedad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFechaConsulta() {
        return fechaConsulta;
    }

    public String getHoraConsulta() {
        return horaConsulta;
    }

    public void setFechaConsulta(String fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }

    public void setHoraConsulta(String horaConsulta) {
        this.horaConsulta = horaConsulta;
    }

    @Override
    public String toString() {
        return String.format(
            "Ciudad: %s | Temp: %.1f°C | Humedad: %d%% | Estado: %s | Fecha: %s | Hora: %s",
            ciudad, temperatura, humedad, descripcion, fechaConsulta, horaConsulta
        );
    }
}
