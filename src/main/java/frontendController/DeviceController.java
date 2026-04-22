package frontendController;

import controller.SmartHomeAppController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import model.AbstractDevice;
import model.DeviceFunction;


public class DeviceController {
    private SmartHomeAppController smartHomeAppController;
    private AbstractDevice device;

    @FXML
    private ScrollPane rootPane;

//    @FXML
//   e private FlowPane deviceGrid;

    @FXML
    private GridPane deviceGrid;

    @FXML
    private Label roomLable;

    @FXML
    public void initialize() {
//        rootPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
//            if (newScene != null) {
//                newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
//                    if (newWindow != null) {
//                        newWindow.setOnCloseRequest(windowEvent -> {
//                            handleClose(windowEvent);
//                        });
//                    }
//                });
//            }
//        });
    }

    private void handleClose(WindowEvent event) {
        System.out.println("DeviceFenster wurde geschlossen");
        smartHomeAppController.save();
    }

    public void setDevice(AbstractDevice device, SmartHomeAppController appController) {
        this.device = device;
        this.smartHomeAppController = appController;

        updateUI();
    }

    private void updateUI() {

        deviceGrid.getChildren().clear();

        roomLable.setText("Raum: " + device.getRoom().getName());

        int functionCounter = 0;
        for (String functionName : device.getAvailableFunctions()) {
            DeviceFunction func = device.getFunctions().get(functionName);


            Label name = new Label(functionName + ": ");
            deviceGrid.add(name, 0, functionCounter);
            System.out.println("Beschreibung: " + functionName);

            //überprüfen, was für eine Art parameter zurückgegeben werden muss
            //Checkbox for Boolean
            if (func.getParameterType() == Boolean.class) {
                Boolean isOn = func.getState();
                CheckBox checkBox = new CheckBox();
                Label state = new Label(isOn ? "An" : "Aus");
                deviceGrid.add(state, 2, functionCounter);
                if (isOn) {
                    checkBox.setSelected(true);
                }
                checkBox.setOnAction(e -> {
                    device.executeFunction(functionName, checkBox.isSelected());
                    state.setText(checkBox.isSelected() ? "An" : "Aus");
                    smartHomeAppController.save();
                });
                deviceGrid.add(checkBox, 1, functionCounter);
//                String aktiv = isOn ? "An" : "Aus";


            }

            // Schieberegler for Integer
            else if (Number.class.isAssignableFrom(func.getParameterType()) && func.getMin() != null) {
                Slider slider = new Slider(func.getMin(), func.getMax(), func.getMin());
                slider.setShowTickLabels(true);

                /// todo: nach dem kommer noch eine Zahl?
                int maxLength = func.getValue().toString().length();
                maxLength = Math.min(maxLength, 4);
                Label value = new Label(func.getValue().toString().substring(0, maxLength) + " " + func.getUnit());
                deviceGrid.add(value, 2, functionCounter);

                slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                    Object param = (func.getParameterType() == Integer.class) ? newVal.intValue() : newVal.doubleValue();
                    device.executeFunction(functionName, param);

                    /// todo: die nächsten 2 Zeilen doppeln sich, kann man das noch schöner lösen?
                    int length = func.getValue().toString().length();
                    length = Math.min(length, 4);
//                    Label value = new Label(
                    value.setText(func.getValue().toString().substring(0, length) + " " + func.getUnit());
                    smartHomeAppController.save();
                });

                slider.setValue(func.getValue());

                deviceGrid.add(slider, 1, functionCounter);
//                deviceGrid.add(value, 2, device.);
//                deviceGrid.getChildren().add(new Label(func.getUnit()));
            } else if (Color.class.isAssignableFrom(func.getParameterType())) {
                ColorPicker colorPicker = new ColorPicker();
                colorPicker.setValue(Color.valueOf(func.getColor()));

                String color = func.getColor();
                Label value = new Label(color);
                deviceGrid.add(value, 2, functionCounter);

                /// todo: aktuell ausgewählte Frabe in den Picker laden

                colorPicker.setOnAction(e -> {
                    Color c = colorPicker.getValue();
                    //formatierung von JavaFX Color zu Hexcode
                    String hex = String.format("#%02X%02X%02X",
                            (int) (c.getRed() * 255),
                            (int) (c.getGreen() * 255),
                            (int) (c.getBlue() * 255));

                    device.executeFunction(functionName, hex);
                    value.setText(hex);
                    smartHomeAppController.save();
                });

                deviceGrid.add(colorPicker, 1, functionCounter);
            }
            functionCounter++;
        }
    }
}
