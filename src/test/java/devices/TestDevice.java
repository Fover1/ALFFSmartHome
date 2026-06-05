package devices;

import model.AbstractDevice;
import java.util.UUID;

public class TestDevice extends AbstractDevice {

    public TestDevice(UUID id, String name) {
        super(id, name);
    }

    @Override
    protected void initializeFunctions() {
        // Für diesen Test nicht relevant
    }

    @Override
    public String getDeviceType() {
        return "TestDevice";
    }

    @Override
    public String getCurrentState() {
        return "Off";
    }
}