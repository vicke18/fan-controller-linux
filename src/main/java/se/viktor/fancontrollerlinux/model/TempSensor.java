package se.viktor.fancontroller.model;

public record TempSensor(
        String id,
        String label,
        String driver,
        double tempCelsius
) {}
