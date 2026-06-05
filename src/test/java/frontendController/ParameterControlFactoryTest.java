package frontendController;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import interfaces.DeviceFunction;
import interfaces.SmartDevice;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParameterControlFactoryTest {

    @Mock
    private SmartDevice mockDevice;

    @Mock
    private DeviceFunction mockFunction;

    @BeforeAll
    static void initToolkit() {
        //startet das JavaFX Toolkit für die UI-Komponenten (Slider, CheckBox etc.)
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            //ignorieren, falls das Toolkit bereits laeuft
        }
    }

    @Test
    void testCreateControlNullInputsReturnsErrorTextField() {
        Node result1 = ParameterControlFactory.createControl(null, "TestFunktion", "");
        Node result2 = ParameterControlFactory.createControl(mockDevice, null, "");

        assertInstanceOf(TextField.class, result1);
        assertEquals("Kein Gerät / Kein Funktionsname", ((TextField) result1).getText());
        assertInstanceOf(TextField.class, result2);
        assertEquals("Kein Gerät / Kein Funktionsname", ((TextField) result2).getText());
    }

    @Test
    void testCreateControlFunctionNotFoundReturnsErrorTextField() {
        when(mockDevice.getFunction("Unbekannt")).thenReturn(null);

        Node result = ParameterControlFactory.createControl(mockDevice, "Unbekannt", "");

        assertInstanceOf(TextField.class, result);
        assertEquals("Funktion: Unbekannt nicht gefunden.", ((TextField) result).getText());
    }

    @Test
    void testCreateControlBooleanReturnsCheckBox() {
        when(mockDevice.getFunction("Schalten")).thenReturn(mockFunction);
        org.mockito.Mockito.doReturn(Boolean.class).when(mockFunction).getParameterType();
        when(mockFunction.getState()).thenReturn(true);

        Node result = ParameterControlFactory.createControl(mockDevice, "Schalten", "");

        assertInstanceOf(CheckBox.class, result, "Sollte eine CheckBox zurückgeben");
        CheckBox checkBox = (CheckBox) result;
        assertTrue(checkBox.isSelected(), "CheckBox sollte initial aktiviert sein (weil getState() = true)");
    }

    @Test
    void testCreateControlDoubleWithMinMaxReturnsSlider() {
        when(mockDevice.getFunction("Dimmen")).thenReturn(mockFunction);
        org.mockito.Mockito.doReturn(Double.class).when(mockFunction).getParameterType();
        when(mockFunction.getMin()).thenReturn(0.0);
        when(mockFunction.getMax()).thenReturn(100.0);

        Node result = ParameterControlFactory.createControl(mockDevice, "Dimmen", "50.0");

        assertInstanceOf(Slider.class, result, "Sollte einen Slider zurückgeben, wenn Min/Max existieren");
        Slider slider = (Slider) result;
        assertEquals(0.0, slider.getMin());
        assertEquals(100.0, slider.getMax());
        assertEquals(50.0, slider.getValue(), "Initialer Wert sollte auf 50.0 gesetzt sein");
    }

    @Test
    void testCreateControlDoubleWithoutMinMaxReturnsTextField() {
        when(mockDevice.getFunction("Temperatur")).thenReturn(mockFunction);
        org.mockito.Mockito.doReturn(Double.class).when(mockFunction).getParameterType();
        when(mockFunction.getMin()).thenReturn(null); // Kein Min/Max vorhanden
        when(mockFunction.getMax()).thenReturn(null);

        Node result = ParameterControlFactory.createControl(mockDevice, "Temperatur", "22.5");

        assertInstanceOf(TextField.class, result, "Sollte als Fallback ein TextField zurückgeben");
        TextField textField = (TextField) result;
        assertEquals("22.5", textField.getText());
    }

    @Test
    void testCreateControlColorReturnsColorPicker() {
        when(mockDevice.getFunction("Lichtfarbe")).thenReturn(mockFunction);
        org.mockito.Mockito.doReturn(Color.class).when(mockFunction).getParameterType();

        Node result = ParameterControlFactory.createControl(mockDevice, "Lichtfarbe", "#FF0000"); // Rot

        assertInstanceOf(ColorPicker.class, result, "Sollte einen ColorPicker zurückgeben");
        ColorPicker colorPicker = (ColorPicker) result;
        assertEquals(Color.web("#FF0000"), colorPicker.getValue(), "Der initialValue muss korrekt geparst werden");
    }

    @Test
    void testGetValueFromControlSlider() {
        Slider slider = new Slider(0, 10, 7.5);
        Object value = ParameterControlFactory.getValueFromControl(slider);

        assertEquals(7.5, value);
    }

    @Test
    void testGetValueFromControlCheckBox() {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(true);
        Object value = ParameterControlFactory.getValueFromControl(checkBox);

        assertEquals(true, value);
    }

    @Test
    void testGetValueFromControlColorPicker() {
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(Color.color(0.0, 0.0, 1.0));

        Object value = ParameterControlFactory.getValueFromControl(colorPicker);

        assertEquals("#0000FF", value);
    }

    @Test
    void testGetValueFromControlComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("Option A", "Option B");
        comboBox.setValue("Option B");

        Object value = ParameterControlFactory.getValueFromControl(comboBox);

        assertEquals("Option B", value);
    }

    @Test
    void testGetValueFromControlUnknownControl() {
        Label label = new Label("Test");
        Object value = ParameterControlFactory.getValueFromControl(label);

        assertEquals("", value, "Bei unbekannten Controls soll ein leerer String zurückgegeben werden");
    }
}