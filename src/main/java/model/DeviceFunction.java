package model;

public interface DeviceFunction {
    //Repräsentiert eine einzelne Funktion eines Gerätes
    void execute(Object parameter);

    /// Für die GUI nutzen?
    String getDescription();

    /// hiermit soll die GUI dann wissen, was man für nen eingabetyp hat
    Class<?> getParameterType();

    default Double getMin() {
        return null;
    }

    default Double getMax() {
        return null;
    }

    default String getUnit() {
        return null;
    }

    default String getColor() {
        System.out.println("aufruf color interface");
        return null;
    }

}