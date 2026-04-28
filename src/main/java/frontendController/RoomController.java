package frontendController;

import controller.SmartHomeAppController;
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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class RoomController {
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

    public void setAppController(SmartHomeAppController smartHomeAppController) {
        this.smartHomeAppController = smartHomeAppController;
        updateUI();
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
    public void showDevices(Room room) {
        deviceAnzeige.setVisible(true);
        updateUI();
        raumAuswahl.setText("Ausgewählter Raum: " + room.getName() + "    ");
        System.out.println("Raum ausgewählt: " + room.getName());

        deleteRoom.setOnAction(e -> {
            smartHomeAppController.deleteRoom(room);
            updateUI();
        });
        editRoom.setOnAction(e -> {
            handleRoomNameChange(room);
            updateUI();
        });


        deleteRoom.setVisible(true);
        editRoom.setVisible(true);


        for (AbstractDevice device : getDevices(room)) {
            ///todo: noch fertig bearbeiten
            Button deviceButton = new Button(device.getName() + " (" + device.getId() + ")" + System.currentTimeMillis());
            deviceButton.setOnAction(e -> openDeviceView(device));

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

    private void openDeviceView(AbstractDevice device) {
        try {
            /// todo: kann man ihm sagen, dass nur ein fenster davon offen sien soll? oder müssen wir die fenster synchronisieren? --> Also das man halt nicht 2 eintsellungsfenster vom selben gerät offen hat --> am besten nur ein fesnter auf haben könen
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/deviceView.fxml"));
            Parent root = fxmlLoader.load();

            DeviceController deviceController = fxmlLoader.getController();
            if (deviceController != null) {
                deviceController.setDevice(device, smartHomeAppController);
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
