package controller;

import interfaces.Action;
import model.DeviceAction;
import model.LogEntry;
import interfaces.LogListener;
import model.PersistenceManager;
import model.Room;
import model.Scenario;
import interfaces.SmartDevice;
import model.SmartHomeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SmartHomeAppControllerTest {
    private SmartHomeAppController controller;

    @Mock
    private SmartHomeModel mockModel;

    @Mock
    private LogListener mockLogListener;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        controller = new SmartHomeAppController();

        //Reflection nutzen, um das im Konstruktor erstellte SmartHomeModel durch Mock zu ersetzen
        Field modelField = SmartHomeAppController.class.getDeclaredField("smartHomeModel");
        modelField.setAccessible(true);
        modelField.set(controller, mockModel);
    }

    @Test
    void testSave() {
        List<Room> rooms = new ArrayList<>();
        List<Scenario> scenarios = new ArrayList<>();
        when(mockModel.getRooms()).thenReturn(rooms);
        when(mockModel.getScenarios()).thenReturn(scenarios);

        try (MockedStatic<PersistenceManager> mockedPersistence = mockStatic(PersistenceManager.class)) {
            controller.save();
            mockedPersistence.verify(() -> PersistenceManager.save(rooms, scenarios));
        }
    }

    @Test
    void testAddRoom() {
        try (MockedStatic<PersistenceManager> mockedPersistence = mockStatic(PersistenceManager.class)) {
            controller.addRoom("Wohnzimmer");

            ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
            verify(mockModel).addRoom(roomCaptor.capture());
            assertEquals("Wohnzimmer", roomCaptor.getValue().getName());

            mockedPersistence.verify(() -> PersistenceManager.save(any(), any()));
        }
    }

    @Test
    void testDeleteRoom() {
        try (MockedStatic<PersistenceManager> mockedPersistence = mockStatic(PersistenceManager.class)) {
            Room mockRoom = mock(Room.class);
            controller.deleteRoom(mockRoom);

            verify(mockModel).removeRoom(mockRoom);
            mockedPersistence.verify(() -> PersistenceManager.save(any(), any()));
        }
    }

    @Test
    void testChangeRoomName() {
        Room mockRoom = mock(Room.class);
        controller.changeRoomName(mockRoom, "Schlafzimmer");
        verify(mockModel).changeRoomName(mockRoom, "Schlafzimmer");
    }

    @Test
    void testChangeDeviceName() {
        SmartDevice mockDevice = mock(SmartDevice.class);
        controller.changeDeviceName(mockDevice, "Deckenlampe");
        verify(mockModel).changeDeviceName(mockDevice, "Deckenlampe");
    }

    @Test
    void testDeleteDevice() {
        SmartDevice mockDevice = mock(SmartDevice.class);
        Room mockRoom = mock(Room.class);
        controller.deleteDevice(mockDevice, mockRoom);
        verify(mockModel).removeDevice(mockDevice, mockRoom);
    }

    @Test
    void testChangeDeviceRoom() {
        SmartDevice mockDevice = mock(SmartDevice.class);
        Room oldRoom = mock(Room.class);
        Room newRoom = mock(Room.class);
        controller.changeDeviceRoom(mockDevice, oldRoom, newRoom);
        verify(mockModel).changeDeviceRoom(mockDevice, oldRoom, newRoom);
    }

    @Test
    void testGetAllDevices() {
        controller.getAllDevices();
        verify(mockModel).getAllDevices();
    }

    @Test
    void testGetAllRooms() {
        controller.getAllRooms();
        verify(mockModel).getRooms();
    }

    @Test
    void testAddScenario() {
        Scenario mockScenario = mock(Scenario.class);
        controller.addSzenario(mockScenario);
        verify(mockModel).addScenario(mockScenario);
    }

    @Test
    void testGetAllScenarios() {
        controller.getAllScenarios();
        verify(mockModel).getScenarios();
    }

    @Test
    void testRemoveScenario() {
        Scenario mockScenario = mock(Scenario.class);
        controller.removeScenario(mockScenario);
        verify(mockModel).removeScenario(mockScenario);
    }

    @Test
    void testExecuteAndRememberDeviceAction() {
        DeviceAction mockAction = mock(DeviceAction.class);
        SmartDevice mockDevice = mock(SmartDevice.class);

        when(mockAction.getTargetDevice()).thenReturn(mockDevice);
        when(mockDevice.getName()).thenReturn("Heizung");
        when(mockAction.getDescription()).thenReturn("Temperatur ändern");
        when(mockAction.getParameter()).thenReturn(22);

        controller.addLogListener(mockLogListener);
        controller.executeAndRemember(mockAction);
        verify(mockAction).execute();

        ArgumentCaptor<LogEntry> logCaptor = ArgumentCaptor.forClass(LogEntry.class);
        verify(mockLogListener).onLogEntryCreated(logCaptor.capture());

        LogEntry capturedLog = logCaptor.getValue();
        assertEquals("Manuell", capturedLog.scenarioName());
        assertEquals("Heizung", capturedLog.deviceName());
    }

    @Test
    void testExecuteScenario() {
        Scenario mockScenario = mock(Scenario.class);
        DeviceAction mockDeviceAction = mock(DeviceAction.class);
        SmartDevice mockDevice = mock(SmartDevice.class);

        when(mockScenario.getName()).thenReturn("Guten Morgen");
        when(mockScenario.getActions()).thenReturn(List.of(mockDeviceAction));
        when(mockDeviceAction.getTargetDevice()).thenReturn(mockDevice);
        when(mockDevice.getName()).thenReturn("Kaffeemaschine");
        when(mockDeviceAction.getDescription()).thenReturn("Einschalten");
        when(mockDeviceAction.getParameter()).thenReturn(1);

        controller.addLogListener(mockLogListener);
        controller.executeScenario(mockScenario);
        verify(mockScenario).execute();

        ArgumentCaptor<LogEntry> logCaptor = ArgumentCaptor.forClass(LogEntry.class);
        verify(mockLogListener).onLogEntryCreated(logCaptor.capture());

        LogEntry capturedLog = logCaptor.getValue();
        assertEquals("Guten Morgen", capturedLog.scenarioName());
        assertEquals("Kaffeemaschine", capturedLog.deviceName());
    }

    @Test
    void testUndoLastActionStackNotEmpty() {
        Action mockAction = mock(Action.class);
        when(mockAction.getDescription()).thenReturn("Test Aktion");

        controller.addLogListener(mockLogListener);
        controller.executeAndRemember(mockAction);
        controller.undoLastAction();

        verify(mockAction).undo();
        ArgumentCaptor<LogEntry> logCaptor = ArgumentCaptor.forClass(LogEntry.class);
        verify(mockLogListener).onLogEntryCreated(logCaptor.capture());

        assertEquals("System", logCaptor.getValue().scenarioName());
    }

    @Test
    void testUndoLastActionStackEmpty() {
        controller.addLogListener(mockLogListener);
        controller.undoLastAction();
        verify(mockLogListener, never()).onLogEntryCreated(any());
    }

    @Test
    void testLoadConfiguration() {
        try (MockedStatic<PersistenceManager> mockedPersistence = mockStatic(PersistenceManager.class)) {
            List<Room> testRooms = new ArrayList<>();
            List<Scenario> testScenarios = new ArrayList<>();
            PersistenceManager.SmartHomeData mockData = new PersistenceManager.SmartHomeData(testRooms, testScenarios);

            String testFile = "config.json";
            mockedPersistence.when(() -> PersistenceManager.load(testFile)).thenReturn(mockData);

            controller.loadConfiguration(testFile);

            mockedPersistence.verify(() -> PersistenceManager.setFileName(testFile));
            verify(mockModel).setRooms(testRooms);
            verify(mockModel).setScenarios(testScenarios);
        }
    }
}