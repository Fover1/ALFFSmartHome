package devices;

import javafx.scene.paint.Color;
import lombok.Getter;
import model.AbstractDevice;
import model.DeviceFunction;
import model.Room;

@Getter
public class RgbLamp extends AbstractDevice {
    //konkretes Gerät
    private double brightness = 0;
    private boolean isOn = false;
    private String hexColor = "#FFFFFF";

    public RgbLamp(String id, String name, Room room) {
        super(id, name, room);
    }

    @Override
    protected void initializeFunctions() {
        //hier werden die Funktionen eines Gerätes angegeben (es können mehrere Funktionen angegeben)
        this.functions.put("Schalten", new DeviceFunction() {
            @Override
            public void execute(Object parameter) {
                if (parameter instanceof Boolean) {
                    isOn = (Boolean) parameter;
                    ///  todo: das ist noch nicht ganz richtig
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
                    isOn = brightness > 0;
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
                return "Stellt die Helligkeit der Lampe ein";
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
                    System.out.println("Farbe geändert auf: " + hexColor);
                }
            }

            @Override
            public String getDescription() {
                return "Stellt die Lichtfarbe via Hex-Code ein";
            }

            @Override
            public Class<?> getParameterType() {
                return Color.class;
            }

            @Override
            public String getColor() {
                System.out.println("Farbe color rgblamp" + hexColor);
                return hexColor;
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