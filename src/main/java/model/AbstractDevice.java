package model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class AbstractDevice implements SmartDevice {

    private final String id;
    // Hier speichern wir die Strategien (Funktionen) des jeweiligen Geräts
    protected Map<String, DeviceFunction> functions = new HashMap<>();
    private String name;
    private Room room;

    public AbstractDevice(String id, String name, Room room) {
        this.id = id;
        this.name = name;
        this.room = room;
    }

    // --- Diese Methoden müssen von Lampe, Heizung etc. implementiert werden ---
    public abstract String getDeviceType();

    public abstract String getCurrentState();

    // --- Diese Methoden setzen das Strategy-Pattern um ---
    @Override
    public void executeFunction(String functionName, Object parameter) {
        DeviceFunction function = functions.get(functionName);
        if (function != null) {
            function.execute(parameter);
        } else {
            throw new IllegalArgumentException("Funktion nicht unterstützt: " + functionName);
        }
    }

    @Override
    public List<String> getAvailableFunctions() {
        return new ArrayList<>(functions.keySet());
    }
}