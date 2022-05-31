package abs;

import dto.objectdata.CustomerAlertData;
import dto.objectdata.LoanDataObject;
import dto.objectdata.TransactionDataObject;
import javafx.util.Pair;
import xmlgenerated.AbsLoan;

import java.util.*;
import java.util.stream.Collectors;

public class BankLoan {

    private String owner;
    private String loanID;
    private String loanCategory;
    private final int loanAmount; // Loan amount of money.
    private final int loanOpeningTime; // Time in YAZ that customer opened new loan.
    private int loanTotalTime;
    private int loanStartTime; // in yaz - set value when being active.
    private int loanEndTime;
    private int loanInterestPerPayment;
    private int paymentInterval; // Time in yaz for every customer payment.(ex: every 2 yaz etc...)
    private LoanDataObject.Status loanStatus;
    private Map<String, Investor> loanInvestors; // List of investors and their amount of investment;
    private List<TransactionDataObject> transactionList; // hold all transaction's history.

    public BankLoan(AbsLoan absLoan) {
        this.owner = absLoan.getAbsOwner();
        this.loanID = absLoan.getId();
        this.loanCategory = absLoan.getAbsCategory();
        this.loanAmount = absLoan.getAbsCapital();
        this.loanTotalTime = absLoan.getAbsTotalYazTime();
        this.loanOpeningTime = 1;
        this.loanStartTime = 0;
        this.loanInterestPerPayment = absLoan.getAbsIntristPerPayment();
        this.paymentInterval = absLoan.getAbsPaysEveryYaz();
        this.loanInvestors = new HashMap<>();
        this.loanStatus = LoanDataObject.Status.NEW;
        transactionList = new ArrayList<>();
    }

    // Return next payment time for current loan in Yaz.
    public int getNextPaymentTime(){

       int paymentTime = this.loanStartTime + this.paymentInterval;
       while(paymentTime < BankSystem.getCurrentYaz())
           paymentTime += this.paymentInterval;

       return paymentTime; // default values for unfound objects after filter.

    }

    public int invest(BankCustomer customer , int InvestmentAmount) {

       int amountToActivate = this.getAmountLeftToActivateLoan();

       // If some investment covers loan's funds so activate it.
      if(InvestmentAmount >= amountToActivate) {
          this.addInvestor(customer, amountToActivate);
          //Change status loan
          this.loanStartTime = BankSystem.getCurrentYaz();
          this.loanStatus = LoanDataObject.Status.ACTIVE;
          this.updateBankLoansTransactionsList(); // Update list to contain all future payments & updates status to not_paied.
          return amountToActivate; //return the amount actually success to invest
      }

      if(this.loanStatus == LoanDataObject.Status.NEW)
          this.setStatus(LoanDataObject.Status.PENDING);

      this.addInvestor(customer, InvestmentAmount);
      return InvestmentAmount;
    }

    // Update list to contain all future payments & updates status to not_paied.
    private void updateBankLoansTransactionsList() {

        for(int i = 0;i<this.getLoanNumberOfPayments();i++) {

            int paymentValue = this.getLoanAmount()/this.getLoanNumberOfPayments();
            int paymentInterest = this.getTotalLoanInterestInMoney() / this.getLoanNumberOfPayments();

            // Make sure the whole amount will be paied and if neccessry add leftover to last payment.
            if(i == transactionList.size()-1) {
                paymentValue = this.getLoanAmount() - ((this.getLoanNumberOfPayments() - 1) * this.getLoanAmount() / this.getLoanNumberOfPayments());
                paymentInterest = this.getTotalLoanInterestInMoney() - ((this.getLoanNumberOfPayments() - 1) * this.getTotalLoanInterestInMoney() / this.getLoanNumberOfPayments());
            }

            this.transactionList.add(i,
                    new TransactionDataObject(this.getLoanStartTime() + (this.getPaymentInterval()*(i+1)),paymentValue, paymentInterest, TransactionDataObject.Status.NOT_PAYED));
        }
    }

