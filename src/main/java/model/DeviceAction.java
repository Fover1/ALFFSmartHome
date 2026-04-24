package model;

public record DeviceAction(SmartDevice targetDevice, String functionName, Object parameter) implements Action {

    //Verbindung von SmartDevice und Action

    //greift auf Zielgerät zu und führt die Action aus
    @Override
    public void execute() {
        targetDevice.executeFunction(functionName, parameter);
    }

    @Override
    public String getDescription() {
        return targetDevice.getName() + " -> " + functionName + " " + parameter;
    }
}