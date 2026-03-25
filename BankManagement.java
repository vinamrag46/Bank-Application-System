import java.util.*;
import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class Account {
    protected String AccountNumber;
    protected String name;
    protected String password;
    protected String AccountType;
    protected double balance;

    public Account(String AccountNumber, String name, String password, String AccountType, double balance) {
        this.AccountNumber = AccountNumber;
        this.name = name;
        this.password = password;
        this.AccountType = AccountType;
        this.balance = balance;
    }

    public String getAccountNumber() {
        return AccountNumber;
    }

    public boolean Authenticate(String enteredPassword) {
        return this.password.equals(enteredPassword);
    }

    public void deposit(double amount) {
        balance += amount;
        BankManagement.updateAccountBalance(this);
        BankManagement.saveTransaction(this.AccountNumber, "Deposit", amount);
        JOptionPane.showMessageDialog(null, "Rs " + amount + " deposited successfully!");
    }

    public boolean withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
            BankManagement.updateAccountBalance(this);
            BankManagement.saveTransaction(this.AccountNumber, "Withdraw", amount);
            JOptionPane.showMessageDialog(null, "Rs " + amount + " withdrawn successfully!");
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "Insufficient balance!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void displayBalance() {
        JOptionPane.showMessageDialog(null, "Current balance is Rs: " + balance);
    }

    public void viewTransaction() {
        BankManagement.viewTransactions(AccountNumber);
    }
}

class SavingsAccount extends Account {
    private static final double InterestRate = 0.05;

    public SavingsAccount(String accountNumber, String name, String password, double balance) {
        super(accountNumber, name, password, "Savings", balance);
    }

    @Override
    public boolean withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
            BankManagement.updateAccountBalance(this);
            BankManagement.saveTransaction(this.AccountNumber, "Withdraw", amount);
            JOptionPane.showMessageDialog(null, "Rs " + amount + " withdrawn successfully!");
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "Insufficient balance!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }


    public void applyInterest() {
        double interest = InterestRate * balance;
        deposit(interest);  // Will trigger JOptionPane itself
        JOptionPane.showMessageDialog(null, "Annual interest Rs: " + interest + " applied.");
    }

}

class CurrentAccount extends Account {
    public CurrentAccount(String accountNumber, String name, String password, double balance) {
        super(accountNumber, name, password, "Current", balance);
    }

    public void applyForCheckbook() {
        JOptionPane.showMessageDialog(null, "Checkbook request submitted for account: " + getAccountNumber());
    }

}

public class BankManagement {
    private static Scanner sc = new Scanner(System.in);
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bankdb";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "changeme";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static void main(String[] args) {
       /* System.out.println("\n\t\tWELCOME TO BANKING MANAGEMENT SYSTEM");
        while (true) {
            System.out.println("\n1. Sign Up \n2. Sign In\n3. Exit");
            int choice = sc.nextInt();
            sc.nextLine();
            switch (choice) {
                case 1:
                    SignUp();
                    break;
                case 2:
                    SignIn();
                    break;
                case 3:
                    System.out.println("Exiting... Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice");
            }
        }*/

        JFrame frame = new JFrame("Online Bank");
        JButton b1 = new JButton("SignUp");
        b1.setBounds(130, 100, 150, 50);
        JButton b2 = new JButton("SignIn");
        b2.setBounds(130, 190, 150, 50);
        JButton b3 = new JButton("Exit");
        b3.setBounds(130, 280, 150, 50);

        b1.addActionListener(e -> BankManagement.SignUp());
        b2.addActionListener(e -> BankManagement.SignIn());
        b3.addActionListener(e -> System.exit(0));

        frame.add(b1);
        frame.add(b2);
        frame.add(b3);
        frame.setSize(400, 400);
        frame.setLayout(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    }

