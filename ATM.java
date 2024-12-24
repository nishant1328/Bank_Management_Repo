import java.util.Scanner;

public class ATM {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BankingService bankingService = new BankingService();
        
        System.out.println("Welcome to ATM system!");
        
        // Sample ATM operations
        while (true) {
            System.out.println("\nATM Menu:");
            System.out.println("1. Check Balance");
            System.out.println("2. Withdraw");
            System.out.println("3. Deposit");
            System.out.println("4. Transfer");
            System.out.println("5. Exit");
            System.out.print("Select option: ");
            int option = scanner.nextInt();

            if (option == 5) {
                System.out.println("Exiting ATM system...");
                break;
            }

            System.out.print("Enter account number: ");
            String accountNumber = scanner.next();

            try {
                switch (option) {
                    case 1:
                        System.out.println("Balance: " + bankingService.checkBalance(accountNumber));
                        break;

                    case 2:
                        System.out.print("Enter amount to withdraw: ");
                        double withdrawAmount = scanner.nextDouble();
                        if (bankingService.withdraw(accountNumber, withdrawAmount)) {
                            System.out.println("Withdrawal successful!");
                        } else {
                            System.out.println("Insufficient funds.");
                        }
                        break;

                    case 3:
                        System.out.print("Enter amount to deposit: ");
                        double depositAmount = scanner.nextDouble();
                        if (bankingService.deposit(accountNumber, depositAmount)) {
                            System.out.println("Deposit successful!");
                        }
                        break;

                    case 4:
                        System.out.print("Enter recipient account number: ");
                        String toAccount = scanner.next();
                        System.out.print("Enter amount to transfer: ");
                        double transferAmount = scanner.nextDouble();
                        if (bankingService.transfer(accountNumber, toAccount, transferAmount)) {
                            System.out.println("Transfer successful!");
                        } else {
                            System.out.println("Transfer failed. Insufficient funds.");
                        }
                        break;

                    default:
                        System.out.println("Invalid option.");
                        break;
                }
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        
        scanner.close();
    }
}

