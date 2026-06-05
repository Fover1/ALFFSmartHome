package frontendController;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import model.DeviceAction;
import model.Room;
import interfaces.SmartDevice;

import java.util.List;

public class ActionDialog extends Dialog<DeviceAction> {

    private final ComboBox<SmartDevice> deviceComboBox;
    private final ComboBox<Room> roomComboBox;
    private final ComboBox<String> functionComboBox;
    private final GridPane grid;
    private Node dynamicParameterControl;

    public ActionDialog(List<Room> availableRooms, List<SmartDevice> availableDevices, DeviceAction existingAction) {
        setTitle(existingAction == null ? "Aktion hinzufügen" : "Aktion bearbeiten");
        setHeaderText("Bitte wähle das Gerät und die gewünschte Aktion aus.");

        //schaut automatisch nach, welches Betriebssystem es ist und verschiebt den Button an die richtige Stelle
        //außerdem mit Entertaste verknuepft
        ButtonType saveButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        //JAVAFX hat bereits vorgefertigte Buttons. Hier werden Done und Cancel genutzt.
        //automatisch mit ESC verknuepft
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        roomComboBox = new ComboBox<>(FXCollections.observableArrayList(availableRooms));
        deviceComboBox = new ComboBox<>(FXCollections.observableArrayList(availableDevices));
        roomComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Room item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });
        roomComboBox.setButtonCell(roomComboBox.getCellFactory().call(null));

        deviceComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(SmartDevice item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName() + " (" + item.getDeviceType() + ")");
            }
        });
        deviceComboBox.setButtonCell(deviceComboBox.getCellFactory().call(null));

        functionComboBox = new ComboBox<>();

        grid.add(new Label("Raum:"), 0, 0);
        grid.add(roomComboBox, 1, 0);
        grid.add(new Label("Gerät:"), 0, 1);
        grid.add(deviceComboBox, 1, 1);
        grid.add(new Label("Funktion:"), 0, 2);
        grid.add(functionComboBox, 1, 2);
        grid.add(new Label("Wert:"), 0, 3);

        getDialogPane().setContent(grid);

        roomComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                deviceComboBox.setItems(FXCollections.observableArrayList(newVal.getSmartDevices()));
                if (!newVal.getSmartDevices().isEmpty()) {
                    functionComboBox.getSelectionModel().selectFirst();
                }
            } else {
                functionComboBox.getItems().clear();
            }
        });

        deviceComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                functionComboBox.setItems(FXCollections.observableArrayList(newVal.getAvailableFunctions()));
                if (!newVal.getAvailableFunctions().isEmpty()) {
                    functionComboBox.getSelectionModel().selectFirst();
                }
            } else {
                functionComboBox.getItems().clear();
            }
        });

        functionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            String initVal = (existingAction != null && existingAction.getFunctionName().equals(newVal))
                    ? String.valueOf(existingAction.getParameter())
                    : null;
            updateParameterUI(deviceComboBox.getValue(), newVal, initVal);
        });

        if (existingAction != null) {
            SmartDevice actionDevice = existingAction.getTargetDevice();
            SmartDevice realDevice = availableDevices.stream()
                    .filter(d -> d.getId().equals(actionDevice.getId()))
                    .findFirst()
                    .orElse(actionDevice);

            Room realRoom = availableRooms.stream()
                    .filter(room -> room.getSmartDevices().stream()
                            .anyMatch(device -> device.getId().equals(actionDevice.getId())))
                    .findFirst()
                    .orElse(null);

            roomComboBox.getSelectionModel().select(realRoom);
            deviceComboBox.getSelectionModel().select(realDevice);
            functionComboBox.getSelectionModel().select(existingAction.getFunctionName());
        }

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                SmartDevice selectedDevice = deviceComboBox.getValue();
                String selectedFunction = functionComboBox.getValue();

                Object parameter = ParameterControlFactory.getValueFromControl(dynamicParameterControl);

                if (selectedDevice != null && selectedFunction != null) {
                    return new DeviceAction(selectedDevice, selectedFunction, parameter);
                }
            }
            return null;
        });
    }

    private void updateParameterUI(SmartDevice device, String functionName, String initialValue) {
        if (dynamicParameterControl != null) {
            grid.getChildren().remove(dynamicParameterControl);
        }

        dynamicParameterControl = ParameterControlFactory.createControl(device, functionName, initialValue);

        if (dynamicParameterControl != null) {
            grid.add(dynamicParameterControl, 1, 3);
            if (getDialogPane().getScene() != null && getDialogPane().getScene().getWindow() != null) {
                getDialogPane().getScene().getWindow().sizeToScene();
            }
        }
    }
}
