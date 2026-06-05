package model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogEntryTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        // Leitet System.out in unseren Captor um, damit wir den println-Befehl im Konstruktor testen können
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        // Stellt den normalen Konsolen-Output nach jedem Test wieder her
        System.setOut(standardOut);
    }

    @Test
    void testPrimaryConstructorAndAccessors() {
        // Arrange
        LocalDateTime time = LocalDateTime.of(2023, 10, 5, 14, 30, 15);

        // Act
        LogEntry entry = new LogEntry(time, "Morgenroutine", "Kaffeemaschine", "Einschalten", "Erfolg");

        // Assert
        // Bei Records heißen die Accessor-Methoden genau wie die Felder (ohne "get")
        assertEquals(time, entry.timeStamp());
        assertEquals("Morgenroutine", entry.scenarioName());
        assertEquals("Kaffeemaschine", entry.deviceName());
        assertEquals("Einschalten", entry.action());
        assertEquals("Erfolg", entry.result());
    }

    @Test
    void testSecondaryConstructor_SetsCurrentTimeAndPrintsToSystemOut() {
        // Act
        LogEntry entry = new LogEntry("Gute Nacht", "Deckenlicht", "Ausschalten", "Erfolg");

        // Assert
        assertNotNull(entry.timeStamp(), "Der Zeitstempel sollte automatisch gesetzt werden");
        assertEquals("Gute Nacht", entry.scenarioName());
        assertEquals("Deckenlicht", entry.deviceName());
        assertEquals("Ausschalten", entry.action());
        assertEquals("Erfolg", entry.result());

        // Prüfen, ob das System.out.println(this) funktioniert hat
        String printedOutput = outputStreamCaptor.toString().trim();
        assertTrue(printedOutput.contains("Szenario: 'Gute Nacht'"));
        assertTrue(printedOutput.contains("Gerät: 'Deckenlicht'"));
    }

    @Test
    void testToString_FormatsCorrectly() {
        // Arrange
        // Wir nutzen eine feste Uhrzeit, um das String-Format genau prüfen zu können
        LocalDateTime time = LocalDateTime.of(2023, 1, 1, 9, 5, 9);
        LogEntry entry = new LogEntry(time, "TestSzenario", "TestGerät", "TestAktion", "TestResultat");

        // Act
        String resultString = entry.toString();

        // Assert
        // Erwartet wird das Format HH:mm:ss, also [09:05:09]
        String expectedString = "[09:05:09] Szenario: 'TestSzenario' | Gerät: 'TestGerät' | Aktion: 'TestAktion' ➜ TestResultat";
        assertEquals(expectedString, resultString);
    }
}