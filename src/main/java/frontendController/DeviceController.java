package frontendController;

import controller.SmartHomeAppController;
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
import model.AbstractDevice;
import model.DeviceFunction;
import model.Room;

import java.util.List;
import java.util.Optional;


public class DeviceController {
    private SmartHomeAppController smartHomeAppController;
    private AbstractDevice device;
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

    public void setData(AbstractDevice device, SmartHomeAppController appController, Room selectedRoom) {
        this.device = device;
        this.smartHomeAppController = appController;
        this.selectedRoom = selectedRoom;
        updateUI();
    }

    private void updateUI() {

        List<Room> roomlist = smartHomeAppController.getAllRooms();
        deviceGrid.getChildren().clear();

        deviceName.setText("Gerät: " + device.getName());
        deviceType.setText("Gerätetype: " + device.getDeviceType());
        roomLabel.setText("Raum: " + selectedRoom.getName());

        int functionCounter = 0;
        for (String functionName : device.getAvailableFunctions()) {
            DeviceFunction func = device.getFunctions().get(functionName);
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
            device.executeFunction(functionName, hex);
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
            device.executeFunction(functionName, checkBox.isSelected());
            valueLabel.setText(checkBox.isSelected() ? "Eingeschaltet" : "Ausgeschaltet");
            smartHomeAppController.save();
        });
    }

    private void setupControllButtons() {
        changeDeviceName.setOnAction(e -> {
            /// todo: kann man das dialog fenster nicht in eine andere Klasse machen, damit man das nicht 5x den sleeben code im projekt hat?
            System.out.println("changeDeviceName.setOnAction");
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Name ändern: " + device.getName());
            dialog.setHeaderText(String.format("Bitte gebe einen neuen Namen für den Raum \"%s\" ein", device.getName()));

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(roomName -> {
                if (!roomName.trim().isEmpty()) {
                    smartHomeAppController.changeDeviceName(device, roomName);
                    System.out.println("Raumname geändert: " + roomName);
                    smartHomeAppController.save();
                    updateUI();
                }
            });
        });

        deleteDevice.setOnAction(e -> {
            smartHomeAppController.deleteDevice(device, selectedRoom);
            System.out.println("Geräte wird gelsöcht");
            smartHomeAppController.save();
        });
        /// todo: die platzierung der buttons überzeugt mich noch nicht gnaz / lable als erklärung hinzufügen was das kann :)
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

        /// todo: die ui muss sich noch aktualisieren, dass das gerät im neuen raum ist (also im Room view) vllt mit nem observer?
        changeDeviceRoom.setOnAction(e -> {
            Room room = changeDeviceRoom.getValue();
            if (room != null) {
                System.out.println("dieser raum wurde ausgewähl" + room.getName());
                /// todo: was davon braucht man wirklich? (raum und gerät sind doppelt verbunden, am ende nochmal nachschaneun)
                smartHomeAppController.changeDeviceRoom(device, selectedRoom, room);
                /// todo: device wird erst im neuen Raum angezeigt, wenn man einen anderen Raum auswählt
                smartHomeAppController.getAllRooms().stream()
                        .filter(r -> r.getName().equals(room.getName()))
                        .findFirst()
                        .ifPresent(r -> r.addDevice(device));
                smartHomeAppController.save();
            }
            updateUI();

        });
    }

    private void executeSliderValue(String functionName, Slider slider) {
        Object param = slider.getValue();

        device.executeFunction(functionName, param);
        smartHomeAppController.save();
        System.out.println("Aktion ausgeführt, da Slider losgelassen wurde.");
    }
}
