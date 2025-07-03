package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.LinkedList;
import java.util.Queue;

/**
 * DBController manages a pool of database connections using the Singleton
 * pattern. Supports thread-safe take and retrive.
 */
public class DBController {


  

	/** Singleton instance of DBController */
	private static DBController instance = null;
	/** Queue for connection pool */
	private final Queue<Connection> connectionPool = new LinkedList<>();
	/** Size of the connection pool */
	private final int POOL_SIZE = 6;
	/** if the DB connection initialization succeeded (1 = success, 0 = failure) */
	private final int successFlag;

	/**
	 * Private constructor. Establishes a connection to the database.
	 *
	 * @param dbName   Name of the database
	 * @param password Password for the "root" user
	 */
	private DBController(String dbName, String password) {
		int flag = 0;

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			String url = "jdbc:mysql://localhost/" + dbName + "?serverTimezone=Asia/Jerusalem";

			for (int i = 0; i < POOL_SIZE; i++) {
				Connection conn = DriverManager.getConnection(url, "root", password);
				connectionPool.add(conn);
			}

			System.out.println("Database connection established.");
			System.out.println("Initialized DB connection pool with " + connectionPool.size() + " connections.");

			flag = 1;
		} catch (Exception e) {
			System.err.println("Failed to connect to database: " + e.getMessage());
		}

		this.successFlag = flag;
	}

	/**
	 * Initializes the singleton instance of DBController. This method must be
	 * called once before using getInstance().
	 *
	 * @param dbName   the name of the database
	 * @param password password for the database
	 */
	public static synchronized void initializeConnection(String dbName, String password) {
		if (instance == null) {
			instance = new DBController(dbName, password);
		}
	}

	/**
	 * Returns the singleton instance of DBController.
	 *
	 * @return the DBController instance
	 * @throws IllegalStateException if initializeConnection() was not called first
	 */
	public static DBController getInstance() {
		if (instance == null) {
			throw new IllegalStateException("DBController not initialized. Call initializeConnection() first.");
		}
		return instance;
	}

	/**
	 * Retrieves an available connection from the pool. Waits up to 30 seconds if no
	 * connections are currently available.
	 *
	 * @return a Connection from the pool
	 * @throws RuntimeException if no connection becomes available within 30 seconds
	 */
	public synchronized Connection getConnection() {
		long startTime = System.currentTimeMillis();
		int waitCounter = 0;
		while (connectionPool.isEmpty()) {
		    if (System.currentTimeMillis() - startTime > 5000) {
		        throw new RuntimeException("Timeout: No available DB connections.");
		    }
		    if (waitCounter % 10 == 0) {
		        System.out.println("Waiting for available DB connection...");
		    }
		    try {
		        wait(100);
		    } catch (InterruptedException e) {
		        Thread.currentThread().interrupt();
		    }
		    waitCounter++;
		}

		Connection conn = connectionPool.remove();
		System.out.println("Connection taken. Remaining in pool: " + connectionPool.size());
		return conn;
	}

	/**
	 * Returns a used connection back to the pool and notifies waiting threads. so
	 * they can try again to use
	 * 
	 * @param conn the Connection to release
	 */
	public synchronized void releaseConnection(Connection conn) {
		if (conn != null) {
			connectionPool.add(conn);
			System.out.println("Connection returned. Now available in pool: " + connectionPool.size());

			notifyAll(); // wake all the "waiting threads".
		}
	}

	/**
	 * Returns a flag indicating success (1) or failure (0) of initial database
	 * connection setup.
	 *
	 * @return 1 if successful, otherwise 0
	 */
	public int getSuccessFlag() {
		return successFlag;
	}

}


