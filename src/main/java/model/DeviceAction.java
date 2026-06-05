package model;

import interfaces.Action;
import interfaces.DeviceFunction;
import interfaces.SmartDevice;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DeviceAction implements Action {
    private final SmartDevice targetDevice;
    private final String functionName;
    private final Object parameter;

    //Hier wird der Zustand gespeichert, bevor die Aktion ausgefuehrt wurde
    @Getter(lombok.AccessLevel.NONE)
    private Object previousParameter;

    @Override
    public void execute() {
        DeviceFunction func = targetDevice.getFunction(functionName);
        if (func != null) {
            if (func.getParameterType() == Boolean.class) {
                this.previousParameter = func.getState();
            } else if (func.getParameterType() == Double.class) {
                this.previousParameter = func.getValue();
            } else {
                this.previousParameter = func.getColor();
            }
        }

        //Hier wird die eigentliche Aktion ausgefuehrt
        targetDevice.executeFunction(functionName, parameter);
    }

    @Override
    public void undo() {
        if (previousParameter != null) {
            targetDevice.executeFunction(functionName, previousParameter);
        }
    }

    @Override
    public String getDescription() {
        return targetDevice.getName() + " ➜ " + functionName + " " + getFormattedParameter();
    }

    public String getFormattedParameter() {
        if (parameter instanceof Double) {
            return String.format(java.util.Locale.US, "%.2f", (Double) parameter);
        }
        return String.valueOf(parameter);
    }
}