package frontendController;

import controller.SmartHomeAppController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import interfaces.DeviceFunction;
import model.Room;
import interfaces.SmartDevice;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceControllerTest {

    private DeviceController deviceController;
    private GridPane deviceGrid;
    private Label roomLabel;
    private Label deviceName;
    private Label deviceType;
    private Label deviceIdLabel;
    private Button deleteDevice;
    private Button changeDeviceName;
    private ComboBox<Room> changeDeviceRoom;

    @Mock
    private SmartHomeAppController mockAppController;

    @Mock
    private SmartDevice mockDevice;

    @Mock
    private Room mockRoom;

    @BeforeAll
    static void initToolkit() {
        //startet das JavaFX Toolkit, damit UI-Komponenten ohne Crash instanziiert werden können
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            //ignorieren, falls das Toolkit bereits laeuft
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        deviceController = new DeviceController();

        deviceGrid = new GridPane();
        roomLabel = new Label();
        deviceName = new Label();
        deviceType = new Label();
        deviceIdLabel = new Label();
        deleteDevice = new Button();
        changeDeviceName = new Button();
        changeDeviceRoom = new ComboBox<>();

        //UI-Elemente via Reflection ins Controller Objekt bringen
        setPrivateField(deviceController, "deviceGrid", deviceGrid);
        setPrivateField(deviceController, "roomLabel", roomLabel);
        setPrivateField(deviceController, "deviceName", deviceName);
        setPrivateField(deviceController, "deviceType", deviceType);
        setPrivateField(deviceController, "deviceIdLabel", deviceIdLabel);
        setPrivateField(deviceController, "deleteDevice", deleteDevice);
        setPrivateField(deviceController, "changeDeviceName", changeDeviceName);
        setPrivateField(deviceController, "changeDeviceRoom", changeDeviceRoom);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testSetDataUpdatesStaticLabels() {
        UUID testId = UUID.randomUUID();
        when(mockDevice.getId()).thenReturn(testId);
        when(mockDevice.getName()).thenReturn("Deckenlampe");
        when(mockDevice.getDeviceType()).thenReturn("Lampe");
        when(mockRoom.getName()).thenReturn("Wohnzimmer");

        when(mockAppController.getAllRooms()).thenReturn(Collections.emptyList());
        when(mockDevice.getAvailableFunctions()).thenReturn(Collections.emptyList());

        deviceController.setData(mockDevice, mockAppController, mockRoom);

        assertEquals("Gerät: Deckenlampe", deviceName.getText());
        assertEquals("Gerätetype: Lampe", deviceType.getText());
        assertEquals("Raum: Wohnzimmer", roomLabel.getText());
        assertEquals("ID: " + testId, deviceIdLabel.getText());
        verify(mockDevice).addObserver(deviceController);
    }

    @Test
    void testSetDataGeneratesDynamicControls() {
        when(mockDevice.getId()).thenReturn(UUID.randomUUID());
        when(mockDevice.getAvailableFunctions()).thenReturn(List.of("Schalten"));

        DeviceFunction mockFunction = mock(DeviceFunction.class);
        org.mockito.Mockito.doReturn(Boolean.class).when(mockFunction).getParameterType();
        when(mockFunction.getState()).thenReturn(true);
        when(mockDevice.getFunction("Schalten")).thenReturn(mockFunction);

        when(mockAppController.getAllRooms()).thenReturn(Collections.emptyList());

        CheckBox dummyCheckBox = new CheckBox();
        try (MockedStatic<ParameterControlFactory> mockedFactory = mockStatic(ParameterControlFactory.class)) {
            mockedFactory.when(() -> ParameterControlFactory.createControl(eq(mockDevice), eq("Schalten"), eq("true")))
                    .thenReturn(dummyCheckBox);

            deviceController.setData(mockDevice, mockAppController, mockRoom);

            assertTrue(deviceGrid.getChildren().contains(dummyCheckBox), "Das generierte Control muss im GridPane liegen");
            assertNotNull(dummyCheckBox.getOnAction(), "Der LiveListener muss einen Action-Event an die CheckBox hängen");
        }
    }

    @Test
    void testRoomComboBoxChangesDeviceRoom() {
        when(mockDevice.getId()).thenReturn(UUID.randomUUID());
        when(mockDevice.getAvailableFunctions()).thenReturn(Collections.emptyList());

        Room targetRoom = mock(Room.class);
        when(targetRoom.getName()).thenReturn("Schlafzimmer");
        when(mockRoom.getName()).thenReturn("Wohnzimmer");

        when(mockAppController.getAllRooms()).thenReturn(List.of(mockRoom, targetRoom));

        deviceController.setData(mockDevice, mockAppController, mockRoom);

        assertEquals(2, changeDeviceRoom.getItems().size());

        changeDeviceRoom.setValue(targetRoom);
        changeDeviceRoom.getOnAction().handle(new ActionEvent());

        verify(mockAppController).changeDeviceRoom(mockDevice, mockRoom, targetRoom);
        verify(mockAppController).save();
    }
}