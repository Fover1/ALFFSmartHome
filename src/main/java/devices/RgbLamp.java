package devices;

import javafx.scene.paint.Color;
import lombok.Getter;
import model.AbstractDevice;
import model.DeviceFunction;

import java.util.UUID;

import static lang.DeviceMessages.*;

//Es wurde bewusst auf eine Vererbung von Lampe verzichtet, um die Autonomie der Klasse RgbLamp von Lampe sicherzustellen
@Getter //TODO Frage: Hat nur die RgbLamp Getter, wegen den Farben?
public class RgbLamp extends AbstractDevice {
    private double brightness = 0;
    private boolean isOn = false;
    private String hexColor = "#FFFFFF";

    public RgbLamp(UUID id, String name) {
        super(id, name);
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
                return LAMP_SWITCH_FUNCTION_DESCRIPTION;
            }

            @Override
            public Class<?> getParameterType() {
                return Boolean.class;
            }

            @Override
            public Boolean getState() {
                return isOn();
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
            public Class<?> getParameterType() {
                return Double.class;
            }

            @Override
            public Double getValue() {
                return brightness;
            }
        });

        this.functions.put("Farbe", new DeviceFunction() {
            @Override
            public void execute(Object parameter) {
                if (parameter instanceof String) {
                    hexColor = (String) parameter;
                }
            }

            @Override
            public String getDescription() {
                return RGBLAMP_COLOR_FUNCTION_DESCRIPTION;
            }

            @Override
            public Class<?> getParameterType() {
                return Color.class;
            }

            @Override
            public String getColor() {
                return hexColor;
            }
        });
    }

    @Override
    public String getDeviceType() {
        return "RgbLampe";
    }

    @Override
    public String getCurrentState() {
        return isOn ? "An (" + brightness + "%)" : "Aus";
    }
}