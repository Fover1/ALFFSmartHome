package devices;

import model.AbstractDevice;
import model.Room;

public class Light extends AbstractDevice {
    public Light(String id, String name, Room room) {
        super(id, name, room);
    }

    @Override
    public String getDeviceType() {
        return "Light";
    }

    @Override
    public String getCurrentState() {
        return "Off";
    }
}