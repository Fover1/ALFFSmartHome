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
import model.AbstractDevice;
import model.DeviceAction;
import model.Room;
import model.SmartDevice;

import java.util.List;

public class ActionDialog extends Dialog<DeviceAction> {

    private final ComboBox<AbstractDevice> deviceComboBox;
    private final ComboBox<Room> roomComboBox;
    private final ComboBox<String> functionComboBox;
    private final GridPane grid;
    private Node dynamicParameterControl;

    public ActionDialog(List<Room> availableRooms, List<AbstractDevice> availableDevices, DeviceAction existingAction) {
        setTitle(existingAction == null ? "Aktion hinzufügen" : "Aktion bearbeiten");
        setHeaderText("Bitte wähle das Gerät und die gewünschte Aktion aus.");

        //schaut automatisch nach, welches Betriebssystem es ist und verschiebt den Button an die richtige stelle
        //außerdem mit Entertaste verknüpft
        ButtonType saveButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        //das DialogPane ist dieses Popup. Die haben standartweise untenn eine Reihe für buttons. Hier werden diese buttons hinzugefügt
        //JAVAFX hat bereits vorgefertigte buttons. Hier werden Done und Cancel genutzt
        //automatisch mit esc verknüpft
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
            protected void updateItem(AbstractDevice item, boolean empty) {
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
        grid.add(new Label("Wert (Parameter):"), 0, 3);

        getDialogPane().setContent(grid);

        roomComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                deviceComboBox.setItems(FXCollections.observableArrayList(newVal.getAbstractDevices()));
                if (!newVal.getAbstractDevices().isEmpty()) {
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
            String initVal = (existingAction != null && existingAction.functionName().equals(newVal))
                    ? String.valueOf(existingAction.parameter())
                    : null;
            updateParameterUI(deviceComboBox.getValue(), newVal, initVal);
        });

        if (existingAction != null) {
            AbstractDevice actionDevice = (AbstractDevice) existingAction.targetDevice();
            AbstractDevice realDevice = availableDevices.stream()
                    .filter(d -> d.getId().equals(actionDevice.getId()))
                    .findFirst()
                    .orElse(actionDevice);

            Room realRoom = availableRooms.stream()
                    .filter(room -> room.getAbstractDevices().stream()
                            .anyMatch(device -> device.getId().equals(actionDevice.getId())))
                    .findFirst()
                    .orElse(null);

            roomComboBox.getSelectionModel().select(realRoom);
            deviceComboBox.getSelectionModel().select(realDevice);
            functionComboBox.getSelectionModel().select(existingAction.functionName());
        }

        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                SmartDevice selectedDevice = deviceComboBox.getValue();
                String selectedFunction = functionComboBox.getValue();

                String parameterStr = ParameterControlFactory.getValueFromControl(dynamicParameterControl);

                if (selectedDevice != null && selectedFunction != null) {
                    return new DeviceAction(selectedDevice, selectedFunction, parameterStr);
                }
            }
            return null;
        });
    }

    private void updateParameterUI(AbstractDevice device, String functionName, String initialValue) {
        if (dynamicParameterControl != null) {
            grid.getChildren().remove(dynamicParameterControl);
        }

        dynamicParameterControl = ParameterControlFactory.createControl(device, functionName, initialValue);

        if (dynamicParameterControl != null) {
            grid.add(dynamicParameterControl, 1, 3);
            // Von Stackoverflow, um die Fenstergröße an die neu erstellte Parametereingabe anzupassne
            if (getDialogPane().getScene() != null && getDialogPane().getScene().getWindow() != null) {
                getDialogPane().getScene().getWindow().sizeToScene();
            }
        }
    }

 
}