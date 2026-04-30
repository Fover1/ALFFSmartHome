package frontendController;

import controller.SmartHomeAppController;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import model.Action;
import model.DeviceAction;
import model.Scenario;

import java.util.Collections;
import java.util.Optional;

public class ScenarioControllerNew {

    @FXML
    private TableView<Scenario> scenarioTable;
    @FXML
    private TableColumn<Scenario, String> colName;
    @FXML
    private TableColumn<Scenario, String> colDesc;
    @FXML
    private TableColumn<Scenario, Number> colActionCount;

    @FXML
    private VBox detailArea;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtDescription;
    @FXML
    private ListView<Action> actionListView;

    private ObservableList<Scenario> observableScenarios;
    private SmartHomeAppController smartHomeAppController;

    public void setAppController(SmartHomeAppController smartHomeAppController) {
        this.smartHomeAppController = smartHomeAppController;
        observableScenarios = FXCollections.observableArrayList(smartHomeAppController.getAllScenarios());
        scenarioTable.setItems(observableScenarios);
    }

    @FXML
    public void initialize() {
        // setCellValueFactory bringt den String in ein Format, dass die JavaFX Zeile verseteht
        colName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        colDesc.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        colActionCount.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCount()));

        actionListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Action action, boolean empty) {
                super.updateItem(action, empty);
                if (empty || action == null) {
                    setText(null);
                } else {
                    setText(action.getDescription());
                }
            }
        });

        scenarioTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showScenarioDetails(newValue));
    }

    private void showScenarioDetails(Scenario scenario) {
        if (scenario != null) {
            detailArea.setDisable(false);
            txtName.setText(scenario.getName());
            txtDescription.setText(scenario.getDescription());
            updateActionList(scenario);
        } else {
            detailArea.setDisable(true);
            txtName.clear();
            txtDescription.clear();
            actionListView.getItems().clear();
        }
    }

    private void updateActionList(Scenario scenario) {
        actionListView.setItems(FXCollections.observableArrayList(scenario.getActions()));
    }

    @FXML
    private void handleNewScenario() {

        /// todo: wenn man einmal was hinzufügt, kommen 2 dazu
        Scenario newScenario = new Scenario("", "");
        smartHomeAppController.addSzenario(newScenario);
        observableScenarios.add(newScenario);
        scenarioTable.getSelectionModel().select(newScenario);
    }

    @FXML
    private void handleDeleteScenario() {
        Scenario selected = scenarioTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            smartHomeAppController.removeScenario(selected);
            observableScenarios.remove(selected);
            smartHomeAppController.save();
        }
    }

    @FXML
    private void handleSaveScenarioDetails() {
        Scenario selected = scenarioTable.getSelectionModel().getSelectedItem();
        System.out.println("handleSaveScenarioDetails");
        if (selected == null) {
            System.out.println("selected == null");
            selected.setName(txtName.getText());
            selected.setDescription(txtDescription.getText());
            System.out.println("in handleScenarioDetail " + selected.getName());
            smartHomeAppController.addSzenario(selected);
            smartHomeAppController.save();
            scenarioTable.refresh();
        } else {
            System.out.println("selected != null --> es sollte kein neues Szeanrio angelegt werden");
            selected.setName(txtName.getText());
            selected.setDescription(txtDescription.getText());
            smartHomeAppController.save();
        }
    }

    @FXML
    private void handleExecuteScenario() {
        /// todo: hier wird noch ncihts ausgeführt
        Scenario selected = scenarioTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.execute();
            System.out.println("Szenario " + selected.getName() + " wurde ausgeführt.");
        }
    }


    @FXML
    private void handleAddAction() {
        /// todo: raum auswhal vor geräteauswahl
        Scenario selectedScenario = scenarioTable.getSelectionModel().getSelectedItem();
        if (selectedScenario != null) {
            ActionDialog dialog = new ActionDialog(smartHomeAppController.getAllRooms(), smartHomeAppController.getAllDevices(), null);
            Optional<DeviceAction> result = dialog.showAndWait();

            result.ifPresent(action -> {
                selectedScenario.addAction(action);
                updateActionList(selectedScenario);
                scenarioTable.refresh();
            });
            smartHomeAppController.save();
        }
    }

    @FXML
    private void handleEditAction() {
        Scenario selectedScenario = scenarioTable.getSelectionModel().getSelectedItem();
        Action selectedAction = actionListView.getSelectionModel().getSelectedItem();

        if (selectedScenario != null && selectedAction instanceof DeviceAction deviceAction) {
            System.out.println(deviceAction.functionName() + deviceAction.getDescription() + deviceAction.targetDevice().getName());
            ActionDialog dialog = new ActionDialog(smartHomeAppController.getAllRooms(), smartHomeAppController.getAllDevices(), deviceAction);
            Optional<DeviceAction> result = dialog.showAndWait();

            result.ifPresent(newAction -> {
                selectedScenario.removeAction(deviceAction);
                selectedScenario.addAction(newAction);
                updateActionList(selectedScenario);
            });
        }
    }

    @FXML
    private void handleDeleteAction() {
        Scenario selectedScenario = scenarioTable.getSelectionModel().getSelectedItem();
        Action selectedAction = actionListView.getSelectionModel().getSelectedItem();
        if (selectedScenario != null && selectedAction != null) {
            selectedScenario.removeAction(selectedAction);
            smartHomeAppController.save();
            updateActionList(selectedScenario);
            scenarioTable.refresh();
        }
    }

    @FXML
    private void handleMoveActionUp() {
        Scenario scenario = scenarioTable.getSelectionModel().getSelectedItem();
        int selectedIndex = actionListView.getSelectionModel().getSelectedIndex();

        if (scenario != null && selectedIndex > 0) {
            Collections.swap(scenario.getActions(), selectedIndex, selectedIndex - 1);
            smartHomeAppController.save();
            updateActionList(scenario);
            actionListView.getSelectionModel().select(selectedIndex - 1);
        }
    }

    @FXML
    private void handleMoveActionDown() {
        Scenario scenario = scenarioTable.getSelectionModel().getSelectedItem();
        int selectedIndex = actionListView.getSelectionModel().getSelectedIndex();

        if (scenario != null && selectedIndex >= 0 && selectedIndex < scenario.getActions().size() - 1) {
            Collections.swap(scenario.getActions(), selectedIndex, selectedIndex + 1);
            updateActionList(scenario);
            actionListView.getSelectionModel().select(selectedIndex + 1);
        }
    }
}