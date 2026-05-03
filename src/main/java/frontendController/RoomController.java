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
import model.AbstractDevice;
import model.Room;
import model.RoomObserver;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class RoomController implements RoomObserver {

    @FXML
    public Button addDevice;
    private SmartHomeAppController smartHomeAppController;
    @FXML
    private FlowPane deviceContainer;
    @FXML
    private FlowPane roomContainer;
    @FXML
    private Label raumAuswahl;
    @FXML
    private Button deleteRoom;
    @FXML
    private Button editRoom;
    @FXML
    private Label deviceAnzeige;
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
            System.out.println("Geräte unsichtbar gemacht");
        }
        raumAuswahl.setText("Noch kein Raum ausgewählt");
    }

    @FXML
    public void handleAddRoom() {
        TextInputDialog dialog = new TextInputDialog("new Room");
        dialog.setTitle("Neuer Raum");
        dialog.setHeaderText("Neuen Raum anlegen");
        dialog.setContentText("Bitte geben Sie den neuen Raum ein: ");

        Optional<String> result = dialog.showAndWait();

        /// todo: was machen wir, wenn es einen raum 2x geben soll (also identischer name)? (raum mit dem namen darf es nur einmal geben)
        result.ifPresent(roomName -> {
            if (!roomName.trim().isEmpty()) {
                smartHomeAppController.addRoom(roomName);
                System.out.println("Neuer Raum angelegt: " + roomName);

            }
        });

        smartHomeAppController.save();
        updateUI();
    }

    @FXML
    public void handleAddDevice() {
        /// todo: ich habe in nem Tutorial gesehen, dass man so fehlermeldungen ausgeben kann. Damit könnten wir ja einen Großteil unseres Fehlerhandlings machen?
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
        /// todo: hier ist ein weiteres beispiel, wie man padding einbauen kann
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        /// todo: mit setPromptText text vorher in Textfelder schreiben
        javafx.scene.control.TextField nameField = new javafx.scene.control.TextField();
        nameField.setPromptText("Gerätename eingeben");

        javafx.scene.control.ComboBox<String> typeComboBox = new javafx.scene.control.ComboBox<>();

        /// todo: getAllDeviceTypes über den controller?
        List<String> deviceTypes = model.DeviceScanner.getAllDeviceTypes("devices");
        typeComboBox.getItems().addAll(deviceTypes);


        grid.add(new javafx.scene.control.Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new javafx.scene.control.Label("Typ:"), 0, 1);
        grid.add(typeComboBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        //hiermit kann man den Fokus direkt auf das Namenfeld setzten, damit man da direkt reinschreiben kann
        Platform.runLater(nameField::requestFocus);

        // Definiert, was zurückgegeben wird, wenn man auf "Hinzufügen" klickt
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new javafx.util.Pair<>(nameField.getText(), typeComboBox.getValue());
            }
            return null;
        });

        // Dialog anzeigen und auf Ergebnis warten
        Optional<javafx.util.Pair<String, String>> result = dialog.showAndWait();

        // Ergebnis verarbeiten
        result.ifPresent(nameTypePair -> {
            String deviceName = nameTypePair.getKey();
            String deviceType = nameTypePair.getValue();

            if (deviceName != null && !deviceName.trim().isEmpty() && deviceType != null) {
                try {
                    // ID generieren (Da ihr auf UUID umstellen wollt / UUID in DeviceFactory verwendet wird)
                    java.util.UUID newId = java.util.UUID.randomUUID();

                    // Neues Gerät über die DeviceFactory erstellen
                    AbstractDevice newDevice = model.DeviceFactory.createDevice(deviceType, newId, deviceName);

                    // Gerät dem aktuellen Raum hinzufügen (notifyObservers wird in addDevice getriggert)
                    currentRoom.addDevice(newDevice);

                    // Speichern
                    smartHomeAppController.save();

                    // Ansicht aktualisieren
                    showDevices(currentRoom);

                    System.out.println("Neues Gerät angelegt: " + deviceName + " (Typ: " + deviceType + ")");
                } catch (Exception e) {
                    // Fehlerbehandlung, falls Factory fehlschlägt
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
        deviceAnzeige.setVisible(true);
        addDevice.setVisible(true);
        updateUI();
        raumAuswahl.setText("Ausgewählter Raum: " + currentRoom.getName() + "    ");
        System.out.println("Raum ausgewählt: " + currentRoom.getName());

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


        for (AbstractDevice device : getDevices(room)) {
            ///todo: noch fertig bearbeiten
            Button deviceButton = new Button(device.getName() + " (" + device.getId() + ")" + System.currentTimeMillis());
            deviceButton.setOnAction(e -> openDeviceView(device, room));

            deviceContainer.getChildren().add(deviceButton);
        }
    }

    private void handleRoomNameChange(Room room) {
        TextInputDialog dialog = new TextInputDialog();
        /// todo: hier noch den Namen des Raums in das Textfeld einfügen
        dialog.setTitle("Name ändern: " + room.getName());
        dialog.setHeaderText(String.format("Bitte gebe einen neuen Namen für den Raum \"%s\" ein", room.getName()));

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(roomName -> {
            if (!roomName.trim().isEmpty()) {
                smartHomeAppController.changeRoomName(room, roomName);
                System.out.println("Raumname geändert: " + roomName);

            }
        });
        smartHomeAppController.save();
    }

    private void openDeviceView(AbstractDevice device, Room selectedRoom) {
        try {
            /// todo: kann man ihm sagen, dass nur ein fenster davon offen sien soll? oder müssen wir die fenster synchronisieren? --> Also das man halt nicht 2 eintsellungsfenster vom selben gerät offen hat --> am besten nur ein fesnter auf haben könen
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/deviceView.fxml"));
            Parent root = fxmlLoader.load();

            DeviceController deviceController = fxmlLoader.getController();
            if (deviceController != null) {
                deviceController.setData(device, smartHomeAppController, selectedRoom);
            }
            Stage stage = new Stage();
            stage.setTitle("Gerätedetails: " + device.getName());
            Scene scene = new Scene(root);
            //weiß wer, warum wir diese Zeile brauchen?
            root.setStyle("-fx-background-color: -color-bg-default;");
            scene.getStylesheets().add(new atlantafx.base.theme.CupertinoDark().getUserAgentStylesheet());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public List<Room> getRooms() {
        return this.smartHomeAppController.getAllRooms();
    }

    public List<AbstractDevice> getDevices(Room room) {
        return room.getAbstractDevices();
    }
}
