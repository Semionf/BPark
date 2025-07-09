package client;

import java.util.ArrayList;

import common.Message;
import common.ParkingOrder;
import controllers.AttendantController;
import controllers.ExtendParkingController;
import controllers.ManagerController;
import controllers.ParkingHistoryController;
import controllers.SubscriberController;
import controllers.UpdateProfileController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Utility class to show Client app scenes (converted from BParkClientApp)
 */
public class BParkClientScenes {
	/**
	 * Default constructor for BParkClientScenes. Required for JavaFX framework
	 * initialization or utility access.
	 */
	public BParkClientScenes() {
		// Default constructor
	}

	/**
	 * The currently logged-in user's username.
	 */
	private static String currentUser;

	/**
	 * A static field that holds the ID of the currently active user. Used to
	 * identify which user is currently logged into the system.
	 */
	private static int currentUserID;

	/**
	 * The type of the currently logged-in user ("sub", "emp", "mng").
	 */
	private static String userType; // "sub", "emp", "mng"

	/**
	 * Controller instance for the parking attendant screen.
	 */
	private static AttendantController attendantController;

	/**
	 * Controller instance for the manager screen.
	 */
	private static ManagerController managerController;

	/**
	 * Displays the login screen on the given stage.
	 *
	 * @param stage The primary stage of the application.
	 * @throws Exception if the FXML file cannot be loaded.
	 */
	public static void showLoginScreen(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(BParkClientScenes.class.getResource("/client/Login.fxml"));
		Parent root = loader.load();

		Scene scene = new Scene(root);
		scene.getStylesheets().add(BParkClientScenes.class.getResource("/css/BParkStyle.css").toExternalForm());
		stage.setTitle("BPark - Login");
		stage.setScene(scene);
		stage.setResizable(false);
		stage.show();
	}

