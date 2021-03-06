package dto.objectdata;

import com.google.gson.*;
import dto.infodata.DataTransferObject;
import javafx.util.Pair;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static dto.objectdata.LoanDataObject.Status.*;

public class LoanDataObject extends DataTransferObject {

    public enum Status {
    NEW,
    PENDING,
    ACTIVE,
    RISK,
    FINISHED
}

    private final String owner;
    private final String loanID;
    private final String loanCategory;
    private final int loanAmount; // Loan amount of money.
    private final int loanOpeningTime; // Time in YAZ that customer opened new loan.
    private final int loanTotalTime;
    private int amountLeftToPay; //left to pay to activate
    private int loanStartTime; // in yaz - set value when being active.
    private int loanEndTime;
    private final int loanInterestPerPayment;
    private final int paymentInterval; // Time in yaz for every customer payment.(ex: every 2 yaz etc...)
    private int unfinishedLoansNumber;
    private Status loanStatus;
    private List<Pair<String, Integer>> investersList = new ArrayList<>();
    private Map<String, Boolean> shareSellList;
    private List<TransactionDataObject> transactionList = new ArrayList<>(); // hold all transaction's history.

    // XML Constructor.
    public LoanDataObject(String loan_owner, String loan_id, String loan_category, int loan_amount, int loan_interest
            ,int loan_payment_interval, int loan_opening_time, int loan_total_time) {

        this(loan_owner, loan_id, loan_category, loan_amount, loan_opening_time,
                loan_total_time, 0, 0, loan_interest, loan_payment_interval, NEW, 0, new ArrayList<>(), new ArrayList<>(), new HashMap<>());

    }

    public LoanDataObject(String owner, String loanID, String loanCategory, int loanAmount, int loanOpeningTime, int loanTotalTime,
                          int loanStartTime, int loanEndTime, int loanInterestPerPayment, int paymentInterval, Status loanStatus,
                          int amountLeftToPay, List<TransactionDataObject> transactionList,
                          List<Pair<String, Integer>> investersList, Map<String, Boolean> shareSellList)
    {
        super();
        this.owner = owner;
        this.loanID = loanID;
        this.loanCategory = loanCategory;
        this.loanAmount = loanAmount;
        this.loanOpeningTime = loanOpeningTime;
        this.loanTotalTime = loanTotalTime;
        this.loanStartTime = loanStartTime;
        this.loanEndTime = loanEndTime;
        this.loanInterestPerPayment = loanInterestPerPayment;
        this.paymentInterval = paymentInterval;
        this.loanStatus = loanStatus;
        this.amountLeftToPay = amountLeftToPay;
        this.transactionList = transactionList;
        this.investersList = investersList;
        this.shareSellList = shareSellList;
    }

    //check if the loan details it's ok. when the client open loan by himself.
    public boolean isValidLoan() {
        // Validate payment.
        float isValidPayment = (float)this.loanTotalTime / (float)this.paymentInterval;
        return isValidPayment == ((int)isValidPayment);
    }

    public int getUnpayedTransactionsAmount(int yaz) {

        // if all transactions alreadty paied.
        if(this.transactionList == null)
            return 0;

        return this.transactionList.stream().filter(s -> s.getTransactionStatus() == TransactionDataObject.Status.NOT_PAYED
                && s.getPaymentTime() <= yaz).collect(Collectors.toList()).size();
    }

    public TransactionDataObject getLastUnPaidTransaction(int yaz) {
        for(int i = this.transactionList.size();i > 0;i--) {
            TransactionDataObject temp = transactionList.get(i-1);
            if(temp.getTransactionStatus() == TransactionDataObject.Status.NOT_PAYED
                    && temp.getPaymentTime() <= yaz)
                return temp;
        }
        return null;
    }

    public String getOwner() {
        return this.owner;
    }

    public int getLoanTotalTime() {
        return this.loanTotalTime;
    }

    public int getLoanInterestPerPayment() {return this.loanInterestPerPayment;}

    public int getLoanAmount() {return this.loanAmount;}

    public String getLoanCategory() {return this.loanCategory;}

    public LoanDataObject.Status getLoanStatus() {return this.loanStatus;}

    public String getLoanID() {return this.loanID; }

    public int getPaymentYaz() {
        for (TransactionDataObject payment :  this.transactionList) {
            if (payment.getTransactionStatus()== TransactionDataObject.Status.NOT_PAYED )
             return payment.getPaymentTime();
        }
        return -1; //return worng yaz in case all transactions payed
    }

