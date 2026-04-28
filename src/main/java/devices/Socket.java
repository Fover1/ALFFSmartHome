package devices;

import model.AbstractDevice;
import model.DeviceFunction;
import model.Room;

import java.util.UUID;

public class Socket extends AbstractDevice {
    private boolean isOn = false;

    public Socket(UUID id, String name, Room room) {
        super(id, name, room);
    }

    @Override
    protected void initializeFunctions() {
        this.functions.put("Schalten", new DeviceFunction() {
            @Override
            public void execute(Object parameter) {
                if (parameter instanceof Boolean) {
                    isOn = (Boolean) parameter;
                }
            }

            @Override
            public String getDescription() {
                return "Schaltet Steckdose ein oder aus";
            }

            @Override
            public Class<?> getParameterType() {
                return Boolean.class;
            }

            @Override
            public Boolean getState() {
                return isOn;
            }
        });
    }

    @Override
    public String getDeviceType() {
        return "Steckdose";
    }

    @Override
    public String getCurrentState() {
        return isOn ? "An" : "Aus";
    }
}