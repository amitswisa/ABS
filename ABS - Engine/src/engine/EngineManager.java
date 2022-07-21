package engine;

import abs.BankCustomer;
import abs.BankLoan;
import abs.BankSystem;
import dto.JSON.AdminData;
import dto.JSON.InvestmentData;
import dto.infodata.DataTransferObject;
import dto.objectdata.CustomerDataObject;
import dto.objectdata.LoanDataObject;
import dto.objectdata.TransactionDataObject;
import engine.xmlmanager.XMLManager;
import xmlgenerated.AbsDescriptor;

import java.util.*;
import java.util.stream.Collectors;

public class EngineManager {

    public enum BankStatus{
        ACTIVE,
        READ_ONLY;
    }

    private final XMLManager xmlManager;
    private BankSystem bankSystem;
    private Map<Integer, AdminData> rewindData;
    private BankStatus bankStatus;
    private Integer realYaz; //In rewind we save the real yaz.

    public EngineManager() {
        bankSystem = new BankSystem(); // Creating bank systenm for the first time.
        xmlManager = new XMLManager(); // Init xml manager.
        rewindData = new HashMap<>();
        bankStatus = BankStatus.ACTIVE;
    }

    /*# readXmlFile - Load relevant xml file to XMLManager.
    # arg::String filePath - path of xml file.
    # return value - DataTransferObject Object.*/
    // TODO - add information to specific customer.
    public synchronized List<LoanDataObject> loadXML(String fileContent,String fileName, String customerName) throws DataTransferObject {

        try {
           AbsDescriptor res = this.xmlManager.loadXMLfile(fileContent, fileName); // Try loading xml file and return AbdDescriptor.
            return bankSystem.LoadCustomerXML(res, customerName);
        } catch(DataTransferObject e) {
            throw e;
        }

    }

    // Section 2 from menu.
    // Returns list of all loan's DTO.
    public List<LoanDataObject> getAllLoansData() {
        return this.bankSystem.getCustomersLoansData();
    }

    public List<String> getAllCustomersNames() {
        if(bankSystem != null)
            return this.bankSystem.getCustomersNames();

        return null;
    }

    public CustomerDataObject getCustomerByName(String customerName) {
        if(bankSystem != null) {
            BankCustomer temp = this.bankSystem.getCustomerByName(customerName);
            return new CustomerDataObject(temp.getName(), temp.getCustomerLog(), temp.getLoansInvested(), temp.getLoansTaken(), temp.getBalance(), temp.getListOfAlerts());
        }

        return null;
    }

    // Section 3
    public List<CustomerDataObject> getAllCustomersLoansAndLogs() {
        return this.bankSystem.getAllCustomersLoansAndLogs();
    }

    // Section 4
    public boolean depositMoney(String userName, int depositeAmount) {
        return this.bankSystem.makeDepositeByName(userName, depositeAmount);
    }

    // Section 5
    public void withdrawMoney(String userName, int widthrawAmount) throws DataTransferObject {
        this.bankSystem.makeWithdrawByName(userName, widthrawAmount);
    }

    //Section 6
    public int getBalanceOfCustomerByName(String customerName) {
        return this.bankSystem.getCustomerByName(customerName).getBalance();
    }

    public Set<String> getBankCategories() {
        return this.bankSystem.getBankCategories();
    }

