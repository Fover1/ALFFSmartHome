package model;

import interfaces.DeviceFunction;
import interfaces.DeviceObserver;
import interfaces.SmartDevice;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static lang.ErrorMessages.FUNCTION_NOT_FOUND;

@Getter
@Setter
//impelementiert Methoden, die alle AbstractDevices haben
public abstract class AbstractDevice implements SmartDevice {
    private final UUID id;
    protected transient Map<String, DeviceFunction> functions = new HashMap<>();
    private String name;
    private transient List<DeviceObserver> observers = new ArrayList<>();

    public AbstractDevice(UUID id, String name) {
        this.id = id;
        this.name = name;
        restoreAfterLoad();
    }

    //Diese Methode wird bei den einzelnen Geraeten implementiert, um die jeweiligen Aktionen festzulegen
    protected abstract void initializeFunctions();

    //"observers" und "functions" werden nicht in der JSON gespeichert und muessen somit neu erstellt werden
    @Override
    public void restoreAfterLoad() {
        if (this.observers == null) {
            this.observers = new ArrayList<>();
        }

        if (this.functions == null) {
            this.functions = new HashMap<>();
            initializeFunctions();
        } else if (this.functions.isEmpty()) {
            initializeFunctions();
        }
    }

    @Override
    public abstract String getDeviceType();

    @Override
    public abstract String getCurrentState();

    @Override
    public void executeFunction(String functionName, Object parameter) {
        restoreAfterLoad();
        DeviceFunction function = functions.get(functionName);
        if (function != null) {
            function.execute(parameter);
            notifyObservers();
        } else {
            throw new IllegalArgumentException(FUNCTION_NOT_FOUND + functionName);
        }
    }

    @Override
    public List<String> getAvailableFunctions() {
        return new ArrayList<>(functions.keySet());
    }

    @Override
    public void addObserver(DeviceObserver observer) {
        if (observers == null) {
            observers = new ArrayList<>();
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(DeviceObserver observer) {
        if (observers != null) {
            observers.remove(observer);
        }
    }

    @Override
    public void notifyObservers() {
        if (observers != null) {
            for (DeviceObserver observer : observers) {
                observer.onStateChanged(this);
            }
        }
    }

    @Override
    public DeviceFunction getFunction(String name) {
        restoreAfterLoad();
        return functions.get(name);
    }
}