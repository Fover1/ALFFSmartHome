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
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    void testPrimaryConstructorAndAccessors() {
        LocalDateTime time = LocalDateTime.of(2026, 5, 5, 14, 30, 15);
        LogEntry entry = new LogEntry(time, "Morgenroutine", "Kaffeemaschine", "Einschalten", "Erfolg");

        assertEquals(time, entry.timeStamp());
        assertEquals("Morgenroutine", entry.scenarioName());
        assertEquals("Kaffeemaschine", entry.deviceName());
        assertEquals("Einschalten", entry.action());
        assertEquals("Erfolg", entry.result());
    }

    @Test
    void testSecondaryConstructorSetsCurrentTimeAndPrintsToSystemOut() {
        LogEntry entry = new LogEntry("Gute Nacht", "Deckenlicht", "Ausschalten", "Erfolg");

        assertNotNull(entry.timeStamp(), "Der Zeitstempel sollte automatisch gesetzt werden");
        assertEquals("Gute Nacht", entry.scenarioName());
        assertEquals("Deckenlicht", entry.deviceName());
        assertEquals("Ausschalten", entry.action());
        assertEquals("Erfolg", entry.result());

        String printedOutput = outputStreamCaptor.toString().trim();
        assertTrue(printedOutput.contains("Szenario: 'Gute Nacht'"));
        assertTrue(printedOutput.contains("Gerät: 'Deckenlicht'"));
    }

    @Test
    void testToString_FormatsCorrectly() {
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 9, 5, 9);
        LogEntry entry = new LogEntry(time, "TestSzenario", "TestGerät", "TestAktion", "TestResultat");
        String resultString = entry.toString();

        String expectedString = "[09:05:09] Szenario: 'TestSzenario' | Gerät: 'TestGerät' | Aktion: 'TestAktion' ➜ TestResultat";
        assertEquals(expectedString, resultString);
    }
}