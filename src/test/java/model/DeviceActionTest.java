package model;

import interfaces.DeviceFunction;
import interfaces.SmartDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceActionTest {

    @Mock
    private SmartDevice mockDevice;

    @Mock
    private DeviceFunction mockFunction;

    private final String testFunctionName = "Testfunktion";

    @BeforeEach
    void setUp() {
        lenient().when(mockDevice.getFunction(testFunctionName)).thenReturn(mockFunction);
        lenient().when(mockDevice.getName()).thenReturn("Test Gerät");
    }

    @Test
    void testConstructorAndGetters() {
        Object param = true;
        DeviceAction action = new DeviceAction(mockDevice, testFunctionName, param);

        assertEquals(mockDevice, action.getTargetDevice());
        assertEquals(testFunctionName, action.getFunctionName());
        assertEquals(param, action.getParameter());
    }

    @Test
    void testExecuteWithBooleanParameter() {
        DeviceAction action = new DeviceAction(mockDevice, testFunctionName, true);

        //Simulation, dass der alte Zustand "false" war
        doReturn(Boolean.class).when(mockFunction).getParameterType();
        when(mockFunction.getState()).thenReturn(false);
        action.execute();

        verify(mockDevice).executeFunction(testFunctionName, true);
    }

    @Test
    void testExecuteWithDoubleParameter() {
        String functionName = "Temperatur";
        DeviceAction action = new DeviceAction(mockDevice, functionName, 22.5);

        when(mockDevice.getFunction(functionName)).thenReturn(mockFunction);
        doReturn(Double.class).when(mockFunction).getParameterType();
        when(mockFunction.getValue()).thenReturn(19.0);
        action.execute();

        verify(mockDevice).executeFunction(functionName, 22.5);
    }

    @Test
    void testExecuteWithOtherParameterType() {
        String functionName = "Farbe";
        DeviceAction action = new DeviceAction(mockDevice, functionName, "RED");

        when(mockDevice.getFunction(functionName)).thenReturn(mockFunction);
        doReturn(String.class).when(mockFunction).getParameterType();
        when(mockFunction.getColor()).thenReturn("BLUE");
        action.execute();

        verify(mockDevice).executeFunction(functionName, "RED");
    }

    @Test
    void testExecuteFunctionIsNull() {
        //Simulation, dass das Geraet die angefragte Funktion nicht kennt (gibt null zurück)
        when(mockDevice.getFunction(testFunctionName)).thenReturn(null);
        DeviceAction action = new DeviceAction(mockDevice, testFunctionName, "Parameter");
        action.execute();

        verify(mockDevice).executeFunction(testFunctionName, "Parameter");
    }

    @Test
    void testUndoWithPreviousParameter() {
        DeviceAction action = new DeviceAction(mockDevice, testFunctionName, true);
        doReturn(Boolean.class).when(mockFunction).getParameterType();
        when(mockFunction.getState()).thenReturn(false);
        action.execute();
        action.undo();

        //Funktion muss nun mit  alten Wert (false) aufgerufen werden
        verify(mockDevice).executeFunction(testFunctionName, false);
    }

    @Test
    void testUndoWithoutPreviousParameter() {
        DeviceAction action = new DeviceAction(mockDevice, testFunctionName, true);
        action.undo();

        verify(mockDevice, never()).executeFunction(any(), any());
    }

    @Test
    void testGetDescription() {
        DeviceAction action = new DeviceAction(mockDevice, testFunctionName, "ON");
        String description = action.getDescription();

        assertEquals("Testfunktion (unbekannt ➜ ON)", description);
    }
}