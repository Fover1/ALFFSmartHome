package frontendController;

import controller.SmartHomeAppController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

    @FXML
    private ScrollPane rootPane;

//    @FXML
//   e private FlowPane deviceGrid;

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
    public void initialize() {
    }

    public void setDevice(AbstractDevice device, SmartHomeAppController appController) {
        this.device = device;
        this.smartHomeAppController = appController;

        updateUI();
    }

    private void updateUI() {

        List<Room> roomlist = smartHomeAppController.getAllRooms();
        deviceGrid.getChildren().clear();

        deviceName.setText("Gerät: " + device.getName());
        deviceType.setText("Gerätetype: " + device.getDeviceType());
        roomLabel.setText("Raum: " + device.getRoom().getName());

        int functionCounter = 0;
        for (String functionName : device.getAvailableFunctions()) {
            DeviceFunction func = device.getFunctions().get(functionName);
            deviceGrid.add(new Label(functionName + ": "), 0, functionCounter);

            //überprüfen, was für eine Art parameter zurückgegeben werden muss
            //Checkbox for Boolean
            if (func.getParameterType() == Boolean.class) {
                createBooleanControl(functionName, func, functionCounter);
            }
            // Schieberegler for Double
            else if (Number.class.isAssignableFrom(func.getParameterType()) && func.getMin() != null) {
                createDoubleControll(functionName, func, functionCounter);
            } else if (Color.class.isAssignableFrom(func.getParameterType())) {
                createColorControl(functionName, func, functionCounter);
            }
            functionCounter++;
        }

        ObservableList<Room> rooms = FXCollections.observableArrayList(roomlist);

        setupRoomComboBox(rooms);

        setupControllButtons();


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
                }
            });
        });

        deleteDevice.setOnAction(e -> {
            smartHomeAppController.deleteDevice(device);
            System.out.println("Geräte wird gelsöcht");
            smartHomeAppController.save();
        });
        /// todo: die platzierung der buttons überzeugt mich noch nicht gnaz
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
        changeDeviceRoom.setPromptText(device.getRoom().getName());

        changeDeviceRoom.setOnAction(e -> {
            Room room = changeDeviceRoom.getValue();
            if (room != null) {
                System.out.println("dieser raum wurde ausgewähl" + room.getName());
                /// todo: was davon braucht man wirklich?
                device.getRoom().removeDevice(device);
                device.setRoom(room);
                device.changeRoom(room);
                smartHomeAppController.getAllRooms().stream()
                        .filter(r -> r.getName().equals(room.getName()))
                        .findFirst()
                        .ifPresent(r -> r.addDevice(device));
                smartHomeAppController.save();
            }
            updateUI();

        });
    }

    private void createColorControl(String functionName, DeviceFunction func, int functionCounter) {
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(Color.valueOf(func.getColor()));

        String color = func.getColor();
        Label value = new Label(color);
        deviceGrid.add(value, 3, functionCounter);

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

    private void createDoubleControll(String functionName, DeviceFunction func, int functionCounter) {
        Slider slider = new Slider(func.getMin(), func.getMax(), func.getMin());
        slider.setShowTickLabels(true);
        slider.setValue(func.getValue());

        /// todo: nach dem kommer noch eine Zahl? (also bei der Stringtrennung aktiv nach dem Kommer suchen)
        Label valueLable = new Label(String.format("%.2f %s", slider.getValue(), func.getUnit()));
        deviceGrid.add(valueLable, 3, functionCounter);

        //Attributanzeige neben dem Slider ändert sich dauernd beim sliden des Sliders
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            valueLable.setText(String.format("%.2f %s", newVal.doubleValue(), func.getUnit()));
        });

        //der neue Attributwert wird allerdings erst gespeichert, sobald der Slider losgelassen wurde
        slider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                executeSliderValue(functionName, slider);
            }
        });
        //wenn slider angeklickt wird funktioniert und nicht verschoben wird reagiert die obige Methode nicht
        slider.onMouseReleasedProperty().addListener((obs, oldVal, newVal) -> {
            executeSliderValue(functionName, slider);
        });
        deviceGrid.add(slider, 1, functionCounter);
    }

    private void createBooleanControl(String functionName, DeviceFunction func, int functionCounter) {
        Boolean isOn = func.getState();
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(isOn);

        Label state = new Label(isOn ? "Eingeschaltet" : "Ausgeschaltet");

        deviceGrid.add(state, 3, functionCounter);

        checkBox.setOnAction(e -> {
            device.executeFunction(functionName, checkBox.isSelected());
            state.setText(checkBox.isSelected() ? "Eingeschaltet" : "Ausgeschlatet");
            smartHomeAppController.save();
        });

        deviceGrid.add(checkBox, 1, functionCounter);
    }

    private void executeSliderValue(String functionName, Slider slider) {
        Object param = slider.getValue();

        device.executeFunction(functionName, param);
        smartHomeAppController.save();
        System.out.println("Aktion ausgeführt, da Slider losgelassen wurde.");
    }
}
