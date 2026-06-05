package frontendController;

import controller.SmartHomeAppController;
import javafx.application.Platform;
import javafx.scene.control.Label;
import interfaces.DeviceFunction;
import model.Room;
import interfaces.SmartDevice;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {
    private DashboardController dashboardController;
    private Label activeDevicesLabel;
    private Label activeDevicesSubtext;
    private Label averageTempLabel;
    private Label warmestRoomLabel;

    @Mock
    private SmartHomeAppController mockAppController;

    @BeforeAll
    static void initToolkit() {
        //startet die JavaFX-Umgebung für die Tests, um Exceptions bei den Labels zu vermeiden
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            //ignorieren, falls das Toolkit bereits von einer anderen Testklasse gestartet wurde
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        dashboardController = new DashboardController();
        activeDevicesLabel = new Label();
        activeDevicesSubtext = new Label();
        averageTempLabel = new Label();
        warmestRoomLabel = new Label();

        setPrivateField(dashboardController, "activeDevicesLabel", activeDevicesLabel);
        setPrivateField(dashboardController, "activeDevicesSubtext", activeDevicesSubtext);
        setPrivateField(dashboardController, "averageTempLabel", averageTempLabel);
        setPrivateField(dashboardController, "warmestRoomLabel", warmestRoomLabel);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testSetAppControllerWithEmptyRooms() {
        when(mockAppController.getAllRooms()).thenReturn(Collections.emptyList());

        dashboardController.setAppController(mockAppController);

        assertEquals("0", activeDevicesLabel.getText());
        assertEquals("Von 0 Geräten im Haus eingeschaltet", activeDevicesSubtext.getText());
        assertEquals("-- °C", averageTempLabel.getText());
        assertEquals("Keine aktiven Temperatursensoren", warmestRoomLabel.getText());
    }

    @Test
    void testUpdateActiveDevicesCountsCorrectly() {
        Room mockRoom = mock(Room.class);

        SmartDevice activeDevice = mock(SmartDevice.class);
        DeviceFunction activeSwitch = mock(DeviceFunction.class);
        when(activeDevice.getAvailableFunctions()).thenReturn(List.of("Schalten"));
        when(activeDevice.getFunction("Schalten")).thenReturn(activeSwitch);
        when(activeSwitch.getState()).thenReturn(true);

        SmartDevice inactiveDevice = mock(SmartDevice.class);
        DeviceFunction inactiveSwitch = mock(DeviceFunction.class);
        when(inactiveDevice.getAvailableFunctions()).thenReturn(List.of("Schalten"));
        when(inactiveDevice.getFunction("Schalten")).thenReturn(inactiveSwitch);
        when(inactiveSwitch.getState()).thenReturn(false);

        SmartDevice noSwitchDevice = mock(SmartDevice.class);
        when(noSwitchDevice.getAvailableFunctions()).thenReturn(Collections.emptyList());
        when(mockRoom.getSmartDevices()).thenReturn(List.of(activeDevice, inactiveDevice, noSwitchDevice));
        when(mockAppController.getAllRooms()).thenReturn(List.of(mockRoom));

        dashboardController.setAppController(mockAppController);

        assertEquals("2", activeDevicesLabel.getText());
        assertEquals("Von 3 Geräten im Haus eingeschaltet", activeDevicesSubtext.getText());
    }

    @Test
    void testUpdateClimateDataCalculatesAverageAndWarmestRoom() {
        Room room1 = mock(Room.class);
        when(room1.getName()).thenReturn("Wohnzimmer");

        Room room2 = mock(Room.class);
        when(room2.getName()).thenReturn("Küche");

        SmartDevice tempDevice1 = mock(SmartDevice.class);
        DeviceFunction tempFunc1 = mock(DeviceFunction.class);
        when(tempDevice1.getAvailableFunctions()).thenReturn(List.of("Temperatur"));
        when(tempDevice1.getFunction("Temperatur")).thenReturn(tempFunc1);
        when(tempFunc1.getValue()).thenReturn(20.5);

        SmartDevice tempDevice2 = mock(SmartDevice.class);
        DeviceFunction tempFunc2 = mock(DeviceFunction.class);
        when(tempDevice2.getAvailableFunctions()).thenReturn(List.of("Temperatur"));
        when(tempDevice2.getFunction("Temperatur")).thenReturn(tempFunc2);
        when(tempFunc2.getValue()).thenReturn(25.0);

        when(room1.getSmartDevices()).thenReturn(List.of(tempDevice1));
        when(room2.getSmartDevices()).thenReturn(List.of(tempDevice2));
        when(mockAppController.getAllRooms()).thenReturn(List.of(room1, room2));

        dashboardController.setAppController(mockAppController);

        assertEquals("22.8 °C", averageTempLabel.getText());
        assertEquals("Küche ist am wärmsten (25.0 °C)", warmestRoomLabel.getText());
    }
}