    private void addInvestor(BankCustomer customer, int investment) {


       customer.addInvestment(this, investment);

        //Amount of capital the customer get per payment.
        int capital = investment / (this.loanTotalTime / this.paymentInterval);
        //Amount of interest the customer get per payment.
        int interest = (capital * this.loanInterestPerPayment) / 100;

        if(loanInvestors.get(customer.getName()) != null)
            loanInvestors.get(customer.getName()).addFundsToInvestment(investment, capital, interest);
        else
            loanInvestors.put(customer.getName(),new Investor(customer, capital, interest, investment));
    }

    public String getOwner() {
        return owner;
    }

    public String getLoanID() {
        return loanID;
    }

    public String getLoanCategory() {
        return loanCategory;
    }

    public int getLoanAmount() {
        return loanAmount;
    }

    public int getLoanTotalTime() {
        return loanTotalTime;
    }

    public int getLoanStartTime() {
        return loanStartTime;
    }

    public int getLoanInterestPerPayment() {
        return loanInterestPerPayment;
    }

    public int getPaymentInterval() {
        return paymentInterval;
    }

    public Map<String, Investor> getLoanInvestors() {
        return loanInvestors;
    }

    public List<Pair<String, Integer>> getLoanInvestorsToView() {
        List<Pair<String, Integer>> list = new ArrayList<>();

        // populate list with pairs of investor name and investment amount.
        getLoanInvestors().forEach((str,inv) -> {
            list.add(new Pair<>(str, inv.getInitialInvestment()));
        });

        return list;
    }

    public LoanDataObject.Status getLoanStatus() {
        return loanStatus;
    }

    public List<TransactionDataObject> getTransactionList() {
        return transactionList;
    }

    public int getLoanOpeningTime() {
        return this.loanOpeningTime;
    }

    public int getLoanNumberOfPayments() {
        return this.getLoanTotalTime() / this.getPaymentInterval();
    }

    // Return list of all transactions that already payed before current YAZ.
    public List<TransactionDataObject> getPayedTransactions() {
        return this.transactionList.stream().filter(s -> s.getTransactionStatus() == TransactionDataObject.Status.PAYED
                && s.getPaymentTime() <= BankSystem.getCurrentYaz()).collect(Collectors.toList());
    }

    // Return list of all transactions that didnt payed yet before current YAZ.
    public List<TransactionDataObject> getUnpayedTransactions() {
        return this.transactionList.stream().filter(s -> s.getTransactionStatus() == TransactionDataObject.Status.NOT_PAYED
                && s.getPaymentTime() <= BankSystem.getCurrentYaz()).collect(Collectors.toList());
    }

    public TransactionDataObject getLastUnPaidTransaction() {
        for(int i = this.transactionList.size();i > 0;i--) {
            TransactionDataObject temp = transactionList.get(i-1);
            if(temp.getTransactionStatus() == TransactionDataObject.Status.NOT_PAYED
                    && temp.getPaymentTime() <= BankSystem.getCurrentYaz())
                return temp;
        }
        return null;
    }

    // Return total unpaied transactions amount of money.
    public int getUnpayedTransactionsAmountOfMoney() {
        List<TransactionDataObject> unPayedTransactions = this.getUnpayedTransactions();

        // if all transactions alreadty paied.
        if(unPayedTransactions == null)
            return 0;

        return unPayedTransactions.stream().mapToInt(e->e.getPaymentValue()+e.getInterestValue()).sum();
    }

    // Return loan interest total value.
    public int getTotalLoanInterestInMoney() {
        return ((this.loanAmount*this.getLoanInterestPerPayment())/100);
    }

    // Return total loan amount minus existing invesments money.
    public int getAmountLeftToActivateLoan() {
        return this.getLoanAmount() - loanInvestors.values().stream().mapToInt(e -> e.getInitialInvestment()).sum();
    }

    public void setStatus(LoanDataObject.Status loanStatus) {
        this.loanStatus = loanStatus;
    }

    public int getLoanEndTime() {
        return this.loanEndTime;
    }

    private int getLastPaymentDate() {
        return this.getLoanStartTime() + this.getLoanTotalTime();
    }