	/**
	 * Switches to the main screen based on the given user type.
	 *
	 * @param userType The type of user ("sub", "emp", or "mng").
	 */
	public static void switchToMainScreen(String userType) {
		try {
			Stage stage = BParkLauncherApp.getPrimaryStage();
			Parent root = null;

			switch (userType) {
			case "sub":
				FXMLLoader subLoader = new FXMLLoader(
						BParkClientScenes.class.getResource("/client/SubscriberMain.fxml"));
				root = subLoader.load();
				SubscriberController controller = subLoader.getController();

				// Move loadHomeView AFTER controller is fully loaded
				Platform.runLater(controller::loadHomeView);

				// Set the user name in the bottom label
				controller.setUserName(getCurrentUser());

				stage.setTitle("BPark - Subscriber Portal");
				break;

			case "emp":
				FXMLLoader empLoader = new FXMLLoader(
						BParkClientScenes.class.getResource("/client/AttendantMain.fxml"));
				root = empLoader.load();
				AttendantController attendantController = empLoader.getController();
				attendantController.setUserName(getCurrentUser());
				stage.setTitle("BPark - Attendant Portal");
				break;

			case "mng":
				FXMLLoader mngLoader = new FXMLLoader(BParkClientScenes.class.getResource("/client/ManagerMain.fxml"));
				root = mngLoader.load();
				stage.setTitle("BPark - Manager Portal");
				break;
			}

			if (root != null) {
				Scene scene = new Scene(root);
				stage.setScene(scene);
				stage.show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a Message object to the server.
	 *
	 * @param msg The message to send.
	 */
	public static void sendMessage(Message msg) {
		BParkLauncherApp.sendMessage(msg);
	}

	/**
	 * Sends a plain string message to the server.
	 *
	 * @param msg The string message to send.
	 */
	public static void sendStringMessage(String msg) {
		BParkLauncherApp.sendStringMessage(msg);
	}

	/**
	 * Checks whether the client is connected to the server.
	 *
	 * @return true if connected, false otherwise.
	 */
	public static boolean isConnected() {
		return BParkLauncherApp.isConnected();
	}

	/**
	 * Attempts to connect to the server if not already connected.
	 */
	public static void connectToServer() {
		if (!isConnected()) {
			BParkLauncherApp.connectToServer();
		}
	}

	/**
	 * Disconnects from the server if connected.
	 */
	public static void disconnect() {
		BParkLauncherApp.disconnect();
	}

	// Getters and setters
	/**
	 * Gets the instance of the UpdateProfileController.
	 *
	 * @return The UpdateProfileController instance.
	 */
	public static UpdateProfileController getUpdateProfileController() {
		return UpdateProfileController.instance;
	}

	/**
	 * Gets the instance of the ExtendParkingController.
	 *
	 * @return The ExtendParkingController instance.
	 */
	public static ExtendParkingController getExtendParkingController() {
		return ExtendParkingController.instance;
	}

	/**
	 * Gets the current username.
	 *
	 * @return The current user's username.
	 */
	public static String getCurrentUser() {
		return currentUser;
	}

	/**
	 * Sets the current user's username.
	 *
	 * @param user The username to set.
	 */
	public static void setCurrentUser(String user) {
		currentUser = user;
	}

	/**
	 * Returns the ID of the currently active user.
	 *
	 * @return the current user's ID
	 */
	public static int getCurrentUserID() {
		return currentUserID;
	}

	/**
	 * Sets the ID of the currently active user.
	 *
	 * @param userID the ID to set as the current user
	 */
	public static void setCurrentUserID(int userID) {
		currentUserID = userID;
	}

	/**
	 * Gets the current user's type.
	 *
	 * @return The user type ("sub", "emp", or "mng").
	 */
	public static String getUserType() {
		return userType;
	}

	/**
	 * Sets the user type.
	 *
	 * @param type The user type to set ("sub", "emp", or "mng").
	 */
	public static void setUserType(String type) {
		userType = type;
	}

	/**
	 * Sets the attendant controller instance.
	 *
	 * @param controller The AttendantController to set.
	 */
	public static void setAttendantController(AttendantController controller) {
		attendantController = controller;
	}

	/**
	 * Gets the attendant controller instance.
	 *
	 * @return The AttendantController instance.
	 */
	public static AttendantController getAttendantController() {
		return attendantController;
	}

	/**
	 * Sets the manager controller instance.
	 *
	 * @param controller The ManagerController to set.
	 */
	public static void setManagerController(ManagerController controller) {
		managerController = controller;
	}

	/**
	 * Gets the manager controller instance.
	 *
	 * @return The ManagerController instance.
	 */
	public static ManagerController getManagerController() {
		return managerController;
	}

	/**
	 * Returns to the login screen and clears current user data.
	 */
	public static void returnToLogin() {
		try {
//			// Send disconnect notification if connected
//			if (BParkLauncherApp.getClient() != null && BParkLauncherApp.getClient().isConnected()) {
//				sendStringMessage("ClientDisconnect");
//			}
//
//			// Close current connection
//			if (BParkLauncherApp.getClient() != null && BParkLauncherApp.getClient().isConnected()) {
//				BParkLauncherApp.getClient().closeConnection();
//			}

			// Get current stage
			Stage currentStage = getCurrentStage();

			// Load login screen
			FXMLLoader loader = new FXMLLoader(BParkClientScenes.class.getResource("/client/Login.fxml"));
			Parent root = loader.load();

			Scene scene = new Scene(root);
			scene.getStylesheets().add(BParkClientScenes.class.getResource("/css/BParkStyle.css").toExternalForm());

			currentStage.setScene(scene);
			currentStage.setTitle("BPark - Login");
			currentStage.setResizable(false);

			// Clear current user data
			currentUser = null;
			currentUserID = 0;
			userType = null;

//			// Reconnect to server for next login
//			connectToServer();

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error returning to login: " + e.getMessage());
		}
	}

	/**
	 * Disconnects the client and exits the application.
	 */
	public static void exitApplication() {
		try {
			// Send disconnect notification if connected
			if (BParkLauncherApp.getClient() != null && BParkLauncherApp.getClient().isConnected()) {
				sendStringMessage("ClientDisconnect");
			}

			// Close connection
			if (BParkLauncherApp.getClient() != null && BParkLauncherApp.getClient().isConnected()) {
				BParkLauncherApp.getClient().closeConnection();
			}

			// Exit application
			Platform.exit();
			System.exit(0);

		} catch (Exception e) {
			e.printStackTrace();
			// Force exit even if there's an error
			System.exit(0);
		}
	}

	/**
	 * Gets the currently active JavaFX stage (window).
	 *
	 * @return The current Stage instance, or a new Stage if none are open.
	 */
	private static Stage getCurrentStage() {
		// Get the primary stage or any showing stage
		return Stage.getWindows().stream().filter(window -> window instanceof Stage && window.isShowing())
				.map(window -> (Stage) window).findFirst().orElse(new Stage());
	}

	/**
	 * Opens a new window displaying the parking history for the current user.
	 *
	 * @param history A list of ParkingOrder objects to display.
	 */
	public static void showParkingHistoryWindow(ArrayList<ParkingOrder> history) {
		try {
			// Load the parking history FXML
			FXMLLoader loader = new FXMLLoader(BParkClientScenes.class.getResource("/client/ParkingHistoryView.fxml"));
			Parent root = loader.load();

			// Get the controller and set up the data
			ParkingHistoryController controller = loader.getController();
			if (currentUser != null) {
				controller.setUserName(currentUser);
			}
			controller.loadHistory(history);

			// Create a new stage for the parking history window
			Stage historyStage = new Stage();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(BParkClientScenes.class.getResource("/css/BParkStyle.css").toExternalForm());

			historyStage.setTitle("Parking History - " + (currentUser != null ? currentUser : "User"));
			historyStage.setScene(scene);
			historyStage.setResizable(true);
			historyStage.show();

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error opening parking history window: " + e.getMessage());
		}
	}
}