import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class BankingService {

    // Check Account Balance
    public double checkBalance(String accountNumber) throws SQLException {
        String query = "SELECT balance FROM accounts WHERE account_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            } else {
                throw new SQLException("Account not found.");
            }
        }
    }

    // Withdraw Money
    public boolean withdraw(String accountNumber, double amount) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Check balance first
            double balance = checkBalance(accountNumber);
            if (balance < amount) {
                return false; // Insufficient funds
            }

            // Update balance
            String updateBalance = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
            stmt = conn.prepareStatement(updateBalance);
            stmt.setDouble(1, amount);
            stmt.setString(2, accountNumber);
            int rowsAffected = stmt.executeUpdate();

            // Log transaction
            if (rowsAffected > 0) {
                String insertTransaction = "INSERT INTO transactions (from_account, amount) VALUES (?, ?)";
                PreparedStatement transactionStmt = conn.prepareStatement(insertTransaction);
                transactionStmt.setString(1, accountNumber);
                transactionStmt.setDouble(2, amount);
                transactionStmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }

    // Deposit Money
    public boolean deposit(String accountNumber, double amount) throws SQLException {
        String updateBalance = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateBalance)) {
            stmt.setDouble(1, amount);
            stmt.setString(2, accountNumber);
            int rowsAffected = stmt.executeUpdate();

            // Log transaction
            if (rowsAffected > 0) {
                String insertTransaction = "INSERT INTO transactions (from_account, to_account, amount) VALUES (?, ?, ?)";
                try (PreparedStatement transactionStmt = conn.prepareStatement(insertTransaction)) {
                    transactionStmt.setString(1, accountNumber);
                    transactionStmt.setString(2, accountNumber);  // same account for deposit
                    transactionStmt.setDouble(3, amount);
                    transactionStmt.executeUpdate();
                }
            }
            return rowsAffected > 0;
        }
    }

    // Transfer Money between accounts
    public boolean transfer(String fromAccount, String toAccount, double amount) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Check balances of both accounts
            double fromBalance = checkBalance(fromAccount);
            double toBalance = checkBalance(toAccount);
            if (fromBalance < amount) {
                return false; // Insufficient funds
            }

            // Deduct from sender's account and add to recipient's account
            String updateSender = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
            stmt = conn.prepareStatement(updateSender);
            stmt.setDouble(1, amount);
            stmt.setString(2, fromAccount);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Update recipient account
                String updateReceiver = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
                PreparedStatement stmtReceiver = conn.prepareStatement(updateReceiver);
                stmtReceiver.setDouble(1, amount);
                stmtReceiver.setString(2, toAccount);
                stmtReceiver.executeUpdate();

                // Log the transaction
                String insertTransaction = "INSERT INTO transactions (from_account, to_account, amount) VALUES (?, ?, ?)";
                PreparedStatement transactionStmt = conn.prepareStatement(insertTransaction);
                transactionStmt.setString(1, fromAccount);
                transactionStmt.setString(2, toAccount);
                transactionStmt.setDouble(3, amount);
                transactionStmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
}

