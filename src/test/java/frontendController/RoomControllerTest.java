package frontendController;

import controller.SmartHomeAppController;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    private RoomController roomController;

    private FlowPane roomContainer;
    private FlowPane deviceContainer;
    private Label roomSelection;
    private Label deviceDisplay;
    private Button addDevice;
    private Button deleteRoom;
    private Button editRoom;

    @Mock
    private SmartHomeAppController mockAppController;

    @Mock
    private Room mockRoom1;

    @Mock
    private Room mockRoom2;

    @Mock
    private SmartDevice mockDevice;

    @BeforeAll
    static void initToolkit() {
        //startet das JavaFX Toolkit
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            //ignorieren, falls das Toolkit bereits von anderen Tests gestartet wurde
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        roomController = new RoomController();

        roomContainer = new FlowPane();
        deviceContainer = new FlowPane();
        roomSelection = new Label();
        deviceDisplay = new Label();
        addDevice = new Button();
        deleteRoom = new Button();
        editRoom = new Button();

        setPrivateField(roomController, "roomContainer", roomContainer);
        setPrivateField(roomController, "deviceContainer", deviceContainer);
        setPrivateField(roomController, "roomSelection", roomSelection);
        setPrivateField(roomController, "deviceDisplay", deviceDisplay);
        setPrivateField(roomController, "addDevice", addDevice);
        setPrivateField(roomController, "deleteRoom", deleteRoom);
        setPrivateField(roomController, "editRoom", editRoom);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testSetAppControllerUpdatesRoomContainer() {
        when(mockAppController.getAllRooms()).thenReturn(List.of(mockRoom1, mockRoom2));
        when(mockRoom1.getName()).thenReturn("Küche");
        when(mockRoom2.getName()).thenReturn("Bad");

        roomController.setAppController(mockAppController);

        assertEquals(2, roomContainer.getChildren().size());

        Button button1 = (Button) roomContainer.getChildren().get(0);
        assertEquals("Küche", button1.getText());

        Button button2 = (Button) roomContainer.getChildren().get(1);
        assertEquals("Bad", button2.getText());

        assertEquals("Noch kein Raum ausgewählt", roomSelection.getText());
    }

    @Test
    void testSetRoomObserverRegistration() {
        when(mockAppController.getAllRooms()).thenReturn(Collections.emptyList());
        roomController.setAppController(mockAppController);

        roomController.setRoom(mockRoom1);

        verify(mockRoom1).addObserver(roomController);

        roomController.setRoom(mockRoom2);

        verify(mockRoom1).removeObserver(roomController);
        verify(mockRoom2).addObserver(roomController);
    }

    @Test
    void testShowDevicesUpdatesUIAndContainer() {
        when(mockRoom1.getName()).thenReturn("Wohnzimmer");
        when(mockRoom1.getSmartDevices()).thenReturn(List.of(mockDevice));
        when(mockDevice.getName()).thenReturn("Fernseher");
        when(mockAppController.getAllRooms()).thenReturn(Collections.emptyList());

        roomController.setAppController(mockAppController);
        roomController.showDevices(mockRoom1);

        assertTrue(deviceDisplay.isVisible());
        assertTrue(addDevice.isVisible());
        assertTrue(deleteRoom.isVisible());
        assertTrue(editRoom.isVisible());
        assertTrue(roomSelection.getText().contains("Ausgewählter Raum: Wohnzimmer"));
        assertEquals(1, deviceContainer.getChildren().size());
        Button deviceButton = (Button) deviceContainer.getChildren().get(0);
        assertEquals("Fernseher", deviceButton.getText());
    }

    @Test
    void testHandleAddRoomSuccess() {
        try (MockedStatic<StringInputDialog> mockedDialog = mockStatic(StringInputDialog.class)) {
            mockedDialog.when(() -> StringInputDialog.get(any(), any(), any(), any()))
                    .thenReturn(Optional.of("Neuer Test Raum"));

            when(mockAppController.getAllRooms()).thenReturn(Collections.emptyList());

            roomController.setAppController(mockAppController); // init
            roomController.handleAddRoom();

            verify(mockAppController).addRoom("Neuer Test Raum");
            verify(mockAppController).save();
        }
    }

    @Test
    void testHandleAddRoomEmptyNameIgnored() {
        try (MockedStatic<StringInputDialog> mockedDialog = mockStatic(StringInputDialog.class)) {
            mockedDialog.when(() -> StringInputDialog.get(any(), any(), any(), any()))
                    .thenReturn(Optional.of("   "));

            roomController.setAppController(mockAppController);
            roomController.handleAddRoom();

            verify(mockAppController, org.mockito.Mockito.never()).addRoom(any());
        }
    }
}