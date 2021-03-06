package abs;

import dto.JSON.AdminData;
import dto.JSON.InvestmentData;
import dto.infodata.DataTransferObject;
import dto.objectdata.CustomerDataObject;
import dto.objectdata.LoanDataObject;
import dto.objectdata.Triple;
import engine.convertor.Convertor;
import xmlgenerated.AbsDescriptor;
import xmlgenerated.AbsLoan;

import java.util.*;

public class BankSystem {

    private static int currentYaz = 1;
    private BankCategories categories;
    private Map<String, BankCustomer> customers;
    private List<AbsLoan> uploaded_loans_data_list; // Hold all loans pre-parse objects to compare with new ones.

    // History hold for admin rewind.
    private Map<Integer, AdminData> systemHistory;
    private static int adminYazTime = 1;

    public BankSystem()
    {
        // Create empty bank when the server is running.
        categories = new BankCategories();
        customers = new HashMap<>();
        uploaded_loans_data_list = new ArrayList<>();
        systemHistory = new HashMap<>();
    }

    public List<LoanDataObject> LoadCustomerXML(AbsDescriptor absDescriptor, String customerName) throws DataTransferObject
    {
        // Check if there is an existing loan and return error if so.
        if(!Collections.disjoint(uploaded_loans_data_list, absDescriptor.getAbsLoans().getAbsLoan()))
            throw new DataTransferObject("Error: There are loans that already exists in the system.", BankSystem.getCurrentYaz());

        uploaded_loans_data_list.addAll(absDescriptor.getAbsLoans().getAbsLoan()); // If all loans are new add the to list.

        // Parse and upload data into existing lists and data.
        categories.addAnotherCategoriesSet(Convertor.parseAbsCategories(absDescriptor.getAbsCategories()));

        return Convertor.parseAbsLoans(this.getCustomerByName(customerName), absDescriptor.getAbsLoans());
    }

    public static int getCurrentYaz() {
        return currentYaz;
    }

    public static int getAdminYazTime() {
        return adminYazTime;
    }

    public void increaseYazDate() {

        adminYazTime++;

        // Do increase yaz process only when were not on read only mode!
        if(adminYazTime > currentYaz) {

            this.saveDataToSystemHistory(); // Load all previous yaz data to history holder object.

            // make relevant investments payment.
            customers.values().forEach(BankCustomer::updateCustomerLoansStatus);

            currentYaz++; // increase YAZ date by 1.
        }
    }

    // Load all previous yaz data to history holder object.
    private void saveDataToSystemHistory() {
        AdminData adminData = new AdminData(this.getAllCustomersLoansAndLogs(), this.getCustomersLoansData(), BankSystem.getCurrentYaz());
        systemHistory.put(BankSystem.getCurrentYaz(), adminData);
    }

    // Close all loans.
    public void handleCustomerPayAllDebt(List<LoanDataObject> loans) throws DataTransferObject {

        if(loans == null || loans.size() <= 0)
            throw new DataTransferObject("There are no loans to pay debt for.", BankSystem.getCurrentYaz());

        // Calculate amount to pay to cover all loans.
        int totalToPay = 0;
        for(LoanDataObject loan : loans)
            totalToPay += loan.getInterestAmount() + loan.getLoanAmount();

        // Check if customer has enough balance.
        BankCustomer customer = this.getCustomerByName(loans.get(0).getOwner());
        if(totalToPay > customer.getBalance())
            throw new DataTransferObject("You dont have enough balance to cover all your loans.", BankSystem.getCurrentYaz());

        loans.forEach(customer::payLoanAllDebt); // pay all loan.
    }

    // Pay for specific loan.
    public void handleCustomerLoanPayment(LoanDataObject loan, int amountToPay) {

        if(loan == null)
            return;

        this.getCustomerByName(loan.getOwner()).payCustomerTakenLoan(loan, amountToPay);
    }

    // Return list of LoanDataObject -> all customers loans data goes inside that list.
    public List<LoanDataObject> getCustomersLoansData() {

        if(customers == null || customers.size() <= 0)
            return null;

        List<LoanDataObject> newList = new ArrayList<>();
        for(Map.Entry<String,BankCustomer> singleCustomer : this.customers.entrySet()) {
            for(LoanDataObject singleLoan : singleCustomer.getValue().getListOfLoans()) {
                newList.add(singleLoan);
            }
        }
        return newList;
    }

    // return list of users names.
    public List<String> getCustomersNames() {

        if( customers == null || customers.size() <= 0)
            return null;

        return new ArrayList<String>(customers.keySet());
    }

    // Makes a deposite for given user.
    public boolean makeDepositeByName(String nameOfUserToDeposit, int depositAmount) {

        if(customers.get(nameOfUserToDeposit) == null)
            return false;

        customers.get(nameOfUserToDeposit).deposite(depositAmount);
        return true;
    }

