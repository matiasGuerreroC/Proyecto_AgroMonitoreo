package server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClimaAPIResponse {
    public MainData main;
    public List<WeatherData> weather;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MainData {
        public double temp;
        public int humidity;

        public MainData() {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WeatherData {
        public String description;

        public WeatherData() {}
    }

    public ClimaAPIResponse() {}
}
