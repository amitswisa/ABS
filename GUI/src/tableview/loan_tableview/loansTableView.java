package tableview.loan_tableview;

import dto.objectdata.LoanDataObject;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import popups.loan_information.LoanInfoController;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class loansTableView implements Initializable {

    //POPUP Data
    private FXMLLoader loader;
    private LoanInfoController controller;
    private Scene firstScene = null;

    // this Data-Members
    private ObservableList<LoanDataObject> obsList;
    private FilteredList<LoanDataObject> list = null;
    private Stage popUpStage;

    // Properties for filter use
    private SimpleIntegerProperty investAmount, minInterest, minYaz, maxOpenLoans, ownershipPrecent;
    private ListProperty<String> catsList;

    @FXML private TableView<LoanDataObject> loansTable;
    @FXML private TableColumn<LoanDataObject, String> loanID, owner, loanCategory, loanAmount, loanInterestPerPayment, loanTotalTime, amountLeftToPay;

    public loansTableView() throws IOException {
        // Create popup window.
        popUpStage = new Stage();
        popUpStage.initModality(Modality.APPLICATION_MODAL);
        popUpStage.setTitle("Loan details");

        loader = new FXMLLoader(getClass().getResource("/popups/loan_information/loanInfo.fxml"));
        firstScene = new Scene(loader.load());
        controller = loader.getController();
        popUpStage.setScene(firstScene);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Init table columns
        loanID.setCellValueFactory(new PropertyValueFactory<>("loanID"));
        owner.setCellValueFactory(new PropertyValueFactory<>("owner"));
        loanCategory.setCellValueFactory(new PropertyValueFactory<>("loanCategory"));
        loanAmount.setCellValueFactory(new PropertyValueFactory<>("loanAmount"));
        amountLeftToPay.setCellValueFactory(new PropertyValueFactory<>("amountLeftToPay"));
        loanInterestPerPayment.setCellValueFactory(new PropertyValueFactory<>("loanInterestPerPayment"));
        loanTotalTime.setCellValueFactory(new PropertyValueFactory<>("loanTotalTime"));

        // Init search property.
        investAmount = new SimpleIntegerProperty(0);
        minInterest = new SimpleIntegerProperty(0);
        minYaz = new SimpleIntegerProperty(0);
        maxOpenLoans = new SimpleIntegerProperty(0);
        ownershipPrecent = new SimpleIntegerProperty(0);
        catsList = new SimpleListProperty<>();

        // Init list of loans.
        obsList = FXCollections.observableArrayList();
        list = new FilteredList<LoanDataObject>(obsList);

        // Filter binding
        list.predicateProperty().bind(Bindings.createObjectBinding(() ->
                        i -> ((catsList.isEmpty()) ? !i.getLoanCategory().equals(""): catsList.contains(i.getLoanCategory()))
                        && ((this.investAmount.get() == 0) ? i.getLoanAmount() > 0 : i.getLoanAmount() <= this.investAmount.get())
                            && (i.getLoanInterestPerPayment() > this.minInterest.get())
                                && (i.getLoanTotalTime() >= this.minYaz.get())
                                && ((this.maxOpenLoans.get() == 0) ? i.getUnfinishedLoansNumber() >= 0 : i.getUnfinishedLoansNumber() <= this.maxOpenLoans.get())
                                    && ((this.ownershipPrecent.get() == 0) ? i.getAmountLeftToPay() > 0 : ((i.getAmountLeftToPay()/i.getLoanAmount())*100) <= this.ownershipPrecent.get()),
                this.catsList, this.investAmount, this.minInterest, this.minYaz, this.maxOpenLoans, this.ownershipPrecent
        ));

        TableColumn<LoanDataObject, Void> colBtn = new TableColumn("View Loan");
        Callback<TableColumn<LoanDataObject, Void>, TableCell<LoanDataObject, Void>> cellFactory = param -> {
            return new TableCell<LoanDataObject, Void>() {

                private final Button btn = new Button("View");
                {
                    btn.setOnAction((ActionEvent event) -> {
                        // Get loan data when clicking specific View button.
                        LoanDataObject dataObject = getTableView().getItems().get(getIndex());

                        // Create popup window with above details.
                        controller.setLoanPopupInfo(dataObject);
                        popUpStage.showAndWait();
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(btn);
                    }
                }
            };
        };
        colBtn.setCellFactory(cellFactory);
        loansTable.getColumns().add(colBtn);
    }

    public void setLoanItems(List<LoanDataObject> loansList) {

        obsList.clear();

        if(loansList == null || loansList.isEmpty())
            return;

        obsList.addAll(loansList);
        loansTable.setItems(list);
    }

    public void setMinAmount(int amountToInvest) {
        this.investAmount.set(amountToInvest);
    }

    public void updateFilterCategories(ObservableList<String> checkedItems) {
        this.catsList.set(checkedItems);
    }

    public void setMinInterest(int newV) {
        minInterest.set(newV);
    }

    public void setMinYaz(int newV) {
        minYaz.set(newV);
    }

    public void setMaxOpenLoans(int newV) {
        this.maxOpenLoans.set(newV);
    }

    public void setOwnershipPrecent(int newV) {
        this.ownershipPrecent.set(newV);
    }
}
