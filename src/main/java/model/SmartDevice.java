package model;

import java.util.List;

public interface SmartDevice {

    String getId();

    String getName();

    void setName(String name);

    Room getRoom();

    void setRoom(Room room);

    String getDeviceType();

    String getCurrentState();

    // Strategy-Pattern
    List<String> getAvailableFunctions();

    void executeFunction(String functionName, Object parameter);
}