    //Returns the current payment number.
    public int getThisPaymentNumber(){
        int count=1, i=0;

        while(this.transactionList.get(i).getTransactionStatus() == TransactionDataObject.Status.PAYED || this.transactionList.get(i).getTransactionStatus() == TransactionDataObject.Status.DEPT_COVERED) {
            count++;
            i++;
        }
        return count;
    }

    public int getThisPaymentAmount(){
        for (TransactionDataObject payment :  this.transactionList) {
            if (payment.getTransactionStatus()== TransactionDataObject.Status.NOT_PAYED )
                return payment.getPaymentValue() + payment.getInterestValue();
        }
        return -1; //return worng amount in case all transactions payed
    }

    //Returns the number of payments.
    public int getNumberOfPayment(){
        return this.loanTotalTime / this.paymentInterval;
    }

    //Returns the amount that left to close the loan.
    public int getAmountLeftToPayTofinished() {
        int paidAlready= 0 ;
        for (TransactionDataObject payment:this.transactionList) {
            if(payment.getTransactionStatus() == TransactionDataObject.Status.DEPT_COVERED ||
                    payment.getTransactionStatus() == TransactionDataObject.Status.PAYED)
                paidAlready += (payment.getPaymentValue() + payment.getInterestValue());
        }
        return this.loanAmount + getInterestAmount() - paidAlready;
    }

    public int getLastPayment(int currentYaz) {

        // Fix problem of first.
        int size = this.transactionList.size();
        for(int i = 0;i < size;i++) {
            TransactionDataObject temp = transactionList.get(i);
            if(temp.getTransactionStatus() == TransactionDataObject.Status.NOT_PAYED
                    && temp.getPaymentTime() >= currentYaz)
                return temp.getTotalPayment();
        }

        return transactionList.get(size-1).getTotalPayment();
    }

    public int getInterestAmount() {
        return (this.loanAmount * this.loanInterestPerPayment)/100;
    }

    public int getPaymentInterval() {return this.paymentInterval;}

    public int getAmountLeftToPay() {return this.amountLeftToPay;}

    public int getLoanOpeningTime() {
        return this.loanOpeningTime;
    }

    public void setUnfinishedLoansNumber(int val) {
        this.unfinishedLoansNumber = val;
    }

    public int getUnfinishedLoansNumber() {return this.unfinishedLoansNumber;}

    public int getLoanInterestInMoney() {
        return ((this.getLoanAmount() * this.getLoanInterestPerPayment()) / 100);
    }

    /* Loan data show */
    // Section 2 from menu.
    public String showLoanData(int yaz) {

        String res = "";

        //Print all the investors.
        List<Pair<String, Integer>> temp = this.investersList;
        if(this.investersList != null && this.investersList.size() > 0) {
            res += "List of investors: \n";
            for (Pair<String, Integer> invester: this.investersList)
                res += "   Name: " + invester.getKey() + ", Investment: " + invester.getValue() + ". \n";
        } else
            res += "   No investors at this moment.\n";

        //Show more deatails according to loan status.
        switch (this.getLoanStatus()){
            case ACTIVE: {
                res += this.presentActiveStatusData();
                break;
            }
            case RISK: {
                res += this.presentActiveStatusData();
                res += this.presentRiskStatusData(yaz);
                break;
            }
            case FINISHED: {
                res += "Starting loan time: " + this.getLoanOpeningTime() + "\n";
                res += "Ending loan time: " + this.loanEndTime;
                break;
            }
            default: {
                if(this.getLoanStatus() != PENDING && this.getLoanStatus() != NEW) // if current loan status is not pending or any of the above.
                    res += "Error: invalid loan status.\n";
            }

        }
        res += " \n";
        return res;
    }

