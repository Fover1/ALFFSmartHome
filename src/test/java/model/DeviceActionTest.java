package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceActionTest {

    @Mock
    private SmartDevice mockDevice;

    @Mock
    private DeviceFunction mockFunction;

    private final String testFunctionName = "setPower";

    @BeforeEach
    void setUp() {
        // Da wir lenient() nutzen, meckert Mockito nicht, wenn in manchen Tests
        // dieser Mock-Aufruf gar nicht gebraucht wird.
        lenient().when(mockDevice.getFunction(testFunctionName)).thenReturn(mockFunction);
        lenient().when(mockDevice.getName()).thenReturn("TestGerät");
    }

    @Test
    void testConstructorAndGetters() {
        // Arrange
        Object param = true;

        // Act
        DeviceAction action = new DeviceAction(mockDevice, testFunctionName, param);

        // Assert
        assertEquals(mockDevice, action.getTargetDevice());
        assertEquals(testFunctionName, action.getFunctionName());
        assertEquals(param, action.getParameter());
    }

    @Test
    void testExecute_WithBooleanParameter() {
        // Arrange
        DeviceAction action = new DeviceAction(mockDevice, testFunctionName, true);

        // Simuliere, dass der alte Zustand "false" war
        doReturn(Boolean.class).when(mockFunction).getParameterType();
        when(mockFunction.getState()).thenReturn(false);

        // Act
        action.execute();

        // Assert
        // Die Funktion muss am Gerät mit dem neuen Parameter (true) aufgerufen werden
        verify(mockDevice).executeFunction(testFunctionName, true);
    }

    @Test
    void testExecute_WithDoubleParameter() {
        // Arrange
        String functionName = "setTemperature";
        DeviceAction action = new DeviceAction(mockDevice, functionName, 22.5);

        when(mockDevice.getFunction(functionName)).thenReturn(mockFunction);
        doReturn(Double.class).when(mockFunction).getParameterType();
        when(mockFunction.getValue()).thenReturn(19.0);

        // Act
        action.execute();

        // Assert
        verify(mockDevice).executeFunction(functionName, 22.5);
    }

    @Test
    void testExecute_WithOtherParameterType() {
        // Arrange (Test für den "else"-Block in der execute-Methode, z.B. String für Color)
        String functionName = "setColor";
        DeviceAction action = new DeviceAction(mockDevice, functionName, "RED");

        when(mockDevice.getFunction(functionName)).thenReturn(mockFunction);
        doReturn(String.class).when(mockFunction).getParameterType();
        when(mockFunction.getColor()).thenReturn("BLUE");

        // Act
        action.execute();

        // Assert
        verify(mockDevice).executeFunction(functionName, "RED");
    }

    @Test
    void testExecute_FunctionIsNull() {
        // Arrange
        // Simuliere, dass das Gerät die angefragte Funktion nicht kennt (gibt null zurück)
        when(mockDevice.getFunction(testFunctionName)).thenReturn(null);
        DeviceAction action = new DeviceAction(mockDevice, testFunctionName, "Parameter");

        // Act
        action.execute();

        // Assert
        // executeFunction wird trotzdem auf dem Gerät aufgerufen
        // (das wirft in eurer AbstractDevice-Logik dann eine Exception,
        // aber das ist Sache des Geräts, nicht der Action)
        verify(mockDevice).executeFunction(testFunctionName, "Parameter");
    }

    @Test
    void testUndo_WithPreviousParameter() {
        // Arrange: Erst execute() aufrufen, um den previousParameter zu setzen
        DeviceAction action = new DeviceAction(mockDevice, testFunctionName, true);

        doReturn(Boolean.class).when(mockFunction).getParameterType();
        when(mockFunction.getState()).thenReturn(false); // Der alte Wert ist false

        action.execute(); // Speichert false als previousParameter

        // Act
        action.undo();

        // Assert
        // Die Funktion muss nun mit dem alten Wert (false) aufgerufen werden
        verify(mockDevice).executeFunction(testFunctionName, false);
    }

    @Test
    void testUndo_WithoutPreviousParameter() {
        // Arrange
        DeviceAction action = new DeviceAction(mockDevice, testFunctionName, true);

        // Wenn execute() nie aufgerufen wurde, ist previousParameter null.
        // Act
        action.undo();

        // Assert
        // executeFunction darf nicht aufgerufen werden, wenn previousParameter null ist
        verify(mockDevice, never()).executeFunction(any(), any());
    }

    @Test
    void testGetDescription() {
        // Arrange
        DeviceAction action = new DeviceAction(mockDevice, testFunctionName, "ON");

        // Act
        String description = action.getDescription();

        // Assert
        assertEquals("TestGerät ➜ setPower ON", description);
    }
}