package model;

public interface DeviceFunction {
    void execute(Object parameter);

    /// Für die GUI nutzen?
    String getDescription();

    Class<?> getParameterType();
}