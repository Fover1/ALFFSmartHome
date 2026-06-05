package devices;

import model.AbstractDevice;
import interfaces.DeviceFunction;

import java.util.UUID;

import static lang.DeviceMessages.LAMP_BRIGHTNESS_FUNCTION_DESCRIPTION;
import static lang.DeviceMessages.LAMP_SWITCH_FUNCTION_DESCRIPTION;

public class Lamp extends AbstractDevice {
    private double brightness = 0;
    private boolean isOn = false;
    
    public Lamp(UUID id, String name) {
        super(id, name);
    }

    @Override
    protected void initializeFunctions() {
        //Hier werden die Funktionen eines Geraetes angegeben (es koennen mehrere Funktionen angegeben werden)
        this.functions.put("Schalten", new DeviceFunction() {
            @Override
            public void execute(Object parameter) {
                if (parameter instanceof Boolean) {
                    isOn = (Boolean) parameter;
                }
            }

            @Override
            public String getDescription() {
                return LAMP_SWITCH_FUNCTION_DESCRIPTION;
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

        this.functions.put("Helligkeit", new DeviceFunction() {
            @Override
            public void execute(Object parameter) {
                if (parameter instanceof Double) {
                    brightness = (Double) parameter;
                }
            }

            @Override
            public Double getMin() {
                return 0.0;
            }

            @Override
            public Double getMax() {
                return 100.0;
            }

            @Override
            public String getUnit() {
                return "%";
            }

            @Override
            public String getDescription() {
                return LAMP_BRIGHTNESS_FUNCTION_DESCRIPTION;
            }

            @Override
            public Double getValue() {
                return brightness;
            }

            @Override
            public Class<?> getParameterType() {
                return Double.class;
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