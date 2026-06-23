package se.viktor.fancontrollerlinux.model;

public record TempSensor(
        String id,
        String label,
        String driver,
        double tempCelsius
) {}
