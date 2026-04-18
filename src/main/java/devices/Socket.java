package devices;

import model.AbstractDevice;
import model.DeviceFunction;
import model.Room;

public class Socket extends AbstractDevice {
    private boolean isOn = false;

    public Socket(String id, String name, Room room) {
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
        });
    }

    @Override
    public String getDeviceType() {
        return "Socket";
    }

    @Override
    public String getCurrentState() {
        return isOn ? "An" : "Aus";
    }
}