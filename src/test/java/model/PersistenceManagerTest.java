package model;

import interfaces.Action;
import interfaces.SmartDevice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistenceManagerTest {

    @TempDir
    Path tempDir;

    private File tempFile;
    private final String originalFileName = "smarthome_config.json";

    @BeforeEach
    void setUp() {
        tempFile = tempDir.resolve("test_config.json").toFile();
        PersistenceManager.setFileName(tempFile.getAbsolutePath());
    }

    @AfterEach
    void tearDown() {
        PersistenceManager.setFileName(originalFileName);
    }

    @Test
    void testSaveAndLoadEmptyData() {
        List<Room> rooms = new ArrayList<>();
        List<Scenario> scenarios = new ArrayList<>();

        PersistenceManager.save(rooms, scenarios);

        assertTrue(tempFile.exists(), "Die JSON-Datei sollte erstellt worden sein");

        PersistenceManager.SmartHomeData loadedData = PersistenceManager.load(tempFile.getAbsolutePath());

        assertNotNull(loadedData);
        assertNotNull(loadedData.rooms);
        assertNotNull(loadedData.scenarios);
        assertTrue(loadedData.rooms.isEmpty());
        assertTrue(loadedData.scenarios.isEmpty());
    }

    @Test
    void testLoadFileDoesNotExist() {
        PersistenceManager.SmartHomeData loadedData = PersistenceManager.load(tempDir.resolve("gibt_es_nicht.json").toString());
        assertNull(loadedData, "Wenn die Datei nicht existiert, soll null zurückgegeben werden");
    }

    @Test
    void testLoadInvalidJsonFormat() throws IOException {
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("{ kaputtes json Format ]");
        }
        PersistenceManager.SmartHomeData loadedData = PersistenceManager.load(tempFile.getAbsolutePath());
        assertNull(loadedData, "Bei einer Exception (JsonSyntaxException) soll null zurückgegeben werden");
    }

    @Test
    void testLinkScenariosToRealDevicesSuccess() {
        UUID sharedId = UUID.randomUUID();

        SmartDevice realDevice = mock(SmartDevice.class);
        when(realDevice.getId()).thenReturn(sharedId);

        Room room = mock(Room.class);
        when(room.getSmartDevices()).thenReturn(List.of(realDevice));

        SmartDevice clonedDevice = mock(SmartDevice.class);
        when(clonedDevice.getId()).thenReturn(sharedId);

        DeviceAction action = new DeviceAction(clonedDevice, "turnOn", null);

        List<Action> scenarioActions = new ArrayList<>();
        scenarioActions.add(action);

        Scenario scenario = mock(Scenario.class);
        when(scenario.getActions()).thenReturn(scenarioActions);

        PersistenceManager.SmartHomeData data = new PersistenceManager.SmartHomeData(
                List.of(room),
                List.of(scenario)
        );

        PersistenceManager.linkScenariosToRealDevices(data);

        Action updatedAction = scenarioActions.get(0);
        assertInstanceOf(DeviceAction.class, updatedAction);

        DeviceAction updatedDeviceAction = (DeviceAction) updatedAction;
        assertEquals(realDevice, updatedDeviceAction.getTargetDevice(),
                "Das TargetDevice muss durch das echte Gerät aus dem Raum ersetzt worden sein");
    }

    @Test
    void testLinkScenariosToRealDevicesDeviceNotFound() {
        UUID realId = UUID.randomUUID();
        UUID cloneId = UUID.randomUUID();

        SmartDevice realDevice = mock(SmartDevice.class);
        when(realDevice.getId()).thenReturn(realId);

        Room room = mock(Room.class);
        when(room.getSmartDevices()).thenReturn(List.of(realDevice));

        SmartDevice clonedDevice = mock(SmartDevice.class);
        when(clonedDevice.getId()).thenReturn(cloneId);

        DeviceAction action = new DeviceAction(clonedDevice, "turnOn", null);

        List<Action> scenarioActions = new ArrayList<>();
        scenarioActions.add(action);

        Scenario scenario = mock(Scenario.class);
        when(scenario.getActions()).thenReturn(scenarioActions);

        PersistenceManager.SmartHomeData data = new PersistenceManager.SmartHomeData(
                List.of(room),
                List.of(scenario)
        );

        PersistenceManager.linkScenariosToRealDevices(data);

        Action notUpdatedAction = scenarioActions.get(0);
        assertEquals(clonedDevice, ((DeviceAction) notUpdatedAction).getTargetDevice(),
                "Das TargetDevice darf nicht ausgetauscht werden, wenn die ID nicht gefunden wird");
    }

    @Test
    void testLinkScenariosToRealDevices_NullScenarios() {
        PersistenceManager.SmartHomeData data = new PersistenceManager.SmartHomeData(new ArrayList<>(), null);
        PersistenceManager.linkScenariosToRealDevices(data);
        assertNull(data.scenarios);
    }
}