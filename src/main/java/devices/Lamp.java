package devices;

import model.AbstractDevice;
import model.DeviceFunction;
import model.Room;

public class Lamp extends AbstractDevice {
    private int helligkeit = 0;
    private boolean isAn = false;

    public Lamp(String id, String name, Room room) {
        super(id, name, room);

        this.functions.put("Schalten", new DeviceFunction() {
            @Override
            public void execute(Object parameter) {
                if (parameter instanceof Boolean) {
                    isAn = (Boolean) parameter;
                    helligkeit = isAn ? 100 : 0;
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
                    helligkeit = (Integer) parameter;
                    isAn = helligkeit > 0;
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
        return isAn ? "An (" + helligkeit + "%)" : "Aus";
    }
}