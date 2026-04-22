package frontendController;

import controller.SmartHomeAppController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    }

    @FXML
    public void handleAddRoom() {
        TextInputDialog dialog = new TextInputDialog("new Room");
        dialog.setTitle("Neuer Raum");
        dialog.setHeaderText("Neuen Raum anlegen");
        dialog.setContentText("Bitte geben Sie den neuen Raum ein: ");

        Optional<String> result = dialog.showAndWait();

        /// todo: was machen wir, wenn es einen raum 2x geben soll (also identischer name)?
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
        updateUI();
        System.out.println("Raum ausgewählt: " + room.getName());

        for (AbstractDevice device : getDevices(room)) {
            ///todo: noch fertig bearbeiten
            Button deviceButton = new Button(device.getName() + " (" + device.getId() + ")" + System.currentTimeMillis());
            deviceButton.setOnAction(e -> openDeviceView(device));

            deviceContainer.getChildren().add(deviceButton);
        }
    }

    private void openDeviceView(AbstractDevice device) {
        try {
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
