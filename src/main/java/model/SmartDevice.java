package model;

import java.util.List;
import java.util.UUID;

public interface SmartDevice {

    //ist dafür da, wenn man später noch weitere Geräte, die nicht von uns "hergestellt" wurden einbinden möchte
    //z.B. philips hue, die müssen dann nur das Interface implementieren und nicht extra ein toAbstractDeviceHandler mäßig

    UUID getId();

    String getName();

    void setName(String name);

    String getDeviceType();

    String getCurrentState();

    // Strategy-Pattern
    List<String> getAvailableFunctions();

    void executeFunction(String functionName, Object parameter);
}