package model;

public record DeviceAction(SmartDevice targetDevice, String functionName, Object parameter) implements Action {

    @Override
    public void execute() {
        targetDevice.executeFunction(functionName, parameter);
    }

    @Override
    public String getDescription() {
        return targetDevice.getName() + " -> " + functionName + " " + parameter;
    }
}