package devices;

import model.AbstractDevice;
import model.DeviceFunction;
import model.Room;

import java.util.UUID;

public class Heater extends AbstractDevice {
    private double temperature = 0;
    private boolean isOn = false;

    public Heater(UUID id, String name, Room room) {
        super(id, name, room);
    }

    @Override
    protected void initializeFunctions() {
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
            public Boolean getState() {
                return isOn;
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
            public Double getMin() {
                return 0.0;
            }

            @Override
            public Double getMax() {
                return 30.0;
            }

            @Override
            public String getUnit() {
                return "°C";
            }

            @Override
            public String getDescription() {
                return "Stellt die Temperatur der Heizung ein";
            }

            @Override
            public Double getValue() {
                return temperature;
            }

            @Override
            public Class<?> getParameterType() {
                return Double.class;
            }
        });
    }

    @Override
    public String getDeviceType() {
        return "Heizung";
    }

    @Override
    public String getCurrentState() {
        return isOn ? "An (" + temperature + "C)" : "Aus";
    }
}