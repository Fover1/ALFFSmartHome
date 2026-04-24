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
import javafx.stage.WindowEvent;
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

        List<Room> roomlist = smartHomeAppController.getAllRooms();

        deviceGrid.getChildren().clear();

        deviceName.setText("Gerät: " + device.getName());
        deviceType.setText("Gerätetype: " + device.getDeviceType());
        roomLabel.setText("Raum: " + device.getRoom().getName());

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
                Label state = new Label(isOn ? "Eingeschaltet" : "Ausgeschaltet");
                /// todo: vllt hardgecodede 3 in variable auslagern?
                deviceGrid.add(state, 3, functionCounter);
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
                deviceGrid.add(value, 3, functionCounter);

                /// todo: bei dem slider muss noch implementiert werden, dass erst wenn der slider los gelassen wird der neue wert in die json geschrieben wird
                slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                    Object param = (func.getParameterType() == Integer.class) ? newVal.intValue() : newVal.doubleValue();
                    device.executeFunction(functionName, param);

                    /// todo: die nächsten 2 Zeilen doppeln sich, kann man das noch schöner lösen?
                    int length = func.getValue().toString().length();
                    length = Math.min(length, 4);
                    value.setText(func.getValue().toString().substring(0, length) + " " + func.getUnit());
                    smartHomeAppController.save();
                    System.out.println("ich war hier");
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
                deviceGrid.add(value, 3, functionCounter);

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

        ObservableList<Room> rooms = FXCollections.observableArrayList(roomlist);

        Button editName = new Button("Name bearbeiten");
        ComboBox<Room> editRoom = new ComboBox<>(rooms);
        editRoom.setConverter(new StringConverter<Room>() {
            @Override
            public String toString(Room room) {
                return (room == null) ? "" : room.getName();
            }

            @Override
            public Room fromString(String s) {
                return null;
            }
        });
        editRoom.setPromptText(device.getRoom().getName());
        Button deleteDevice = new Button("Gerät löschen");


        editName.setOnAction(e -> {
            /// todo: kann man das dialog fenster nicht in eine andere Klasse machen, damit man das nicht 5x den sleeben code im projekt hat?
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

        editRoom.setOnAction(e -> {
            Room room = editRoom.getValue();
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

        deleteDevice.setOnAction(e -> {
            smartHomeAppController.deleteDevice(device);
            System.out.println("Geräte wird gelsöcht");
            smartHomeAppController.save();
        });

        /// todo: das muss noch schön aussehen (man kann die buttons nicht richtig lesen)
        //buttons direkt unter der untertesten Geräteeinstellung anzeigen
        functionCounter++;
        deviceGrid.add(editName, 0, functionCounter);
        deviceGrid.add(editRoom, 1, functionCounter);
        deviceGrid.add(deleteDevice, 3, functionCounter);
    }
}
