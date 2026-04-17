package devices;

import model.AbstractDevice;
import model.DeviceFunction;
import model.Room;

public class Heizung extends AbstractDevice {
    private double temperatur = 0;
    private boolean isAn = false;

    public Heizung(String id, String name, Room room) {
        super(id, name, room);

        this.functions.put("Schalten", new DeviceFunction() {
            @Override
            public void execute(Object parameter) {
                if (parameter instanceof Boolean) {
                    isAn = (Boolean) parameter;
                    temperatur = isAn ? temperatur : 0;
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
                    temperatur = (Double) parameter;
                    isAn = temperatur > 0;
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
        return isAn ? "An (" + temperatur + "C)" : "Aus";
    }
}