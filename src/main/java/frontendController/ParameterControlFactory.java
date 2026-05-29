package frontendController;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import model.DeviceFunction;
import model.SmartDevice;

//da die Parametereingaben in verschiedenen Anwendungsfällen benötigt werden, werden diese hier Zentral erstellt
//sollte ein neuer Parametertyp vorkommen, kann man diesen hier implementieren und somit können die verschiedenen Klassen diesen neuen typen "automatisch" handeln
public class ParameterControlFactory {

    //Node ist wie ein Abstract Device für Frontend Kopmonenten (also z.B. Slider, Checkbox und so sind alles Nodes)
    public static Node createControl(SmartDevice device, String functionName, String initialValue) {
        if (device == null || functionName == null) {
            return new TextField("kein device / functionname");
        }

////        DeviceFunction function = device.getFunctions().get(functionName);
//        List<String> functions = device.getAvailableFunctions();
//        for (int i = 0; i < functions.size(); i++) {}
//        DeviceFunction function = device.getAvailableFunctions().get

//        DeviceFunction function = device.getFunctions().get(i)

//        List<DeviceFunction> functions = device.getFunctions();
//        DeviceFunction function = null;
//        for (DeviceFunction function1 : functions) {
//            if (function1.getClass().getSimpleName().equals(functionName)) {
//                System.out.println(functionName.getClass().getSimpleName());
//                function = function1;
//            }
//        }

        DeviceFunction function = device.getFunction(functionName);

        if (function == null) {
            return new TextField("funcktion: " + functionName + " nicht gefunden");
        }

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


    public static Object getValueFromControl(Node control) {
        if (control instanceof Slider slider) {
            return slider.getValue();
        } else if (control instanceof ColorPicker picker) {
            System.out.println("ColorPicker value: " + picker.getValue().toString());
            Color c = picker.getValue();
            return String.format("#%02X%02X%02X",
                    (int) (c.getRed() * 255),
                    (int) (c.getGreen() * 255),
                    (int) (c.getBlue() * 255));
        } else if (control instanceof ComboBox<?> combo) {
            return combo.getValue();
        } else if (control instanceof CheckBox check) {
            return check.isSelected();
        }
        return "";
    }
}