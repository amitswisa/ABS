package dto.objectdata;

import dto.infodata.DataTransferObject;

import java.util.List;
import java.util.Map;

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
    private final int amountLeftToPay; //left to pay to activate
    private final int loanStartTime; // in yaz - set value when being active.
    private final int loanEndTime;
    private final int loanInterestPerPayment;
    private final int paymentInterval; // Time in yaz for every customer payment.(ex: every 2 yaz etc...)
    private int unfinishedLoansNumber;
    private final Status loanStatus;
    //private Map<String, Investor> loanInvestors; // List of investors and their amount of investment;
    private List<TransactionDataObject> transactionList; // hold all transaction's history.

    public LoanDataObject(String owner, String loanID, String loanCategory, int loanAmount, int loanOpeningTime, int loanTotalTime,
                          int loanStartTime, int loanEndTime, int loanInterestPerPayment, int paymentInterval, Status loanStatus,
                          int amountLeftToPay, List<TransactionDataObject> transactionList)
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
    }

    // Section 2 from menu.
    /*public void showLoan() {
        System.out.println(this);

        //Print all the investors.
        Map<String, Investor> temp = this.loan.getLoanInvestors();
        if(temp != null && temp.size() > 0) {
            System.out.println("List of investors: ");
            for (Map.Entry<String, Investor> invester : temp.entrySet())
                System.out.println("Name:" + invester.getKey() + ", Investment: " + invester.getValue().getInitialInvestment());
        } else
            System.out.println("No investors at this moment.");

        //Show more deatails according to loan status.
        BankLoan.Status status = this.loan.getLoanStatus();
        switch (status){
            case ACTIVE: {
                this.presentActiveStatusData();
                break;
            }
            case RISK: {
                this.presentActiveStatusData();
                this.presentRiskStatusData();
                break;
            }
            case FINISHED: {
                System.out.println("Starting loan time: " + this.loan.getLoanStartTime());
                System.out.println("Ending loan time: " + this.loan.getLoanEndTime());
                break;
            }
            default: {
                if(status != BankLoan.Status.PENDING && status != BankLoan.Status.NEW) // if current loan status is not pending or any of the above.
                    System.out.println("Error: invalid loan status.");
            }

        }
        System.out.println(" ");
    }

    private void presentActiveStatusData() {
        System.out.println("Started YAZ:" + this.loan.getLoanStartTime()); //print the YAZ start to be active

        // Calculate next payment in yaz.
        int nextYaz = this.loan.getNextPaymentTime();
        if(nextYaz <= BankSystem.getCurrentYaz())
            nextYaz += + this.loan.getPaymentInterval();
        System.out.println("Next YAZ payment: " + nextYaz);

        int totalInterestPayed = 0;
        int totalLoanPayment = 0;
        // Print all transaction that are payed in time.
        for(BankLoanTransaction tranc : this.loan.getPayedTransactions()) {
            System.out.println(tranc);
            totalInterestPayed += tranc.getInterestValue();
            totalLoanPayment += tranc.getPaymentValue();
        }

        System.out.println("Total payments already payed: " + totalLoanPayment);
        System.out.println("Total interest already payed: " + totalInterestPayed);
        System.out.println("Total payments left to pay: " + (this.loan.getLoanAmount()-totalLoanPayment));
        System.out.println("Total interest left to pay: "
                + (this.loan.getTotalLoanInterestInMoney()-totalInterestPayed));

    }

    private void presentRiskStatusData() {
        System.out.println("There are " + this.loan.getUnpayedTransactions().size() + " unpaied payments.");
        System.out.println("Total unpaid payments so far: "
                + (this.loan.getLastUnPaidTransaction().getPaymentValue() + this.loan.getLastUnPaidTransaction().getInterestValue()));
    }

    // Part of section 3 from menu.
    public String getLoanDetails() {
         return "   Loan Name: " + this.loan.getLoanID() + ".\n" +
                "      Loan Category: " + this.loan.getLoanCategory() + ".\n" +
                "      Loan Amount: " + this.loan.getLoanAmount() + ".\n" +
                "      Payment Interval: " + this.loan.getPaymentInterval() + ".\n" +
                "      Loan Interest Per Payment: " + this.loan.getLoanInterestPerPayment() + ".\n" +
                "      Loan total (interest+capital): " + (this.loan.getTotalLoanInterestInMoney()+this.loan.getLoanAmount()) + ".\n" +
                "      Loan Status: " + this.loan.getLoanStatus() + ".\n" +
                this.getLoanDetailsAccordingToStatus();
    }

    private String getLoanDetailsAccordingToStatus() {
        String res = "";
        switch(loan.getLoanStatus()) {
            case PENDING: {
                res = "      Amount left to make loan active: " + loan.getAmountLeftToActivateLoan() + "\n";
                break;
            }
            case ACTIVE: {
                res = "      Next payment date (in YAZ): " + this.loan.getNextPaymentTime()
                        +".\n      Next payment value: " + ((this.loan.getLoanAmount()/(this.loan.getLoanTotalTime()/this.loan.getPaymentInterval()))+this.loan.getLoanInterestPerPayment()) + "\n";
                break;
            }
            case RISK: {
                res = "      Total value of unpaied transactions: " + this.loan.getUnpayedTransactionsAmountOfMoney() + "\n";
                break;
            }
            case FINISHED: {
                res = "      Loan started at: " + this.loan.getLoanStartTime() +
                        ".\n      Loan ending time: " + (this.loan.getLoanEndTime()) + "\n";
                break;
            }
        }
        return res;
    }*/

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
                return payment.getPaymentValue();
        }
        return -1; //return worng amount in case all transactions payed
    }

    //Returns the number of payments.
    public int getNumberOfPayment(){
        return this.loanTotalTime / this.paymentInterval;
    }

    //Returns the amount that left to close the loan.
    public int getAmountLeftToPay() {
        int paidAlready= 0 ;
        for (TransactionDataObject payment:this.transactionList) {
            if(payment.getTransactionStatus() == TransactionDataObject.Status.DEPT_COVERED ||
                    payment.getTransactionStatus() == TransactionDataObject.Status.PAYED)
                paidAlready += payment.getPaymentValue();
        }
        return this.loanAmount - paidAlready;
    }

    public int getCapitalANDIntrest() {
        for (TransactionDataObject payment :  this.transactionList) {
            if (payment.getTransactionStatus()== TransactionDataObject.Status.NOT_PAYED )
                return payment.getInterestValue() + payment.getPaymentValue();
        }
        return -1; //return worng amount in case all transactions payed
    }

    public int getPaymentInterval() {return this.paymentInterval;}

    public int getAmountLeftToPayToActivate() {return this.amountLeftToPay;}

    public int getLoanOpeningTime() {
        return this.loanOpeningTime;
    }

    public void setUnfinishedLoansNumber(int val) {
        this.unfinishedLoansNumber = val;
    }

    public int getUnfinishedLoansNumber() {return this.unfinishedLoansNumber;}

   /* @Override
    public String toString() {
        return  "Loan ID: " + this.loan.getLoanID() + "\n" +
                "Loan Category: " + this.loan.getLoanCategory() + "\n" +
                "Loan Amount: " + this.loan.getLoanAmount() + "\n" +
                "Original Time of loan: " + this.loan.getLoanTotalTime() + "\n" +
                "Loan Interest: " + this.loan.getTotalLoanInterestInMoney() + "\n" +
                "Payment Interval: " + this.loan.getPaymentInterval() + "\n" +
                "Loan Status: " + this.loan.getLoanStatus();
    }*/

}
