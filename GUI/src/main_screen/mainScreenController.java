package main_screen;

import Utils.User;
import admin_screen.AdminController;
import customer_screen.customerScreenController;
import dto.objectdata.CustomerAlertData;
import engine.EngineManager;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.Duration;
import listview.ListViewCell;

import java.net.URL;
import java.util.*;

public class mainScreenController implements Initializable {

    // Data members
    private final EngineManager engineManager;
    private User currentUser;
    private TranslateTransition translateTransition;
    private ChoiceDialog<String> dialog;

    // Pages
    @FXML private AnchorPane mainPane;
    @FXML private AnchorPane dataDiv;
    @FXML private AnchorPane adminPageComponent;
    @FXML private AdminController adminPageComponentController;
    @FXML private AnchorPane customerPageComponent;
    @FXML private customerScreenController customerPageComponentController;

    // FXML members
    @FXML private ChoiceBox<String> userTypeChoice;
    @FXML private ImageView settingsBtn;
    @FXML private Label pathLabel;
    @FXML private Label yazLabel;
    @FXML private Label alertMessageCounter;
    @FXML private ImageView alertBtn;
    @FXML private AnchorPane alertPane;
    @FXML private ListView<CustomerAlertData> alertsViewList;
    @FXML private HBox alertBox;
    private ObservableList<CustomerAlertData> alertObservableList;

    public mainScreenController() {
        engineManager = new EngineManager();
        alertObservableList = FXCollections.observableArrayList();

        // Dialog to settings
        List<String> choices = new ArrayList<String>();
        choices.add("Light theme");
        choices.add("Dark theme");
        dialog = new ChoiceDialog<>("Light theme", choices);
        dialog.setTitle("Settings");
        dialog.setHeaderText(null);
        dialog.setGraphic(null);
        dialog.setContentText("Theme: ");
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.OK)).setText("Save");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        translateTransition = new TranslateTransition(Duration.millis(200), alertBox);
        final double start = 0.0;
        final double end = start - 4.0;
        translateTransition.setFromY(start);
        translateTransition.setToY(end);
        translateTransition.setAutoReverse(true);
        translateTransition.setCycleCount(Animation.INDEFINITE);
        translateTransition.setInterpolator(Interpolator.EASE_BOTH);

        // Initial settings.
        customerPageComponent.setVisible(false);
        customerPageComponentController.setEngineManager(this.engineManager);
        currentUser = customerPageComponentController.getCurrentUser();

        // Transfer engine to included fxmls.
        adminPageComponentController.setInitData(this.engineManager, this);

        // Bind data-members
        this.pathLabel.textProperty().bind(adminPageComponentController.getPathTextProperty());

        //Set init values for choice-box (only admin).
        adminPageComponentController.setCustomersNamesChoiceBox();

        // Alerts list view init to invisible.
        alertPane.setVisible(false);
        alertsViewList.setPlaceholder(new Label("No content found"));

        // Handling clicking on alert icon open listview event.
        alertBtn.onMouseClickedProperty().set(e -> handleAlertBoxClick());
        alertMessageCounter.onMouseClickedProperty().set(e -> handleAlertBoxClick());

        // Set alert listview cell factory.
        alertsViewList.setCellFactory(new Callback<ListView<CustomerAlertData>, ListCell<CustomerAlertData>>()
        {
            @Override
            public ListCell<CustomerAlertData> call(ListView<CustomerAlertData> listView)
            {
                return new ListViewCell();
            }
        });

    }

    public ChoiceBox<String> getUserTypeChoice() {
        return userTypeChoice;
    }

    // Return current chosen customer name.
    public String getCurrentUser() {
        return this.currentUser.getUsername();
    }

    public void setYazLabelText(String yazString) {
        this.yazLabel.setText(yazString);
    }

    // Reset settings and setup customer changing event.
    public void changeUserType(ActionEvent actionEvent) {
        String newUser = userTypeChoice.getValue();

        if(newUser == null)
            return;

        // Update details according to customer select. (different "Admin" Or Other).
        if(newUser.equals(this.currentUser.getDefaultUserName())) { // if Admin chosen.
            this.customerPageComponent.setVisible(false);
            this.adminPageComponent.setVisible(true);
            this.currentUser.setUsername(this.currentUser.getDefaultUserName());
            this.alertBox.setVisible(false);
            this.adminPageComponentController.updateAdminLists();
        } else {
            this.customerPageComponent.setVisible(true);
            this.adminPageComponent.setVisible(false);
            this.currentUser.setUsername(newUser);
            customerPageComponentController.resetSettings();
            this.getCustomerAlerts();
            this.alertBox.setVisible(true);
        }

        this.alertPane.setVisible(false); // Set alert view invisible when changing user.
    }

    private void getCustomerAlerts() {
        // Alerts handling.
        alertObservableList.setAll(customerPageComponentController.getCustomerAlertList());
        FXCollections.reverse(alertObservableList); // sort from end to beginning.
        alertsViewList.setItems(alertObservableList);

        if(countUnReadMsg() > 0)
            translateTransition.play();
        else
            translateTransition.stop();
    }

    private void handleAlertBoxClick() {

        if(userTypeChoice.getValue().equals("Admin")) {
            alertPane.setVisible(false);
            return;
        }

        // Refresh changes.
        alertPane.setVisible(!alertPane.isVisible());

        if(!alertPane.isVisible())
            alertsViewList.refresh();
        else {
            this.alertMessageCounter.setText("0");
            translateTransition.stop();
            customerPageComponentController.getCustomerAlertList().forEach(CustomerAlertData::markAsRead);
        }
    }

    private int countUnReadMsg() {
        // Count number of unread notifications.
        int count = (int) customerPageComponentController.getCustomerAlertList().stream().filter(e -> !e.isAlertGotRead()).count();
        this.alertMessageCounter.setText(count + "");
        return count;
    }

    public void settingsFunctionallity(MouseEvent mouseEvent) {

        String formResult = dialog.getResult();
        Optional<String> result = dialog.showAndWait();

        // If clicked ok.
        if (result.isPresent()){

            if(result.get().equals(formResult))
                return;

            if(result.get().equals("Dark theme")) {
                mainPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("mainScreenDark.css")).toExternalForm());
                alertBtn.setImage(new Image("/resources/images/alertWhite.png"));
                settingsBtn.setImage(new Image("/resources/images/settingsWhite.png"));
            } else {
                mainPane.getStylesheets().remove(Objects.requireNonNull(getClass().getResource("mainScreenDark.css")).toExternalForm());
                alertBtn.setImage(new Image("/resources/images/alert.png"));
                settingsBtn.setImage(new Image("/resources/images/settings.png"));
            }

        }

    }
}
