package frontendController;

import controller.SmartHomeAppController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
        Button saveButton = new Button("save");
        saveButton.setOnAction(e -> smartHomeAppController.save());
        deviceGrid.add(saveButton, 0, 0);

        int functionCounter = 1;
        for (String functionName : device.getAvailableFunctions()) {
            DeviceFunction func = device.getFunctions().get(functionName);
            String color = func.getColor();
            System.out.println("akutelle fareb: " + color);

            Label name = new Label(functionName + ": ");
            Label value = new Label(color);
            deviceGrid.add(name, 0, functionCounter);
            System.out.println("Beschreibung: " + functionName);

            //überprüfen, was für eine Art parameter zurückgegeben werden muss
            //Checkbox for Boolean
            if (func.getParameterType() == Boolean.class) {
                CheckBox checkBox = new CheckBox();
                checkBox.setOnAction(e -> device.executeFunction(functionName, checkBox.isSelected()));
                deviceGrid.add(checkBox, 1, functionCounter);
            }

            // Schieberegler for Integer
            else if (Number.class.isAssignableFrom(func.getParameterType()) && func.getMin() != null) {
                Slider slider = new Slider(func.getMin(), func.getMax(), func.getMin());
                slider.setShowTickLabels(true);

                slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                    Object param = (func.getParameterType() == Integer.class) ? newVal.intValue() : newVal.doubleValue();
                    device.executeFunction(functionName, param);
                });

                deviceGrid.add(slider, 1, functionCounter);
//                deviceGrid.add(value, 2, device.);
//                deviceGrid.getChildren().add(new Label(func.getUnit()));
            } else if (Color.class.isAssignableFrom(func.getParameterType())) {
                ColorPicker colorPicker = new ColorPicker();
                colorPicker.setValue(Color.valueOf(color));

                /// todo: aktuell ausgewählte Frabe in den Picker laden

                colorPicker.setOnAction(e -> {
                    Color c = colorPicker.getValue();
                    //formatierung von JavaFX Color zu Hexcode
                    String hex = String.format("#%02X%02X%02X",
                            (int) (c.getRed() * 255),
                            (int) (c.getGreen() * 255),
                            (int) (c.getBlue() * 255));

                    device.executeFunction(functionName, hex);
                });

                deviceGrid.add(colorPicker, 1, functionCounter);
                deviceGrid.add(value, 2, functionCounter);
            }
            functionCounter++;
        }
    }
}
