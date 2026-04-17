package model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lang.ErrorMessages.FUNCTION_NOT_FOUND;

@Getter
@Setter
///  todo: hierfür fehlen noch tests


///  todo: ich habe jetzt die devices aus diesem Test coverage ding rausgenommen. Kann man die vernünftig testen, vor allem wenn sie dynamisch geladen werden?
public abstract class AbstractDevice implements SmartDevice {

    private final String id;

    protected Map<String, DeviceFunction> functions = new HashMap<>();
    private String name;
    private Room room;

    public AbstractDevice(String id, String name, Room room) {
        this.id = id;
        this.name = name;
        this.room = room;
    }

    public abstract String getDeviceType();

    public abstract String getCurrentState();

    @Override
    public void executeFunction(String functionName, Object parameter) {
        DeviceFunction function = functions.get(functionName);
        if (function != null) {
            function.execute(parameter);
        } else {
            throw new IllegalArgumentException(FUNCTION_NOT_FOUND + functionName);
        }
    }

    @Override
    public List<String> getAvailableFunctions() {
        return new ArrayList<>(functions.keySet());
    }
}