    private String presentActiveStatusData() {
        String res = "\nStarted YAZ:" + this.getLoanOpeningTime() + "\n"; //print the YAZ start to be active

        // Calculate next payment in yaz.
        int nextYaz = this.getPaymentYaz();
        res += "Next YAZ payment: " + nextYaz + ".\n\n";

        res += "List of payed payments: \n";

        int totalInterestPayed = 0;
        int totalLoanPayment = 0;

        List<TransactionDataObject> filteredList = this.transactionList.stream()
                .filter(e -> e.getTransactionStatus() == TransactionDataObject.Status.PAYED)
                .collect(Collectors.toList());

        if(filteredList.size() <= 0)
            res += "   There are no payed payments yet.\n\n";
        else {
            for (TransactionDataObject tranc : filteredList) {
                res += tranc + "\n\n";
                totalInterestPayed += tranc.getInterestValue();
                totalLoanPayment += tranc.getPaymentValue();
            }
        }

        res += "Total payments already payed: " + totalLoanPayment + "\n";
        res += "Total interest already payed: " + totalInterestPayed + "\n";
        res += "Total payments left to pay: " + (this.getLoanAmount()-totalLoanPayment) + "\n";
        res += "Total interest left to pay: " + (this.getLoanInterestInMoney()/this.getNumberOfPayment()) * (this.getNumberOfPayment()-this.getThisPaymentNumber()+1) + ".\n\n";
        System.out.println(this.getLoanInterestInMoney()/this.getNumberOfPayment());
        System.out.println((this.getNumberOfPayment()-this.getThisPaymentNumber()));

        return res;
    }

    private String presentRiskStatusData(int yaz) {
        String res = "There are " + this.getUnpayedTransactionsAmount(yaz) + " unpaied payments.\n";
        res += "Total unpaid payments so far: "
                + (this.getLastUnPaidTransaction(yaz).getPaymentValue() + this.getLastUnPaidTransaction(yaz).getInterestValue()) + ".\n";
        return res;
    }

    public int getLoanStartTime() {
        return loanStartTime;
    }

    public int getLoanEndTime() {
        return loanEndTime;
    }

    public List<Pair<String, Integer>> getInvestersList() {
        return investersList;
    }

    public List<TransactionDataObject> getTransactionList() {
        return transactionList;
    }

    public void update(LoanDataObject e) {
        this.amountLeftToPay = e.getAmountLeftToPay();
        this.loanStartTime = e.getLoanStartTime();
        this.loanEndTime = e.getLoanEndTime();
        this.unfinishedLoansNumber = e.getUnfinishedLoansNumber();
        this.loanStatus = e.getLoanStatus();
        this.investersList = e.getInvestersList();
        this.transactionList = e.getTransactionList();
    }

    public Map<String, Boolean> getShareSellList() {
        return shareSellList;
    }

    public boolean getInvestorShareSellStatus(String investorName) {

        Boolean sellStatus = getShareSellList().get(investorName);

        if(sellStatus == null)
            return false;

        return sellStatus;
    }

    public Integer getInvestorShare(String sellerName) {

        for(Pair<String, Integer> pair : this.getInvestersList())
            if(pair.getKey().equals(sellerName))
                return pair.getValue();

        return null;
    }

    @Override
    public String toString() {
        return this.getLoanID();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LoanDataObject that = (LoanDataObject) o;

        return loanID.equals(that.loanID);
    }

    @Override
    public int hashCode() {
        return loanID.hashCode();
    }

    public static class LoanDataObjectAdapter implements JsonSerializer<LoanDataObject> {

        @Override
        public JsonElement serialize(LoanDataObject loan, Type type, JsonSerializationContext jsc) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("owner", loan.getOwner());
            jsonObject.addProperty("loanID", loan.getLoanID());
            jsonObject.addProperty("loanCategory", loan.getLoanCategory());
            jsonObject.addProperty("loanAmount", loan.getLoanAmount());
            jsonObject.addProperty("loanOpeningTime", loan.getLoanOpeningTime());
            jsonObject.addProperty("loanTotalTime", loan.getLoanTotalTime());
            jsonObject.addProperty("amountLeftToPay", loan.getAmountLeftToPay());
            jsonObject.addProperty("loanStartTime", loan.getLoanStartTime());
            jsonObject.addProperty("loanEndTime", loan.getLoanEndTime());
            jsonObject.addProperty("loanInterestPerPayment", loan.getLoanInterestPerPayment());
            jsonObject.addProperty("paymentInterval", loan.getPaymentInterval());
            jsonObject.addProperty("unfinishedLoansNumber", loan.getUnfinishedLoansNumber());
            jsonObject.addProperty("loanStatus", loan.getLoanStatus().toString());
            jsonObject.add("investersList", new Gson().toJsonTree(loan.getInvestersList()));
            jsonObject.add("transactionList", new Gson().toJsonTree(loan.getTransactionList()));
            jsonObject.add("shareSellList", new Gson().toJsonTree(loan.getShareSellList()));
            return jsonObject;
        }
    }
}