    public List<CustomerDataObject> getAllCustomersLoansAndLogs() {
        if(customers == null || customers.size() <= 0)
            return null;

        List<CustomerDataObject> customerData = new ArrayList<>();
        for(Map.Entry<String, BankCustomer> customer : customers.entrySet()) {
            customerData.add(new CustomerDataObject(customer.getValue().getName(), customer.getValue().getCustomerLog(), customer.getValue().getLoansInvested(), customer.getValue().getLoansTaken(), customer.getValue().getBalance(), customer.getValue().getListOfAlerts()));
        }

        return customerData;
    }

    // Makes a deposite for given user.
    public void makeWithdrawByName(String nameOfUserToDeposite, int depositeAmount) throws DataTransferObject {
        customers.get(nameOfUserToDeposite).withdraw(depositeAmount);
    }

    // Return customer if found by name.
    public BankCustomer getCustomerByName(String customerName) {
        return customers.get(customerName);
    }

    public Set<String> getBankCategories() {
        return this.categories.getBankCategories();
    }

    //Section 6 - from menu.
    public String makeInvestments(InvestmentData investmentData) {

        // Get list of Bank Loans from list of bank loans names.
        List<BankLoan> loansToInvest = this.makeLoansListFromLoansNames(investmentData.getNameOfLoansToInvest());

        //Sort the list by  the amount left to activate the loan.
        this.sortLoanslist(loansToInvest);

        // go through the list and invest money as much as possible and equally between all loans.
        return this.investEqually(getCustomerByName(investmentData.getInvestorName()) , investmentData.getInvestmentAmount() , loansToInvest);

    }

    // go through the list and invest money as much as possible and equally berween all loans.
    private String investEqually(BankCustomer customerName, int amountToInvest, List<BankLoan> loansToInvest) {
        String res = "New investments: \n";
        for (int i = 0 ; i < loansToInvest.size() ; i++) {

            int avgInvestmentAmount = amountToInvest / (loansToInvest.size() - i ); // Initial average investment in each loan.

            //for the last investment we try to invest the max amount letf
            int realTimeInvestedAmount = 0;
            if(i == (loansToInvest.size() -1)) {
                if (amountToInvest > 0)
                    realTimeInvestedAmount = loansToInvest.get(i).invest(this.getCustomerByName(loansToInvest.get(i).getOwner()), customerName, amountToInvest);
            } else {
                if (avgInvestmentAmount > 0)
                    realTimeInvestedAmount = loansToInvest.get(i).invest(this.getCustomerByName(loansToInvest.get(i).getOwner()), customerName, avgInvestmentAmount);
            }

            amountToInvest -= realTimeInvestedAmount;
            res += "Invested " + realTimeInvestedAmount + " in " + loansToInvest.get(i).getLoanID() + ".\n";
        }
        return res;
    }

    //Sort the list by  the amount left to activate the loan.
    private void sortLoanslist(List<BankLoan> loansToInvest) {

        Collections.sort(loansToInvest, new Comparator<BankLoan>() {
            @Override
            public int compare(BankLoan o1, BankLoan o2) {
                return o1.getAmountLeftToActivateLoan() < o2.getAmountLeftToActivateLoan() ? -1
                        : o1.getAmountLeftToActivateLoan() == o2.getAmountLeftToActivateLoan() ? 0 : 1;
            }
        });

    }

    // Return list of Bank Loans from list of bank loans names.
    private List<BankLoan> makeLoansListFromLoansNames(List<Triple<String,Integer,String>> customerLoansToInvestList) {
        List<BankLoan> loansToInvest = new ArrayList<>(customerLoansToInvestList.size());
        customerLoansToInvestList.stream().forEach(loan -> {
            loansToInvest.add(customers.get(loan.getKey()).getLoanByNameAndYaz(loan.getValue(), loan.getExtraData()));
        });

        return loansToInvest;
    }

    // Add new customer to customers' list when first logged in.
    public void addNewCustomer(String customerName) {
        this.customers.put(customerName, new BankCustomer(customerName));
    }

    public void markCustomerMessagesAsRead(String customerName) {
        this.customers.get(customerName).markCustomerMessagesAsRead();
    }

    public String changeLoanSellStatus(String name, String sellerName, String loanName) {
        return this.getCustomerByName(name).changeLoanSellStatus(sellerName, loanName);
    }

    public void handleLoanBuying(String loanOwnerName, String sellerName, String buyerName, String loanName) throws DataTransferObject{

        BankCustomer loanOwner = this.getCustomerByName(loanOwnerName);
        BankCustomer seller = this.getCustomerByName(sellerName);
        BankCustomer buyer = this.getCustomerByName(buyerName);

        loanOwner.changeShareOwner(seller, buyer, loanName);

    }

    public AdminData getPrevYazData(Integer yaz_time) {
        return this.systemHistory.get(yaz_time);
    }

    public void decreaseYaz() {
        if(adminYazTime > 1)
            adminYazTime--;
    }
}
