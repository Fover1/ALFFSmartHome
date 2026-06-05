package model;

import java.util.List;
import java.util.UUID;

//ist dafuer da, wenn spaeter noch weitere Geraete, die nicht von uns "hergestellt" wurden, eingebunden werden sollen
//z.B. Philips Hue: Es muesste nur das Interface implementiert werden und nicht extra ein toAbstractDeviceHandler oder sonstiges angelegt werden
public interface SmartDevice {
    UUID getId();

    String getName();

    void setName(String name);

    String getDeviceType();

    String getCurrentState();

    //Strategy-Pattern
    List<String> getAvailableFunctions();

    void executeFunction(String functionName, Object parameter);

    void addObserver(DeviceObserver observer);

    void removeObserver(DeviceObserver observer);

    void restoreAfterLoad();

    void notifyObservers();

    DeviceFunction getFunction(String functionName);
}