    // This function make payment according to investors investment part when payday is arrived.
    // pay loan payment -> if amountToPay.equals(-1) amountToPay is all balane
    public int makePayment(BankCustomer loanOwner, int amountToPay) {

        // if customer has enough money in his balance to make the payment of this loan.
        int lastPaymentIndex = this.getLastPaymentDate() / this.getPaymentInterval();
        int paymentNumber = this.getLastPaymentIndex(lastPaymentIndex);

        TransactionDataObject loanToPay = transactionList.get(paymentNumber - 1);

        // If loan owner have enough money to pay all loans.
        if(loanOwner.getBalance() >= (loanToPay.getPaymentValue() + loanToPay.getInterestValue())) {

            // go through ivestors list and pay them accordingly
            int numberOfUnPaidPayments = this.transactionList.stream().filter(e -> e.getTransactionStatus() == TransactionDataObject.Status.NOT_PAYED && e.getPaymentTime() <= BankSystem.getCurrentYaz()).collect(Collectors.toList()).size();
            loanInvestors.values().forEach(investor -> {
                investor.getInvestor().addInvestmentMoneyToBalance(this.owner, this.getLoanID(), investor.getPaymentAmount() * numberOfUnPaidPayments);
            });

            // change current payment to payed.
            loanToPay.setTransactionStatus(TransactionDataObject.Status.PAYED);

            // Change loan status to finish when last payment is made.
            if (paymentNumber == this.getLoanNumberOfPayments()) {
                this.setStatus(LoanDataObject.Status.FINISHED);
                this.loanEndTime = BankSystem.getCurrentYaz();
                loanOwner.addAlert(this.getLoanID(),"loan has been payed!", CustomerAlertData.Type.CONFIRMATION);
            } else if (this.getLoanStatus() == LoanDataObject.Status.RISK) {
                this.setStatus(LoanDataObject.Status.ACTIVE);
                loanOwner.addAlert(this.getLoanID(), "loan's status changed back to ACTIVE!", CustomerAlertData.Type.CONFIRMATION);

                // change previouse unpaid payments status from UN_PAID to DEBT_COVERED
                for(int i = 0;i < paymentNumber;i++) {
                    TransactionDataObject tranc = transactionList.get(i);
                    if(tranc.getTransactionStatus() == TransactionDataObject.Status.NOT_PAYED)
                        tranc.setTransactionStatus(TransactionDataObject.Status.DEPT_COVERED);
                }
            }

            return (loanToPay.getPaymentValue() + loanToPay.getInterestValue());
        } else {
            this.updateLoanStatus(loanOwner);
            return 0;
        }
    }

    // Update loan status according to current yaz.
    public void updateLoanStatus(BankCustomer loanOwner) {

        int lastPaymentIndex = this.getLastPaymentDate() / this.getPaymentInterval();
        int paymentNumber = this.getLastPaymentIndex(lastPaymentIndex);
        TransactionDataObject loanToPay = transactionList.get(this.getLastPaymentIndex(lastPaymentIndex) - 1);

        if(loanToPay.getTransactionStatus() == TransactionDataObject.Status.NOT_PAYED)
        {
            this.setLoanStatusToRisk(loanOwner); // define loans status as risk and update notifications.

            // if this is not the last payment add this debt to next payment.
            if (paymentNumber < lastPaymentIndex)
                transactionList.get(paymentNumber).addDebt(loanToPay.getPaymentValue(), loanToPay.getInterestValue());
        }

    }

    // Change loan status to risk and add notifications.
    private void setLoanStatusToRisk(BankCustomer loanOwner) {
        // Add notification message
        if(this.getLoanStatus() == LoanDataObject.Status.ACTIVE)
            loanOwner.addAlert(this.getLoanID(), "loan's status changed to RISK.", CustomerAlertData.Type.ALERT);
        else
            loanOwner.addAlert(this.getLoanID(), "Couldn't set payments due to low balance.", CustomerAlertData.Type.ALERT);

        // If balance is not enough to make the payment.
        this.setStatus(LoanDataObject.Status.RISK); // set status to RISK.
    }

    // Returns the index of current payment need to pay.
    private int getLastPaymentIndex(int lastPaymentIndex) {
        int paymentNumber = (BankSystem.getCurrentYaz() - this.getLoanStartTime()) / this.getPaymentInterval();
        if(paymentNumber > lastPaymentIndex)
            paymentNumber = lastPaymentIndex;

        return paymentNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankLoan bankLoan = (BankLoan) o;
        return loanID.equals(bankLoan.loanID);
    }

    @Override
    public int hashCode() {
        return loanID.hashCode();
    }
}
