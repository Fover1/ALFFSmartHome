package frontendController;

import controller.SmartHomeAppController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import interfaces.DeviceFunction;
import model.Room;
import interfaces.SmartDevice;

import java.util.Locale;

public class DashboardController {

    private SmartHomeAppController appController;

    @FXML
    private Label activeDevicesLabel;

    @FXML
    private Label activeDevicesSubtext;

    @FXML
    private Label averageTempLabel;

    @FXML
    private Label warmestRoomLabel;

    public void setAppController(SmartHomeAppController appController) {
        this.appController = appController;
        updateDashboardData();
    }

    private void updateDashboardData() {
        if (appController == null) { return; }

        int totalDevices = 0;
        int activeDevices = 0;

        double totalTemperature = 0.0;
        int sensorCount = 0;
        double maxTemperature = -Double.MAX_VALUE;
        String warmestRoomName = "-";

        for (Room room : appController.getAllRooms()) {
            for (SmartDevice device : room.getSmartDevices()) {
                totalDevices++;

                if (!isDeviceActive(device)) {
                    continue;
                }
                activeDevices++;

                if (!device.getAvailableFunctions().contains("Temperatur")) {
                    continue;
                }

                DeviceFunction tempFunc = device.getFunction("Temperatur");
                if (tempFunc.getValue() == null) {
                    continue;
                }

                double currentTemp = tempFunc.getValue();
                totalTemperature += currentTemp;
                sensorCount++;

                if (currentTemp > maxTemperature) {
                    maxTemperature = currentTemp;
                    warmestRoomName = room.getName();
                }
            }
        }
        updateActiveDevicesUI(activeDevices, totalDevices);
        updateClimateUI(sensorCount, totalTemperature, maxTemperature, warmestRoomName);
    }

    private void updateActiveDevicesUI(int activeDevices, int totalDevices) {
        activeDevicesLabel.setText(String.valueOf(activeDevices));
        activeDevicesSubtext.setText("Von " + totalDevices + " Geräten im Haus eingeschaltet");
    }

    private void updateClimateUI(int sensorCount, double totalTemperature, double maxTemperature, String warmestRoomName) {
        if (sensorCount > 0) {
            double averageTemp = totalTemperature / sensorCount;
            averageTempLabel.setText(String.format(java.util.Locale.US, "%.1f °C", averageTemp));
            warmestRoomLabel.setText(warmestRoomName + " ist am wärmsten ("
                    + String.format(java.util.Locale.US, "%.1f °C", maxTemperature) + ")");
        } else {
            averageTempLabel.setText("-- °C");
            warmestRoomLabel.setText("Keine aktiven Temperatursensoren");
        }
    }

    private boolean isDeviceActive(SmartDevice device) {
        if (device.getAvailableFunctions().contains("Schalten")) {
            DeviceFunction switchFunc = device.getFunction("Schalten");
            if (switchFunc.getState() != null) {
                return switchFunc.getState();
            }
        }
        return true;
    }
}
