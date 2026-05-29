package model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DeviceAction implements Action {

    private final SmartDevice targetDevice;
    private final String functionName;
    private final Object parameter;

    // Hier speichern wir den Zustand, bevor die Aktion ausgeführt wurde
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

        System.out.println("Alter Wert (" + this.previousParameter + ") gemerkt. Führe neuen Wert aus: " + parameter);

        // 2. Die eigentliche Aktion ausführen
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
        return targetDevice.getName() + " -> " + functionName + " " + parameter;
    }
}