package frontendController;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import model.AbstractDevice;
import model.DeviceFunction;

//da die Parametereingaben in verschiedenen Anwendungsfällen benötigt werden, werden diese hier Zentral erstellt
//sollte ein neuer Parametertyp vorkommen, kann man diesen hier implementieren und somit können die verschiedenen Klassen diesen neuen typen "automatisch" handeln
public class ParameterControlFactory {

    //Node ist wie ein Abstract Device für Frontend Kopmonenten (also z.B. Slider, Checkbox und so sind alles Nodes)
    public static Node createControl(AbstractDevice device, String functionName, String initialValue) {
//        if (device == null || functionName == null) {
//            return new TextField();
//        }

        DeviceFunction function = device.getFunctions().get(functionName);

        Class<?> paramType = function.getParameterType();

        if (paramType == Boolean.class || paramType == boolean.class) {
            return createBooelanControl(function);
        }

        if (paramType == Double.class || paramType == double.class) {
            return createDoubleControl(initialValue, function);
        }

        if (paramType == Color.class) {
            return createColorControl(initialValue);
        }
        return null;
    }

    private static Control createDoubleControl(String initialValue, DeviceFunction function) {
        Double min = function.getMin();
        Double max = function.getMax();

        if (min != null && max != null) {
            double defaultVal = min;
            if (initialValue != null && !initialValue.isEmpty()) {
                try {
                    defaultVal = Double.parseDouble(initialValue);
                } catch (NumberFormatException ignored) {
                }
            }

            Slider slider = new Slider(min, max, defaultVal);
            slider.setShowTickLabels(true);
            slider.setShowTickMarks(true);
            slider.setMajorTickUnit((max - min) / 5.0);
            return slider;
        } else {
            TextField doubleField = new TextField();
            doubleField.setText(initialValue != null ? initialValue : "0.0");
            return doubleField;
        }
    }

    private static ColorPicker createColorControl(String initialValue) {
        ColorPicker picker = new ColorPicker();
        if (initialValue != null && !initialValue.isEmpty()) {
            try {
                picker.setValue(Color.web(initialValue));
            } catch (Exception ignored) {
            }
        }
        return picker;
    }

    private static CheckBox createBooelanControl(DeviceFunction function) {
        Boolean isOn = function.getState();
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(isOn);
        return checkBox;
    }


    public static String getValueFromControl(Node control) {
        if (control instanceof Slider slider) {
            return String.format("%.1f", slider.getValue());
        } else if (control instanceof ColorPicker picker) {
            System.out.println("ColorPicker value: " + picker.getValue().toString());
            return "#" + picker.getValue().toString().substring(2, 8).toUpperCase();
        } else if (control instanceof ComboBox<?> combo) {
            return combo.getValue() != null ? combo.getValue().toString() : "";
        }
        return "";
    }
}