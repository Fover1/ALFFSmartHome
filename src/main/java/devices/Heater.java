package devices;

import model.AbstractDevice;
import model.DeviceFunction;
import model.Room;

public class Heater extends AbstractDevice {
    private double temperature = 0;
    private boolean isOn = false;

    public Heater(String id, String name, Room room) {
        super(id, name, room);

        this.functions.put("Schalten", new DeviceFunction() {
            @Override
            public void execute(Object parameter) {
                if (parameter instanceof Boolean) {
                    isOn = (Boolean) parameter;
                    temperature = isOn ? temperature : 0;
                }
            }

            @Override
            public String getDescription() {
                return "Schaltet die Heizung ein oder aus";
            }

            @Override
            public Class<?> getParameterType() {
                return Boolean.class;
            }
        });


        this.functions.put("Temperatur", new DeviceFunction() {
            @Override
            public void execute(Object parameter) {
                if (parameter instanceof Double) {
                    temperature = (Double) parameter;
                    isOn = temperature > 0;
                }
            }

            @Override
            public String getDescription() {
                return "Stellt die Temperatur der Heizung ein";
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
        return isOn ? "An (" + temperature + "C)" : "Aus";
    }
}