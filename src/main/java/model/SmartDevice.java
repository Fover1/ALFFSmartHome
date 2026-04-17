package model;

import java.util.List;

public interface SmartDevice {

    //Attribute, die jedes Gerät haben muss
    String getId();

    String getName();

    void setName(String name);

    Room getRoom();

    void setRoom(Room room);

    // Gerätespezifische Methoden, die jedes Gerät haben muss
    String getDeviceType();

    String getCurrentState();

    // Strategy-Pattern
    List<String> getAvailableFunctions();

    void executeFunction(String functionName, Object parameter);
}