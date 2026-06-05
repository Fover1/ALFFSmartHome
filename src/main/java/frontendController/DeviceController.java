package frontendController;

import controller.SmartHomeAppController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import model.DeviceAction;
import model.DeviceFunction;
import model.DeviceObserver;
import model.Room;
import model.SmartDevice;

import java.util.List;
import java.util.Optional;


public class DeviceController implements DeviceObserver {
    private SmartHomeAppController smartHomeAppController;
    private SmartDevice device;
    private Room selectedRoom;

    @FXML
    private GridPane deviceGrid;

    @FXML
    private Label roomLabel;

    @FXML
    private Label deviceName;

    @FXML
    private Label deviceType;

    @FXML
    private Button deleteDevice;

    @FXML
    private Button changeDeviceName;

    @FXML
    private ComboBox<Room> changeDeviceRoom;

    @FXML
    private Label deviceIdLabel;

    public void setData(SmartDevice device, SmartHomeAppController appController, Room selectedRoom) {
        this.device = device;
        this.smartHomeAppController = appController;
        this.selectedRoom = selectedRoom;
        this.device.addObserver(this);
        updateUI();
    }

    private void updateUI() {
        List<Room> roomlist = smartHomeAppController.getAllRooms();
        deviceGrid.getChildren().clear();

        deviceName.setText("Gerät: " + device.getName());
        deviceType.setText("Gerätetype: " + device.getDeviceType());
        roomLabel.setText("Raum: " + selectedRoom.getName());

        if (deviceIdLabel != null) {
            deviceIdLabel.setText("ID: " + device.getId().toString());
        }

        int functionCounter = 0;
        for (String functionName : device.getAvailableFunctions()) {
            DeviceFunction func = device.getFunction(functionName);
            deviceGrid.add(new Label(functionName + ": "), 0, functionCounter);

            String initialValue = "";
            if (func.getParameterType() == Boolean.class) initialValue = String.valueOf(func.getState());
            else if (func.getParameterType() == Double.class) initialValue = String.valueOf(func.getValue());
            else if (func.getParameterType() == Color.class) initialValue = func.getColor();

            //UI Element wird von der Factory erstellt
            Node control = ParameterControlFactory.createControl(device, functionName, initialValue);
            deviceGrid.add(control, 1, functionCounter);

            Label valueLabel = new Label();
            deviceGrid.add(valueLabel, 3, functionCounter);

            initializeLiveListener(functionName, control, valueLabel, func, initialValue);

            functionCounter++;
        }
        ObservableList<Room> rooms = FXCollections.observableArrayList(roomlist);
        setupRoomComboBox(rooms);
        setupControllButtons();
    }

    private void initializeLiveListener(String functionName, Node control, Label valueLabel, DeviceFunction func, String initialValue) {
        if (control instanceof CheckBox checkBox) {
            checkBoxListener(functionName, valueLabel, checkBox);

        } else if (control instanceof Slider slider) {
            sliderListener(functionName, valueLabel, func, slider);

        } else if (control instanceof ColorPicker colorPicker) {
            colorListener(functionName, valueLabel, initialValue, colorPicker);
        }
    }

    private void colorListener(String functionName, Label valueLabel, String initialValue, ColorPicker colorPicker) {
        valueLabel.setText(initialValue);
        colorPicker.setOnAction(e -> {
            Color c = colorPicker.getValue();
            String hex = String.format("#%02X%02X%02X",
                    (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
            DeviceAction action = new DeviceAction(device, functionName, hex);
            smartHomeAppController.executeAndRemember(action);
            valueLabel.setText(hex);
            smartHomeAppController.save();
        });
    }

    private void sliderListener(String functionName, Label valueLabel, DeviceFunction func, Slider slider) {
        String unit = func.getUnit() != null ? func.getUnit() : "";
        valueLabel.setText(String.format("%.2f %s", slider.getValue(), unit));

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            valueLabel.setText(String.format("%.2f %s", newVal.doubleValue(), unit));
        });

        slider.setOnMouseReleased(event -> {
            executeSliderValue(functionName, slider);
        });
    }

    private void checkBoxListener(String functionName, Label valueLabel, CheckBox checkBox) {
        valueLabel.setText(checkBox.isSelected() ? "Eingeschaltet" : "Ausgeschaltet");
        checkBox.setOnAction(e -> {
            DeviceAction action = new DeviceAction(device, functionName, checkBox.isSelected());
            smartHomeAppController.executeAndRemember(action);
            valueLabel.setText(checkBox.isSelected() ? "Eingeschaltet" : "Ausgeschaltet");
            smartHomeAppController.save();
        });
    }

    private void setupControllButtons() {
        changeDeviceName.setOnAction(e -> {
            System.out.println("changeDeviceName.setOnAction");
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Name ändern: " + device.getName());
            dialog.setHeaderText(String.format("Bitte geben Sie einen neuen Namen für das Gerät \"%s\" ein", device.getName()));

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(inputName -> {
                String newDeviceName = inputName.trim();

                if (!newDeviceName.isEmpty() && !newDeviceName.equalsIgnoreCase(device.getName())) {
                    boolean nameExistsInRoom = selectedRoom.getSmartDevices().stream()
                            .anyMatch(d -> d.getName().equalsIgnoreCase(newDeviceName));

                    if (nameExistsInRoom) {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                        alert.setTitle("Name bereits vergeben");
                        alert.setHeaderText("Dieses Gerät existiert bereits in diesem Raum.");
                        alert.setContentText("Ein Gerät mit dem Namen '" + newDeviceName + "' ist in diesem Raum bereits vorhanden. Bitte wähle einen anderen Namen.");
                        alert.showAndWait();
                        return;
                    }
                    smartHomeAppController.changeDeviceName(device, newDeviceName);
                    smartHomeAppController.save();
                    updateUI();
                }
            });
        });

        deleteDevice.setOnAction(e -> {
            smartHomeAppController.deleteDevice(device, selectedRoom);
            smartHomeAppController.save();

            javafx.stage.Stage stage = (javafx.stage.Stage) deleteDevice.getScene().getWindow();
            stage.close();
        });
    }

    private void setupRoomComboBox(ObservableList<Room> rooms) {
        changeDeviceRoom.setItems(rooms);
        changeDeviceRoom.setConverter(new StringConverter<Room>() {
            @Override
            public String toString(Room room) {
                return (room == null) ? "" : room.getName();
            }

            @Override
            public Room fromString(String s) {
                return null;
            }
        });
        changeDeviceRoom.setPromptText(selectedRoom.getName());

        changeDeviceRoom.setOnAction(e -> {
            Room room = changeDeviceRoom.getValue();
            if (room != null && !room.getName().equals(selectedRoom.getName())) {
                smartHomeAppController.changeDeviceRoom(device, selectedRoom, room);
                selectedRoom = room;
                smartHomeAppController.save();
            }
            updateUI();

        });
    }

    private void executeSliderValue(String functionName, Slider slider) {
        Object param = slider.getValue();

        DeviceAction action = new DeviceAction(device, functionName, param);
        smartHomeAppController.executeAndRemember(action);
        smartHomeAppController.save();
    }

    @Override
    public void onStateChanged(SmartDevice device) {
        Platform.runLater(() -> {
            updateUI();
        });
    }
}
