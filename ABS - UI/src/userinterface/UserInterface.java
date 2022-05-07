package userinterface;

import abs.BankSystem;
import dto.infodata.DataTransferObject;
import dto.objectdata.CustomerDataObject;
import dto.objectdata.LoanDataObject;
import engine.EngineManager;
import generalObjects.Triple;
import javafx.util.Pair;

import javax.xml.crypto.Data;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class UserInterface {

    private final Scanner scanner;
    private EngineManager engine; // Program engine, contains all program processes.

    public UserInterface() {
        scanner = new Scanner(System.in); // Init scanner.
        engine = new EngineManager(); // Init program engine.
    }

    // Initialize menu display.
    public void init() {
        do {
            this.presentMenu();
            int userChoice = this.getUserChoice(); // Read user choice.
            this.cleanBuffer(); // Clean '\n' stuck in buffer after reading choice.

            // Skip current loop when user chose 2/3/6/7 without loading xml file first.
            if ( this.unvalidFirstChoice(userChoice))
                continue;

            // Handle user choice by calling relevant function from program engine.
            this.applyUserChoice(userChoice);

        } while(true);

    }

    private boolean unvalidFirstChoice(int userChoice) {

        if ((userChoice == 2 || userChoice == 3 || userChoice == 6 || userChoice == 7)
                && !this.engine.isFileLoaded()) {
            System.out.println("Error: Xml file must be loaded first.");
           return true;
        }
        return false;
    }

    // Handle user choice by calling relevant function from program engine.
    private void applyUserChoice(int userChoice) {

            switch (userChoice) {
                case 1: {
                    this.loadXml(); //load xml from user
                    break;
                }
                case 2: {
                    this.getAllLoansInformation();// Get all loans information and status in BankSystem.
                    break;
                }
                case 3: {
                    this.getAllCustomerInformation();// Get all customer information in BankSystem.
                    break;
                }
                case 4: {
                    this.depositMoneyToCustomer(); //Deposit money to a specific user.
                    break;
                }
                case 5: {
                    this.withrawalMoneyToCustomer(); //Withrawal money to a specific user.
                    break;
                }
                case 6: {
                    this.startInvestmentProccess(); //customer choose invest money and investments.
                    break;
                }
                case 7: {
                    this.increaseYazDate();
                    break;
                }
                case 8: {
                    System.exit(0);
                    break;
                }
                default: {
                    System.out.println("Error: invalid choice.");
                }
            }
    }

    //***CASE 1***
    //load xml from user
    private void loadXml() {
        System.out.print("Enter full xml file path: ");
        String filePathString = scanner.nextLine();
        // Try load xml file and validate it, print DTO result.
        System.out.println(engine.loadXML(filePathString));
    }

    //***CASE 2***
    // Get all loans information and status in BankSystem.
    private void getAllLoansInformation() {

        List<LoanDataObject> allLoans = this.engine.getAllLoansData();
        if(allLoans.size() == 0)
            System.out.println("There are no loans exist.");
        else {
            System.out.println("All loans information:");
            for (LoanDataObject loanData : allLoans)
                loanData.showLoan();
        }

    }

    //***CASE 3***
    // Get all customer information in BankSystem.
    private void getAllCustomerInformation() {

        List<CustomerDataObject> getCustomersLoansData = this.engine.getAllCustomersLoansAndLogs();
        if(getCustomersLoansData == null)
            System.out.println("There are no customers to report on.");
        else {
            for(CustomerDataObject data : getCustomersLoansData) {
                System.out.println(data.getName() + ":"); // prints customer name.

                //Print each customer logs.
                System.out.println("   Customer operations list: ");
                if(data.getLogCustomer() == null || data.getLogCustomer().size() <= 0)
                    System.out.println("      There are no operations to view.");
                else
                    data.getLogCustomer().stream().forEach(e -> System.out.println(e + "\n"));


                //Print eacg customer investments.
                System.out.println("   Customer Investments list: ");
                if(data.getInvestmentList() == null || data.getInvestmentList().size() <= 0)
                    System.out.println("      There are no investments to view.");
                else {
                    // Trigger relevant printing function for each of the customer investments.
                    for(LoanDataObject loanData : data.getInvestmentList())
                        System.out.println(loanData.getLoanDetails() + "\n");

                }

                //Print eacg customer loans taken.
                System.out.println("   Customer taken loans list: ");
                if(data.getLoanList() == null || data.getLoanList().size() <= 0)
                    System.out.println("      There are no taken loans to view.\n");
                else {
                    // Trigger relevant printing function for each of the customer investments.
                    for(LoanDataObject loanData : data.getLoanList())
                        System.out.println(loanData.getLoanDetails());
                }

            }
        }
    }

    //***CASE 4***
    //Deposit money to a specific user.
    private void depositMoneyToCustomer() {

        this.printCustomersNames(false); // printing all customers names.
        List<String> customersNames = this.engine.getAllCustomersNames();
        if(customersNames != null && customersNames.size() > 0) {
            String userName = this.readUserNameAndValidateFromList();

            // Wait for the user to insert a valid amount of money.
            int depositeAmount = -1;
            do {
                System.out.println("Enter amount to deposit: ");
                if (scanner.hasNextInt())
                    depositeAmount = scanner.nextInt();

                this.cleanBuffer();

                if (depositeAmount <= 0)
                    System.out.println("Error: Please enter a valid amount to deposit.");

            } while (depositeAmount <= 0);

            this.engine.depositeMoney(userName, depositeAmount);
            System.out.println("Bank: " + userName + " deposit made successfully.");
        }
    }

    //***CASE 5***
    //Withrawal money to a specific user.
    private void withrawalMoneyToCustomer() {

        this.printCustomersNames(false); // printing all customers names.
        List<String> customersNames = this.engine.getAllCustomersNames();
        if(customersNames != null && customersNames.size() > 0) {
            String userName = this.readUserNameAndValidateFromList();

            // Wait for the user to insert a valid amount of money.
            int withdrawAmount = -1;
            do {
                System.out.println("Enter amount to withdraw: ");

                if (scanner.hasNextInt())
                    withdrawAmount = scanner.nextInt();

                this.cleanBuffer();

                if (withdrawAmount <= 0)
                    System.out.println("Error: Withdraw amount must be greater then 0.");

            } while (withdrawAmount <= 0);

            try {
                this.engine.withdrawMoney(userName, withdrawAmount);
                System.out.println("Bank: " + userName + " withrawal made successfully.");
            } catch (DataTransferObject e) {
                System.out.println(e);
            }
        }

    }

    // ***CASE 6***
    // Print customers names list and choose where to invest.
    private void startInvestmentProccess() {

        this.printCustomersNames(true);
        String chosenCustomer = this.readUserNameAndValidateFromList();
        if(chosenCustomer.equals(""))
            System.out.println("Error: there are no customers in the system.");
        else {
            // Check chosen custoemr balance.
            if(this.engine.getBalanceOfCustomerByName(chosenCustomer) == 0) {
                System.out.println("Error: Cant choose customer with no money in balance.");
                return;
            }

            // Read amount to invest from customer.
            int amountToInvest = this.readUserInvestmentAmount(chosenCustomer);
            if(amountToInvest == 0) {
                System.out.println("Error: Cant invest 0 money, please try again later.");
                return;
            }

            // Proccess of letting customer choose to which category he would like to invest.
            int categoriesCounter = 0;

            // Check if categories are exist before letting customer to invest.
            if(this.engine.getBankCategories() == null || this.engine.getBankCategories().size() <= 0)
                System.out.println("Error: there are no loans to invest in.");
            else {
                int catCounter = 0;

                // print all categories names with their ids in list.
                List<String> categoriesNames = new ArrayList<>(this.engine.getBankCategories());
                for(String catName : categoriesNames) {
                    catCounter++;
                    System.out.println(catCounter + ". " + catName + ".");
                }

                System.out.println("0. All categories.");

                // Reads proccess required arguments.
                List<String> catToSend = this.readUserCategoryChoice(categoriesNames);
                int interest = this.readUserRelevantInterest();
                int totalTime = this.readUserTotalTimeOfInvestment();

                List<LoanDataObject> pendingLoans
                        = this.engine.getRelevantPendingLoansList(chosenCustomer, catToSend, interest, totalTime);

                if(pendingLoans.size() <= 0 || pendingLoans == null)
                    System.out.println("   - There are no loans that meets your requirements.");
                else {
                    System.out.println("Loans to invest:");
                    for (LoanDataObject loanData : pendingLoans)
                        System.out.println((pendingLoans.indexOf(loanData) + 1) + ". " + loanData.getLoanDetails());

                    // Reading customer loans ids to invest.
                    List<Triple<String,Integer,String>> loansToInvest = null;
                    do {
                        System.out.println("Enter loans id to invest (Ex. 1,4,7...): ");
                        String customerChoiceToInvest = scanner.nextLine(); // Get loans id from customer.

                        loansToInvest = this.makeListOfLoansToInvest(pendingLoans, customerChoiceToInvest);
                    } while(loansToInvest == null);

                    try {
                        System.out.println(this.engine.makeInvestments(chosenCustomer, amountToInvest, loansToInvest));
                    }catch(DataTransferObject e) {
                        System.out.println(e);
                    }
                }

            }
        }
    }

    //***CASE 7***
    // Increase YAZ date in 1.
    private void increaseYazDate() {
        this.engine.increaseYazDate();
        System.out.println("Current YAZ: " + BankSystem.getCurrentYaz() + ", previouse YAZ: " + (BankSystem.getCurrentYaz() - 1));
    }


    // Gets string of loan ids to invest (Ex 1,2,3 ...) and return list of loans names.
    private List<Triple<String,Integer,String>> makeListOfLoansToInvest(List<LoanDataObject> pendingLoans, String customerChoiceToInvest) {

        if(customerChoiceToInvest.isEmpty())
            return null;

        List<Triple<String,Integer,String>> nameOfLoansToInvest = new ArrayList<>();
        String[] loansNames = customerChoiceToInvest.split(",");
        for(String loanName : loansNames) {
            try {
                int choice = Integer.parseInt(loanName);
                // create new list -> for each loan get owner name and owner id.
                nameOfLoansToInvest.add(new Triple<>(pendingLoans.get(choice -1).getLoanOwnerName(), pendingLoans.get(choice-1).getLoanOpeningTime(), pendingLoans.get(choice-1).getLoanName()));
            } catch(NumberFormatException | IndexOutOfBoundsException e) { // in case string sent wasnt valid.
                System.out.println("Error: invalid list of loans to invest.");
                return null;
            }
        }

        return nameOfLoansToInvest;
    }

    // Reads customer's total time to receive his money back from relevant loans.
    private int readUserTotalTimeOfInvestment() {
        int totalTime = -1;
        do {
            System.out.println("Enter loan's duration (type 0 for no-limit): ");
            if(!scanner.hasNextInt())
                System.out.println("Error: Invalid argurment was given.");
            else {
                int amountHolder = scanner.nextInt();
                this.cleanBuffer();

                if(amountHolder >= 0)
                    totalTime = amountHolder;
                else
                    System.out.println("Error: Invalid interest amount (required integer greater then 0).");
            }

        } while(totalTime < 0);

        return totalTime;
    }

    // Reads customer's interest to gain from loan.
    private int readUserRelevantInterest() {
        int loansInterest = -1;
        do {
            System.out.println("Enter loan's interest (type 0 for no-limit): ");
            if(!scanner.hasNextInt())
                System.out.println("Error: Invalid argurment was given.");
            else {
                int amountHolder = scanner.nextInt();
                if(amountHolder >= 0)
                    loansInterest = amountHolder;
                else
                    System.out.println("Error: Invalid interest amount (required integer greater then 0).");
            }

        } while(loansInterest < 0);

        return loansInterest;
    }

    // Reads customer's category choice.
    private List<String> readUserCategoryChoice(List<String> catNames) {

        boolean choiseIsValid;
        int optionsLength = catNames.size() + 1;
        List<String> result = new ArrayList<>();
        do {
            choiseIsValid = true;
            result.clear(); // clear prev choices.
            System.out.println("Choose categories id (1,2,3,...): ");

            // Read user input and do validation.
            String lineRead = scanner.nextLine();
            if(lineRead == null || lineRead.equals(""))
            {
                choiseIsValid = false;
                continue;
            }

            String[] userChoice = lineRead.split(",");

            // Go over user multiple choice and valid them.
            for(String choice : userChoice) {
                try {
                    int curIntChoice = Integer.parseInt(choice);

                    // Check chosen id.
                    if(curIntChoice < 0 || curIntChoice > optionsLength) {
                        System.out.println("Error: invalid categories choice.");
                        choiseIsValid = false;
                        break;
                    }

                    // if user chose All categories as a option, return just all categories.
                    if(curIntChoice != 0)
                        result.add(catNames.get(curIntChoice-1)); // Adding cat name to list.
                    else {
                        // If user chose all so return just all.
                        result.clear();
                        result.add("All");
                        return result;
                    }

                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    System.out.println("Error: invalid categories choice.");
                    choiseIsValid = false;
                    break;
                }
            }

        }while(!choiseIsValid);

        return result;
    }

    // Reading investment amount of money and validate customer have enough money.
    private int readUserInvestmentAmount(String chosenCustomer) {

        int amount = -1;
        do {
            System.out.println("Enter amount to invest: ");
            if(!scanner.hasNextInt())
                System.out.println("Error: Investment money must be an integer.");
            else {
                int amountHolder = scanner.nextInt();
                this.cleanBuffer();

                if (amountHolder < 0)
                    System.out.println("Error: Investment money must be a positive number.");
                else if (amountHolder > this.engine.getBalanceOfCustomerByName(chosenCustomer))
                    System.out.println("Error: Investment money is greater then customer balance.");
                else
                    amount = amountHolder;
            }

        } while(amount < 0 || amount > this.engine.getBalanceOfCustomerByName(chosenCustomer));

        return amount;
    }

    // Get name that exist in customers names list.
    private String readUserNameAndValidateFromList() {
        // Wait for the user to insert an existing name.
        List<String> customersNames = this.engine.getAllCustomersNames();
        String userName = "";

        if(customersNames != null && customersNames.size() > 0) {
            do {
                System.out.println("Enter a name from the list: ");
                userName = scanner.nextLine();

                if (customersNames.indexOf(userName) == -1)
                    System.out.println("Error: There is no user with that given name exist.");

            } while (customersNames.indexOf(userName) == -1);
        }
        return userName;
    }

    // getUserChoice -> Check if next input is an int, if so return it else return 0 (non-valid choice).
    private int getUserChoice() {
        if(scanner.hasNextInt())
            return scanner.nextInt();

        return 0;
    }

    // presentMenu -> Print menu to screen.
    private void presentMenu() {
        if(BankSystem.getCurrentYaz() != 0)
            System.out.println("Current Time Unit: " + BankSystem.getCurrentYaz() + ".");

        System.out.println("1. Load XML file.");
        System.out.println("2. Get available information about loans and status.");
        System.out.println("3. Present members information.");
        System.out.println("4. Deposit amount of money to member's account.");
        System.out.println("5. Withdraw amount of money from member's account.");
        System.out.println("6. Start investing.");
        System.out.println("7. Time unit management.");
        System.out.println("8. Exit.");
    }

    // Prints all customers names.
    private void printCustomersNames(boolean printBalance) {
        List<String> customersNames = this.engine.getAllCustomersNames();
        if(customersNames == null) {
            System.out.println("Error: There are no customers exist.");
        } else {
            System.out.println("List of users names: ");
            customersNames.stream().forEach(customerName -> {
                String toPrint = customerName;

                //if true print customer balance.
                if(printBalance)
                    toPrint += " - balance: " + this.engine.getBalanceOfCustomerByName(customerName);

                System.out.println(toPrint);

            });
        }
    }

    // cleanBuffer -> Read next line to clean buffer from un relevant data left.
    private void cleanBuffer() {
        scanner.nextLine();
    }
}
