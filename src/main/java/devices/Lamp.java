package devices;

import model.AbstractDevice;
import model.DeviceFunction;
import model.Room;

public class Lamp extends AbstractDevice {
    private int brightness = 0;
    private boolean isOn = false;

    public Lamp(String id, String name, Room room) {
        super(id, name, room);
    }

    @Override
    protected void initializeFunctions() {
        this.functions.put("Schalten", new DeviceFunction() {
            @Override
            public void execute(Object parameter) {
                if (parameter instanceof Boolean) {
                    isOn = (Boolean) parameter;
                    brightness = isOn ? 100 : 0;
                }
            }

            @Override
            public String getDescription() {
                return "Schaltet die Lampe ein oder aus";
            }

            @Override
            public Class<?> getParameterType() {
                return Boolean.class;
            }
        });


        this.functions.put("Helligkeit", new DeviceFunction() {
            @Override
            public void execute(Object parameter) {
                if (parameter instanceof Integer) {
                    brightness = (Integer) parameter;
                    isOn = brightness > 0;
                }
            }

            @Override
            public String getDescription() {
                return "Stellt die Helligkeit der Lampe ein";
            }

            @Override
            public Class<?> getParameterType() {
                return Integer.class;
            }
        });
    }

    @Override
    public String getDeviceType() {
        return "Lampe";
    }

    @Override
    public String getCurrentState() {
        return isOn ? "An (" + brightness + "%)" : "Aus";
    }
}