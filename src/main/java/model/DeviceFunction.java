package model;

public interface DeviceFunction {
    //repraesentiert eine einzelne Funktion eines Geraetes
    void execute(Object parameter);

    String getDescription();

    //hiermit soll die GUI  wissen, was man fuer einen Eingabetypen hat
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
        return null;
    }

    default Double getValue() {
        return null;
    }

    default Boolean getState() {
        return null;
    }
}