package frontendController;

import controller.SmartHomeAppController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import model.Room;
import interfaces.RoomObserver;
import interfaces.SmartDevice;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RoomController implements RoomObserver {

    private final Map<UUID, Stage> deviceWindows = new HashMap<>();
    @FXML
    public Button addDevice;
    private SmartHomeAppController smartHomeAppController;
    @FXML
    private FlowPane deviceContainer;
    @FXML
    private FlowPane roomContainer;
    @FXML
    private Label roomSelection;
    @FXML
    private Button deleteRoom;
    @FXML
    private Button editRoom;
    @FXML
    private Label deviceDisplay;
    private Room currentRoom;

    public void setAppController(SmartHomeAppController smartHomeAppController) {
        this.smartHomeAppController = smartHomeAppController;
        updateUI();
    }

    public void setRoom(Room room) {
        if (this.currentRoom != null) {
            this.currentRoom.removeObserver(this);
        }
        this.currentRoom = room;

        if (this.currentRoom != null) {
            this.currentRoom.addObserver(this);
        }
        updateUI();
    }

    @Override
    public void onDeviceListChanged(Room room) {
        Platform.runLater(this::updateUI);
    }

    private void updateUI() {
        if (roomContainer != null && smartHomeAppController != null) {
            roomContainer.getChildren().clear();

            for (Room room : getRooms()) {
                Button roomButton = new Button(room.getName());
                roomButton.setOnAction(e -> showDevices(room));

                roomContainer.getChildren().add(roomButton);
            }
        }

        if (deviceContainer != null && smartHomeAppController != null) {
            deviceContainer.getChildren().clear();
        }
        roomSelection.setText("Noch kein Raum ausgewählt");
    }

    @FXML
    public void handleAddRoom() {
        Optional<String> result = StringInputDialog.get(
                "Neuer Raum",
                "Raumplanung",
                "Name des Raums:",
                ""
        );

        result.ifPresent(inputName -> {
            String roomName = inputName.trim();
            if (!roomName.isEmpty()) {
                if (checkIfRoomNameAlreadyExists(roomName)) { return; }
                smartHomeAppController.addRoom(roomName);
                smartHomeAppController.save();
                updateUI();
            }
        });
    }

    @FXML
    public void handleAddDevice() {
        if (currentRoom == null) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Kein Raum ausgewählt");
            alert.setHeaderText(null);
            alert.setContentText("Bitte wählen Sie zuerst einen Raum aus, bevor Sie ein Gerät hinzufügen.");
            alert.showAndWait();
            return;
        }


        javafx.scene.control.Dialog<javafx.util.Pair<String, String>> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Neues Gerät");
        dialog.setHeaderText("Neues Gerät zum Raum '" + currentRoom.getName() + "' hinzufügen");

        javafx.scene.control.ButtonType addButtonType = new javafx.scene.control.ButtonType("Hinzufügen", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        javafx.scene.control.TextField nameField = new javafx.scene.control.TextField();
        nameField.setPromptText("Gerätename eingeben");

        javafx.scene.control.ComboBox<String> typeComboBox = new javafx.scene.control.ComboBox<>();

        List<String> deviceTypes = model.DeviceScanner.getAllDeviceTypes("devices");
        typeComboBox.getItems().addAll(deviceTypes);


        grid.add(new javafx.scene.control.Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new javafx.scene.control.Label("Typ:"), 0, 1);
        grid.add(typeComboBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        //hiermit kann der Fokus direkt auf das Namenfeld gesetzt werden, damit direkt reingeschrieben werden kann
        Platform.runLater(nameField::requestFocus);

        //definiert, was zurueckgegeben wird, wenn auf "Hinzufuegen" geklickt wird
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new javafx.util.Pair<>(nameField.getText(), typeComboBox.getValue());
            }
            return null;
        });

        //Dialog anzeigen und auf Ergebnis warten
        Optional<javafx.util.Pair<String, String>> result = dialog.showAndWait();

        //Ergebnis verarbeiten
        result.ifPresent(nameTypePair -> {
            String deviceName = nameTypePair.getKey().trim();
            String deviceType = nameTypePair.getValue();

            if (!deviceName.isEmpty() && deviceType != null) {
                boolean nameExistsInRoom = currentRoom.getSmartDevices().stream().anyMatch(device -> device.getName().equalsIgnoreCase(deviceName));
                if (nameExistsInRoom) {
                    javafx.scene.control.Alert duplicateAlert = new  javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                    duplicateAlert.setTitle("Der Name ist bereits vergeben.");
                    duplicateAlert.setHeaderText("Dieses Gerät existiert bereits.");
                    duplicateAlert.setContentText("Ein Gerät mit dem Namen '" + deviceName + "' ist bereits vorhanden. Bitte wähle einen anderen Namen.");
                    duplicateAlert.showAndWait();
                    return;
                }

                try {
                    java.util.UUID newId = java.util.UUID.randomUUID();

                    //neues Geraet ueber die DeviceFactory erstellen
                    SmartDevice newDevice = model.DeviceFactory.createDevice(deviceType, newId, deviceName);

                    //Geraet dem aktuellen Raum hinzufuegen (notifyObservers wird in addDevice getriggert)
                    currentRoom.addDevice(newDevice);

                    //Speichern
                    smartHomeAppController.save();

                    //Ansicht aktualisieren
                    showDevices(currentRoom);

                } catch (Exception e) {
                    //Fehlerbehandlung, falls Factory fehlschlaegt
                    javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    errorAlert.setTitle("Fehler");
                    errorAlert.setHeaderText("Gerät konnte nicht erstellt werden");
                    errorAlert.setContentText(e.getMessage());
                    errorAlert.showAndWait();
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    public void showDevices(Room room) {
        this.currentRoom = room;
        deviceDisplay.setVisible(true);
        addDevice.setVisible(true);
        updateUI();
        roomSelection.setText("Ausgewählter Raum: " + currentRoom.getName() + "    ");

        deleteRoom.setOnAction(e -> {
            smartHomeAppController.deleteRoom(currentRoom);
            updateUI();
        });
        editRoom.setOnAction(e -> {
            handleRoomNameChange(currentRoom);
            updateUI();
        });

        deleteRoom.setVisible(true);
        editRoom.setVisible(true);

        for (SmartDevice device : getDevices(room)) {
            Button deviceButton = new Button(device.getName());
            deviceButton.setOnAction(e -> openDeviceView(device, room));
            deviceContainer.getChildren().add(deviceButton);
        }
    }

    private void handleRoomNameChange(Room room) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Name ändern: " + room.getName());
        dialog.setHeaderText(String.format("Bitte geben Sie einen neuen Namen für den Raum \"%s\" ein", room.getName()));

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(inputName -> {
            String newRoomName = inputName.trim();
            if (!newRoomName.isEmpty() && !newRoomName.equalsIgnoreCase(room.getName())) {
                if (checkIfRoomNameAlreadyExists(newRoomName)) { return; }
                smartHomeAppController.changeRoomName(room, newRoomName);
                smartHomeAppController.save();
            }
        });
    }

    private void openDeviceView(SmartDevice device, Room selectedRoom) {
        //hier wird geprueft, ob fuer das Geraet schon ein Fenster offen ist
        if (deviceWindows.containsKey(device.getId())) {
            Stage stage = deviceWindows.get(device.getId());
            if (stage.isShowing()) {
                //wenn ja, wird es in den Vordergrund geholt
                stage.toFront();
                return;
            }
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/DeviceView.fxml"));
            Parent root = fxmlLoader.load();

            DeviceController deviceController = fxmlLoader.getController();
            if (deviceController != null) {
                deviceController.setData(device, smartHomeAppController, selectedRoom);
            }
            Stage stage = new Stage();
            stage.setTitle("Gerätedetails: " + device.getName());
            Scene scene = new Scene(root);
            root.setStyle("-fx-background-color: -color-bg-default;");
            scene.getStylesheets().add(new atlantafx.base.theme.CupertinoDark().getUserAgentStylesheet());
            deviceWindows.put(device.getId(), stage);
            stage.setScene(scene);
            stage.setOnHidden(event -> {
                deviceWindows.remove(device.getId());
                device.removeObserver((deviceController));
            });
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public List<Room> getRooms() {
        return this.smartHomeAppController.getAllRooms();
    }

    public List<SmartDevice> getDevices(Room room) {
        return room.getSmartDevices();
    }

    private boolean checkIfRoomNameAlreadyExists(String roomName) {
        boolean roomExists = smartHomeAppController.getAllRooms().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(roomName));

        if (roomExists) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Name bereits vergeben");
            alert.setHeaderText("Dieser Raum existiert bereits.");
            alert.setContentText("Ein Raum mit dem Namen '" + roomName + "' ist bereits vorhanden. Bitte wähle einen anderen Namen.");
            alert.showAndWait();
        }
        return roomExists;
    }
}
