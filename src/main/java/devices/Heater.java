package devices;

import model.AbstractDevice;
import model.DeviceFunction;

import java.util.UUID;

import static lang.DeviceMessages.HEATER_SWITCH_FUNCTION_DESCRIPTION;
import static lang.DeviceMessages.HEATER_TEMPERATURE_FUNCTION_DESCRIPTION;

public class Heater extends AbstractDevice {
    private double temperature = 0;
    private boolean isOn = false;

    public Heater(UUID id, String name) {
        super(id, name);
    }

    @Override
    protected void initializeFunctions() {
        this.functions.put("Schalten", new DeviceFunction() { //TODO Frage: Soll sowas und "Manuell" auch in die Lang oder nur Sätze?
            @Override
            public void execute(Object parameter) {
                if (parameter instanceof Boolean) {
                    isOn = (Boolean) parameter;
                    temperature = isOn ? temperature : 0;
                }
            }

            @Override
            public String getDescription() {
                return HEATER_SWITCH_FUNCTION_DESCRIPTION;
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
                System.out.println(parameter.getClass().getSimpleName());
                if (parameter instanceof Double) {
                    temperature = (Double) parameter;
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
                return HEATER_TEMPERATURE_FUNCTION_DESCRIPTION;
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
        return isOn ? "An (" + temperature + "°C)" : "Aus";
    }
}