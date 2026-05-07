package frontendController;

import controller.SmartHomeAppController;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import model.Action;
import model.DeviceAction;
import model.Scenario;
import model.ScenarioAction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ScenarioController {

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
            java.util.UUID idToDelete = selected.getId();

            for (Scenario scenario : observableScenarios) {
                System.out.println("Das Scenario: " + scenario.getName() + " mit : " + scenario.getId());
            }


            for (Scenario scenario : smartHomeAppController.getAllScenarios()) {
                System.out.println("Das Scenario: " + scenario.getName() + " mit : " + scenario.getId());
            }

            for (Scenario scenario : observableScenarios) {
                System.out.println("aktuelles Scenario: " + scenario.getName() + " | " + scenario.getId());
                scenario.getActions().removeIf(action -> {
                    if (action instanceof Scenario) {
                        System.out.println("ist das gleich? :" + ((Scenario) action).getId().equals(idToDelete));
                        return (((Scenario) action).getId().equals(idToDelete));
                    }
                    System.out.println("das ist es nicht" + scenario.getName());
                    return false;
                });
            }
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
        smartHomeAppController.executeScenario(scenarioTable.getSelectionModel().getSelectedItem());
    }


    @FXML
    private void handleAddAction() {
        Scenario selectedScenario = scenarioTable.getSelectionModel().getSelectedItem();
        if (selectedScenario != null) {
            ActionDialog dialog = new ActionDialog(smartHomeAppController.getAllRooms(), smartHomeAppController.getAllDevices(), null);
            Optional<DeviceAction> result = dialog.showAndWait();

            result.ifPresent(action -> {
                System.out.println(action.parameter().getClass().getSimpleName());
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

    @FXML
    public void handleAddExistingScenario() {

        /// todo: vllt alle Alerts in eine Alertbuilder klasse auslagern oder so?
        Scenario currentScenario = scenarioTable.getSelectionModel().getSelectedItem();
        if (currentScenario == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Scenario nicht gefFunden");
            alert.setContentText("Bitte wählen Sie ein Szenario aus");
            alert.showAndWait();
            return;
        }

        List<Scenario> test = smartHomeAppController.getAllScenarios();

        for (Scenario scenario : test) {
            System.out.println("Name: " + scenario.getName() + "ID: " + scenario.getId());
        }

        List<Scenario> availableScenarios = smartHomeAppController.getAllScenarios().stream()
                .filter(scenario -> !scenario.getId().equals(currentScenario.getId()))
                .toList();

        if (availableScenarios.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Keine Szenarien");
            alert.setHeaderText(null);
            alert.setContentText("Es gibt keine anderen Szenarien zum Einfügen.");
            alert.showAndWait();
        }

        List<String> availableScenarioNames = availableScenarios.stream()
                .map(Scenario::getName)
                .toList();

        javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(availableScenarioNames.getFirst(), availableScenarioNames);
        dialog.setTitle("Szenario einfügen");
        dialog.setHeaderText("Wählen Sie ein Szenario aus");
        dialog.setContentText("Szenario:");

        java.util.Optional<String> result = dialog.showAndWait();

        System.out.println("result: " + result);
        result.ifPresent(selectedName -> {
            Scenario selectedScenario = availableScenarios.stream()
                    .filter(scenario -> scenario.getName().equals(selectedName))
                    .findFirst()
                    .orElse(null);

            if (selectedScenario != null) {
                ScenarioAction scenarioAction = new ScenarioAction(selectedScenario.getId());
//                System.out.println("im Coontrolle: Name:" + scenarioAction.getTargetScenario().getName() + "ID:" + scenarioAction.getTargetScenario().getId());
                currentScenario.addAction(selectedScenario);
//                updateActionList(scenarioAction);
                scenarioTable.refresh();
                smartHomeAppController.save();

                System.out.println("Szenario eingefügt! Gespeicherte ID: " + selectedScenario.getId());
            }
        });
    }


}