    private static void SignUp() {
        JFrame f1 = new JFrame("SignUp Page");
        JLabel l1 = new JLabel("WELCOME To SignUp Page!!");
        l1.setBounds(100, 50, 400, 20);
        JLabel l2 = new JLabel("Name");
        l2.setBounds(50, 100, 100, 50);
        JTextField nameField = new JTextField();
        nameField.setBounds(160, 110, 150, 30);

        JLabel l3 = new JLabel("Generate Password");
        l3.setBounds(50, 150, 200, 50);
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(200, 160, 150, 30);

        JLabel l4 = new JLabel("Account Type");
        l4.setBounds(50, 200, 100, 50);
        JCheckBox savingsBox = new JCheckBox("Savings");
        savingsBox.setBounds(160, 210, 100, 30);
        JCheckBox currentBox = new JCheckBox("Current");
        currentBox.setBounds(260, 210, 100, 30);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(290, 300, 80, 30);

        submitButton.addActionListener(e -> {
            String name = nameField.getText();
            String password = new String(passwordField.getPassword());
            String accountType = "";

            if (savingsBox.isSelected()) {
                accountType += "Savings ";
            }
            if (currentBox.isSelected()) {
                accountType += "Current";
            }

            if (name.isEmpty() || password.isEmpty() || accountType.isEmpty()) {
                JOptionPane.showMessageDialog(f1, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                String accountNumber = UUID.randomUUID().toString().substring(0, 10);
                try (Connection conn = getConnection()) {
                    String sql = "INSERT INTO accounts (account_number, name, password, account_type, balance) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, accountNumber);
                    ps.setString(2, name);
                    ps.setString(3, password);
                    ps.setString(4, accountType);
                    ps.setDouble(5, 0.0);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(f1, "Account Created! Your Account Number: " + accountNumber, "Success", JOptionPane.INFORMATION_MESSAGE);
                    f1.dispose();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(f1, "Error creating account: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        f1.add(l1);
        f1.add(l2);
        f1.add(l3);
        f1.add(l4);
        f1.add(nameField);
        f1.add(passwordField);
        f1.add(savingsBox);
        f1.add(currentBox);
        f1.add(submitButton);
        f1.setSize(400, 400);
        f1.setLayout(null);
        f1.setVisible(true);
        f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    private static void SignIn() {
        JFrame f1 = new JFrame("SignIn Page");
        JLabel l1 = new JLabel("WELCOME To SignIn Page!!");
        l1.setBounds(100, 50, 400, 20);
        JLabel l2 = new JLabel("Account Number");
        l2.setBounds(50, 100, 200, 50);
        JTextField accountField = new JTextField();
        accountField.setBounds(160, 110, 150, 30);

        JLabel l3 = new JLabel("Password");
        l3.setBounds(50, 150, 100, 50);
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(160, 160, 150, 30);

        JButton submitButton = new JButton("Submit");
        submitButton.setBounds(290, 300, 80, 30);

        submitButton.addActionListener(e -> {
            String accountNumber = accountField.getText();
            String password = new String(passwordField.getPassword());

            if (accountNumber.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(f1, "Please enter both account number and password.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                try (Connection conn = getConnection()) {
                    String sql = "SELECT * FROM accounts WHERE account_number = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, accountNumber);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        String name = rs.getString("name");
                        String storedPassword = rs.getString("password");
                        String accountType = rs.getString("account_type");
                        double balance = rs.getDouble("balance");

                        if (storedPassword.equals(password)) {
                            Account account = accountType.equalsIgnoreCase("Savings") ?
                                    new SavingsAccount(accountNumber, name, password, balance) :
                                    new CurrentAccount(accountNumber, name, password, balance);
                            JOptionPane.showMessageDialog(f1, "Login successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                            f1.dispose();
                            accountMenu(account);
                            return;
                        } else {
                            JOptionPane.showMessageDialog(f1, "Invalid password", "Login Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(f1, "Invalid account number", "Login Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(f1, "Error signing in: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        f1.add(l1);
        f1.add(l2);
        f1.add(l3);
        f1.add(accountField);
        f1.add(passwordField);
        f1.add(submitButton);
        f1.setSize(400, 400);
        f1.setLayout(null);
        f1.setVisible(true);
        f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void accountMenu(Account account) {
       /* while (true) {
            System.out.println("\n1. View Balance\n2. Deposit\n3. Withdraw\n4. View Transactions\n5. Transfer Funds\n6. Apply Interest (Savings Only)\n7. Apply for CheckBook (Current Only)\n8. Logout");
            int choice = sc.nextInt();
            switch (choice) {
                case 1:
                    account.displayBalance();
                    break;
                case 2:
                    System.out.print("Enter amount to deposit: ");
                    double depositAmount = sc.nextDouble();
                    account.deposit(depositAmount);
                    break;
                case 3:
                    System.out.print("Enter amount to withdraw: ");
                    double withdrawAmount = sc.nextDouble();
                    account.withdraw(withdrawAmount);
                    break;
                case 4:
                    account.viewTransaction();
                    break;
                case 5:
                    transferFund(account);
                    break;
                case 6:
                    if (account instanceof SavingsAccount) {
                        ((SavingsAccount) account).applyInterest();
                    } else {
                        System.out.println("Interest can only be applied to Savings Accounts.");
                    }
                    break;
                case 7:
                    if (account instanceof CurrentAccount) {
                        ((CurrentAccount) account).applyForCheckbook();
                    } else {
                        System.out.println("Cannot issue checkbook");
                    }
                    break;
                case 8:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }*/
        JFrame f2 = new JFrame("Account Menu");
        JLabel welcomeLabel = new JLabel("Welcome, User!");
        welcomeLabel.setBounds(200, 50, 200, 30);


        JButton viewBalanceButton = new JButton("View Balance");
        viewBalanceButton.setBounds(150, 100, 200, 30);
        JButton depositButton = new JButton("Deposit");
        depositButton.setBounds(150, 145, 200, 30);
        JButton withdrawButton = new JButton("Withdraw");
        withdrawButton.setBounds(150, 190, 200, 30);
        JButton viewTransactionsButton = new JButton("View Transactions");
        viewTransactionsButton.setBounds(150, 235, 200, 30);
        JButton transferFundsButton = new JButton("Transfer Funds");
        transferFundsButton.setBounds(150, 280, 200, 30);
        JButton applyInterestButton = new JButton("Apply Interest (Savings Only)");
        applyInterestButton.setBounds(150, 325, 200, 30);
        JButton applyForCheckbookButton = new JButton("Apply for Checkbook (Current Only)");
        applyForCheckbookButton.setBounds(150, 370, 200, 30);
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBounds(150, 415, 200, 30);

        viewBalanceButton.addActionListener(e -> account.displayBalance());
        depositButton.addActionListener(e -> {

            String input = JOptionPane.showInputDialog(f2, "Enter amount to deposit:");
            if (input != null && !input.isEmpty()) {
                try {
                    double amount = Double.parseDouble(input);
                    account.deposit(amount);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(f2, "Invalid amount!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        });
        withdrawButton.addActionListener(e -> {

                String input = JOptionPane.showInputDialog(f2, "Enter amount to withdraw:");
                if (input != null && !input.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(input);
                        account.withdraw(amount);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(f2, "Invalid amount!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
        });
        viewTransactionsButton.addActionListener(e -> account.viewTransaction());
        transferFundsButton.addActionListener(e -> transferFund(account));
        applyInterestButton.addActionListener(e -> {
            if (account instanceof SavingsAccount) {
                ((SavingsAccount) account).applyInterest();
            } else {
                JOptionPane.showMessageDialog(f2, "Interest is only for Savings accounts.");
            }
        });
        applyForCheckbookButton.addActionListener(e -> {
            if (account instanceof CurrentAccount) {
                ((CurrentAccount) account).applyForCheckbook();
            } else {
                JOptionPane.showMessageDialog(f2, "Checkbook only for Current accounts.");
            }
        });
        logoutButton.addActionListener(e -> {
            f2.dispose();
            SignIn();
        });

        f2.add(welcomeLabel);
        f2.add(viewBalanceButton);
        f2.add(depositButton);
        f2.add(withdrawButton);
        f2.add(viewTransactionsButton);
        f2.add(transferFundsButton);
        f2.add(applyInterestButton);
        f2.add(applyForCheckbookButton);
        f2.add(logoutButton);

        f2.setSize(500, 500);
        f2.setLayout(null);
        f2.setVisible(true);
        f2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);




    }

    public static void updateAccountBalance(Account account) {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, account.balance);
            ps.setString(2, account.getAccountNumber());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating balance: " + e.getMessage());
        }
    }

    public static void saveTransaction(String accountNumber, String type, double amount) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO transactions (account_number, type, amount) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, accountNumber);
            ps.setString(2, type);
            ps.setDouble(3, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving transaction: " + e.getMessage());
        }
    }

    public static void viewTransactions(String accountNumber) {
        try (Connection conn = getConnection()) {
            StringBuilder sb = new StringBuilder("Transaction History:\n\n");
            String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY timestamp DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sb.append(rs.getString("timestamp"))
                        .append(" - ")
                        .append(rs.getString("type"))
                        .append(" - Rs: ")
                        .append(rs.getDouble("amount"))
                        .append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading transactions: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void transferFund(Account account) {
        Scanner st = new Scanner(System.in);
        System.out.print("Enter the receiver account number: ");
        String receiverAccNo = st.nextLine();
        Account recipient = null;

        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM accounts WHERE account_number = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, receiverAccNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String password = rs.getString("password");
                String accountType = rs.getString("account_type");
                double balance = rs.getDouble("balance");
                recipient = accountType.equalsIgnoreCase("Savings") ?
                        new SavingsAccount(receiverAccNo, name, password, balance) :
                        new CurrentAccount(receiverAccNo, name, password, balance);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching recipient: " + e.getMessage());
            return;
        }

        if (recipient == null) {
            System.out.println("Receiver account not found");
            return;
        }

        System.out.print("Enter amount to transfer: ");
        double amount = st.nextDouble();
        if (account.withdraw(amount)) {
            recipient.deposit(amount);
            updateAccountBalance(recipient);
            System.out.println("Transfer successful");
        }
    }
}
