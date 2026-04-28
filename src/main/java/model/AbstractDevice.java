package model;

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
///  todo: hierfür fehlen noch tests


///  todo: ich habe jetzt die devices aus diesem Test coverage ding rausgenommen.
public abstract class AbstractDevice implements SmartDevice {

    //impelementiert Methoden, die alle AbstractDevices haben

    private final UUID id;
    protected transient Map<String, DeviceFunction> functions = new HashMap<>();
    private String name;
    private transient Room room;
    private transient List<DeviceObserver> observers = new ArrayList<>();


    public AbstractDevice(UUID id, String name, Room room) {
        /// todo: Problem: Räume speichern ihre Geräte und Geräte speichern ihre Räume. Darüber sollten wir nochmal sprechen
        this.id = id;
        this.name = name;
        this.room = room;
        restoreAfterLoad();
    }

    //diese Methode wird bei den einzelnen Geräten implementiert um die jeweiligen actions festzulegen
    protected abstract void initializeFunctions();

    //observers und functions werden nicht in der JSON gespeichert und müssen somit neu erstellt werden
    public void restoreAfterLoad() {
        this.observers = new ArrayList<>();
        this.functions = new HashMap<>();
        initializeFunctions();
    }

    public abstract String getDeviceType();

    public abstract String getCurrentState();


    @Override
    public void executeFunction(String functionName, Object parameter) {
        DeviceFunction function = functions.get(functionName);
        if (function != null) {
            function.execute(parameter);
            notifyObservers();
        } else {
            throw new IllegalArgumentException(FUNCTION_NOT_FOUND + functionName);
        }
    }

    public void changeRoom(Room room) {
        this.room = room;
    }


    @Override
    public List<String> getAvailableFunctions() {
        return new ArrayList<>(functions.keySet());
    }

    public void addObserver(DeviceObserver observer) {
        if (observers == null) {
            observers = new ArrayList<>();
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(DeviceObserver observer) {
        if (observers != null) {
            observers.remove(observer);
        }
    }

    protected void notifyObservers() {
        if (observers != null) {
            for (DeviceObserver observer : observers) {
                observer.onStateChanged(this);
            }
        }
    }
}