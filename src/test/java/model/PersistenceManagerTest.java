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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistenceManagerTest {

    // JUnit erstellt automatisch einen temporaeren Ordner
    @TempDir
    Path tempDir;

    private File tempFile;
    private final String originalFileName = "smarthome_config.json"; // Standard-Name merken

    @BeforeEach
    void setUp() {
        // Leite die Speicher-Datei in unseren temporaeren Ordner um
        tempFile = tempDir.resolve("test_config.json").toFile();
        PersistenceManager.setFileName(tempFile.getAbsolutePath());
    }

    @AfterEach
    void tearDown() {
        // Setze den Dateinamen nach jedem Test zurueck, um Seiteneffekte zu vermeiden
        PersistenceManager.setFileName(originalFileName);
    }

    @Test
    void testSaveAndLoad_EmptyData() {
        // Arrange
        List<Room> rooms = new ArrayList<>();
        List<Scenario> scenarios = new ArrayList<>();

        // Act: Speichern
        PersistenceManager.save(rooms, scenarios);

        // Assert: Pruefen, ob die Datei erstellt wurde
        assertTrue(tempFile.exists(), "Die JSON-Datei sollte erstellt worden sein");

        // Act: Laden
        PersistenceManager.SmartHomeData loadedData = PersistenceManager.load(tempFile.getAbsolutePath());

        // Assert: Pruefen, ob die Daten korrekt (und leer) geladen wurden
        assertNotNull(loadedData);
        assertNotNull(loadedData.rooms);
        assertNotNull(loadedData.scenarios);
        assertTrue(loadedData.rooms.isEmpty());
        assertTrue(loadedData.scenarios.isEmpty());
    }

    @Test
    void testLoad_FileDoesNotExist() {
        // Act
        // Uebergabe eines Pfads, den es garantiert nicht gibt
        PersistenceManager.SmartHomeData loadedData = PersistenceManager.load(tempDir.resolve("gibt_es_nicht.json").toString());

        // Assert
        assertNull(loadedData, "Wenn die Datei nicht existiert, soll null zurückgegeben werden");
    }

    @Test
    void testLoad_InvalidJsonFormat() throws IOException {
        // Arrange
        // Absichtlich kaputtes JSON in die Datei schreiben
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("{ kaputtes json Format ]");
        }

        // Act
        PersistenceManager.SmartHomeData loadedData = PersistenceManager.load(tempFile.getAbsolutePath());

        // Assert
        assertNull(loadedData, "Bei einer Exception (JsonSyntaxException) soll null zurückgegeben werden");
    }

    @Test
    void testLinkScenariosToRealDevices_Success() {
        // Arrange
        UUID sharedId = UUID.randomUUID();

        // 1. Das "echte" Geraet im Raum vorbereiten
        SmartDevice realDevice = mock(SmartDevice.class);
        when(realDevice.getId()).thenReturn(sharedId);

        Room room = mock(Room.class);
        when(room.getSmartDevices()).thenReturn(List.of(realDevice));

        // 2. Den "Klon" (aus der JSON geladen) in der Aktion vorbereiten
        SmartDevice clonedDevice = mock(SmartDevice.class);
        when(clonedDevice.getId()).thenReturn(sharedId);

        DeviceAction action = new DeviceAction(clonedDevice, "turnOn", null);

        // Da die Liste im Scenario veraendert wird (actions.set), muss sie modifizierbar sein
        List<Action> scenarioActions = new ArrayList<>();
        scenarioActions.add(action);

        Scenario scenario = mock(Scenario.class);
        when(scenario.getActions()).thenReturn(scenarioActions);

        // 3. Das Data-Objekt bauen
        PersistenceManager.SmartHomeData data = new PersistenceManager.SmartHomeData(
                List.of(room),
                List.of(scenario)
        );

        // Act
        PersistenceManager.linkScenariosToRealDevices(data);

        // Assert
        // Die Action in der Liste sollte durch eine neue ersetzt worden sein,
        // die nun auf 'realDevice' zeigt statt auf 'clonedDevice'
        Action updatedAction = scenarioActions.get(0);
        assertTrue(updatedAction instanceof DeviceAction);

        DeviceAction updatedDeviceAction = (DeviceAction) updatedAction;
        assertEquals(realDevice, updatedDeviceAction.getTargetDevice(),
                "Das TargetDevice muss durch das echte Gerät aus dem Raum ersetzt worden sein");
    }

    @Test
    void testLinkScenariosToRealDevices_DeviceNotFound() {
        // Arrange
        UUID realId = UUID.randomUUID();
        UUID cloneId = UUID.randomUUID(); // Absichtlich andere ID

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

        // Act
        PersistenceManager.linkScenariosToRealDevices(data);

        // Assert
        // Da die IDs nicht uebereinstimmen, darf das Geraet nicht ersetzt werden
        Action notUpdatedAction = scenarioActions.get(0);
        assertEquals(clonedDevice, ((DeviceAction) notUpdatedAction).getTargetDevice(),
                "Das TargetDevice darf nicht ausgetauscht werden, wenn die ID nicht gefunden wird");
    }

    @Test
    void testLinkScenariosToRealDevices_NullScenarios() {
        // Arrange
        PersistenceManager.SmartHomeData data = new PersistenceManager.SmartHomeData(new ArrayList<>(), null);

        // Act
        // Darf keine NullPointerException werfen
        PersistenceManager.linkScenariosToRealDevices(data);

        // Assert
        assertNull(data.scenarios);
    }
}