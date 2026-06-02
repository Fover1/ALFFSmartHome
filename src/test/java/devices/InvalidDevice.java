package devices;

import model.AbstractDevice;
import java.util.UUID;

public class InvalidDevice extends AbstractDevice {

    // Hat absichtlich NICHT den (UUID, String) Konstruktor
    public InvalidDevice() {
        super(UUID.randomUUID(), "Invalid");
    }

    @Override
    protected void initializeFunctions() {}

    @Override
    public String getDeviceType() {
        return "Invalid";
    }

    @Override
    public String getCurrentState() {
        return "Invalid";
    }
}