    public List<LoanDataObject> getRelevantPendingLoansList(String chosenCustomer, List<String> catChoice, int interest, int totalTime) {
        List<LoanDataObject> pendingLoans = this.getAllLoansData();

        if(pendingLoans == null)
            return null;

        List<LoanDataObject> resData = new ArrayList<>(); // result list.

        // filter loans list -> only loans with status PENDING or NEW & loan's owner is other then current customer.
        pendingLoans = pendingLoans.stream().filter(loan -> !loan.getOwner().equals(chosenCustomer)
                && (loan.getLoanStatus() == LoanDataObject.Status.PENDING || loan.getLoanStatus() == LoanDataObject.Status.NEW)).collect(Collectors.toList());

        // filter loans list -> if customer chose specific category then the list will hold loans from that category.
        if(!catChoice.get(0).equals("All"))
            for(String ch : catChoice)
                resData.addAll(pendingLoans.stream().filter(loan -> loan.getLoanCategory().equals(ch)).collect(Collectors.toList()));

        // filter loans lost -> if interent isnt equal to 0 -> hold loans with interest greater or equal to given interest.
        if(interest > 0)
            resData = resData.stream().filter(loan -> loan.getLoanInterestPerPayment() >= interest).collect(Collectors.toList());

        // filter loans list -> filter list by total time left to loans.
        if(totalTime > 0)
            resData = resData.stream().filter(loan -> loan.getLoanTotalTime() <= totalTime).collect(Collectors.toList());

        return resData;
    }

    // Get list of loans to invest.
    public String makeInvestments(InvestmentData investmentData) throws DataTransferObject {
        return this.bankSystem.makeInvestments(investmentData);
    }

    // Increase YAZ date by 1.
    public void increaseYazDate(AdminData adminData) {
        rewindData.put(BankSystem.getCurrentYaz(),adminData);
        this.bankSystem.increaseYazDate();

    }

    public List<CustomerDataObject> getAllCustomerData() {
        return this.bankSystem.getAllCustomersLoansAndLogs();
    }

    // Customer Payments.

    // Pay loan current payment.
    public void handleCustomerLoansPayments(LoanDataObject loan, int amountToPay) {
        this.bankSystem.handleCustomerLoanPayment(loan, amountToPay);
    }

    // Close all loans debt or specific loan.
    public void handleCustomerPayAllDebt(List<LoanDataObject> loans) throws DataTransferObject {
        this.bankSystem.handleCustomerPayAllDebt(loans);
    }

    // Add new customer to customer's list when first logged in.
    public void addNewCustomer(String customerName) {
        this.bankSystem.addNewCustomer(customerName);
    }

    public void markCustomerMessagesAsRead(String customerName) {
        this.bankSystem.markCustomerMessagesAsRead(customerName);
    }

    public boolean addLoan(CustomerDataObject customer, LoanDataObject loan) {

        if(this.getAllLoansData().contains(loan))
            return false;

        BankLoan bankLoan = new BankLoan(loan);
        this.bankSystem.getCustomerByName(customer.getName()).addLoan(bankLoan);
        return true;
    }

    public String changeLoanSellStatus(String name, String sellerName, String loanName) {
        return bankSystem.changeLoanSellStatus(name,sellerName, loanName);
    }

    public void handleLoanBuying(String loanOwner, String sellerName, String buyerName, String loanName) throws DataTransferObject {

        if(bankSystem.getCustomerByName(loanOwner) == null ||
                bankSystem.getCustomerByName(sellerName) == null ||
                    bankSystem.getCustomerByName(buyerName) == null) {
            throw new DataTransferObject("Error: one of the customer doesnt exist.", BankSystem.getCurrentYaz());
        }

        this.bankSystem.handleLoanBuying(loanOwner, sellerName, buyerName, loanName);

    }

    public AdminData rewindYazDate(Integer choosenYaz, AdminData adminData) {
        //save the real yaz
        realYaz = BankSystem.getCurrentYaz();
        //save cuurent yaz data
        rewindData.put(BankSystem.getCurrentYaz(),adminData);

        //change status to read only
        bankStatus = BankStatus.READ_ONLY;

        //get data from choosen yaz.
        if(choosenYaz< BankSystem.getCurrentYaz()){
            AdminData res = rewindData.get(choosenYaz);
            return res;
        }
        return null;
    }

    public AdminData endRewind() {
        //Cange status back to active.
        bankStatus = BankStatus.ACTIVE;
        //Return the data from real yaz.
        return rewindData.get(realYaz);
    }
}
