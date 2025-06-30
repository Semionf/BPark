package server;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * DBController manages a single connection instance to the database (Singleton pattern).
 */
public class DBController {

    private static DBController instance = null;
    private final Connection connection;
    private final int successFlag;

    /**
     * Private constructor. Establishes a connection to the database.
     *
     * @param dbName   Name of the database
     * @param password Password for the "root" user
     */
    private DBController(String dbName, String password) {
        Connection tempConnection = null;
        int flag = 0;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost/" + dbName + "?serverTimezone=Asia/Jerusalem";
            tempConnection = DriverManager.getConnection(url, "root", password);
            System.out.println("✅ Database connection established.");
            flag = 1;
        } catch (Exception e) {
            System.err.println("❌ Failed to connect to database: " + e.getMessage());
        }

        this.connection = tempConnection;
        this.successFlag = flag;
    }

    public static synchronized void initializeConnection(String dbName, String password) {
        if (instance == null) {
            instance = new DBController(dbName, password);
        }
    }

    public static DBController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DBController not initialized. Call initializeConnection() first.");
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public int getSuccessFlag() {
        return successFlag;
    }
    
}