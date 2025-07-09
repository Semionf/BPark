package controllers;

import static common.Message.MessageType.ACTIVATE_RESERVATION_KIOSK;
import static common.Message.MessageType.ENTER_PARKING_KIOSK;
import static common.Message.MessageType.FORGOT_CODE_KIOSK;
import static common.Message.MessageType.EXIT_PARKING;
import static common.Message.MessageType.RETRIEVE_CAR_KIOSK;

import java.util.Optional;

import client.BParkKioskScenes;
import common.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

/**
 * Controller class for the Kiosk Dashboard screen. This class handles user
 * interactions in the Kiosk application, such as entering the parking lot,
 * retrieving cars, activating reservations, and handling forgotten codes.
 */
public class KioskDashboardController {

	/**
	 * Default constructor for KioskDashboardController. Required by the JavaFX
	 * framework.
	 */
	public KioskDashboardController() {
		// Default constructor
	}

	/**
	 * Controller for the kiosk dashboard screen. Handles user actions such as
	 * entering parking, retrieving car, activating reservation, and logging out.
	 */

	/** The username of the currently logged-in user */
	private static String loggedInUsername; // Added username storage

	/** The user ID of the currently logged-in user */
	private static int loggedInUserID;

	// UI element declarations
	/** Label that displays user-related information, such as user name or ID */
	@FXML
	private Label lblUserInfo; // Added label reference

	/** Button to initiate the parking entry process */
	@FXML
	private Button btnEnterParking;

	/** Button to trigger the car retrieval process */
	@FXML
	private Button btnRetrieveCar;

	/** Button that allows the user to retrieve their forgotten parking code */
	@FXML
	private Button btnForgotCode;

	/** Button to activate an existing reservation for parking */
	@FXML
	private Button btnActivateReservation;

	/** Button to close the Kiosk application */
	@FXML
	private Button btnExit;

	/** Button to log the user out and return to login screen */
	@FXML
	private Button btnLogout;

	/**
	 * Sets the logged-in user information.
	 * 
	 * @param username the user's name
	 * @param userID   the user's ID
	 */
	public static void setLoggedInUser(String username, int userID) {
		loggedInUsername = username; // Store username

		loggedInUserID = userID;
	}

	/**
	 * Resets the logged-in user information.
	 */
	public static void resetLoggedInUser() {
		loggedInUsername = null; // Reset username

		loggedInUserID = 0;
	}

	/**
	 * Sends a message to enter the parking lot using the logged-in user ID.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleEnterParking(ActionEvent event) {
		Message msg = new Message(ENTER_PARKING_KIOSK, loggedInUserID);
		BParkKioskScenes.sendMessage(msg);
	}

	/**
	 * Initializes the dashboard screen and updates user info label.
	 */
	@FXML
	public void initialize() {
		// Update user info label when the dashboard loads
		if (loggedInUsername != null) {
			lblUserInfo.setText("User: " + loggedInUsername);
		}
	}

	/**
	 * Handles car retrieval using a parking code entered by the user.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleRetrieveCar(ActionEvent event) {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Retrieve Car");
		dialog.setHeaderText(null);
		dialog.setContentText("Enter your parking code:");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(codeStr -> {
			try {
				int parkingInfoID = Integer.parseInt(codeStr);
				// Send parking code with logged in user ID for validation
				// Format: parkingCode,userID  
				String exitData = parkingInfoID + "," + loggedInUserID;
				Message msg = new Message(EXIT_PARKING, exitData);
				BParkKioskScenes.sendMessage(msg);
			} catch (NumberFormatException e) {
				showInfo("Invalid Input", "Parking code must be numeric.");
			}
		});
	}

	/**
	 * Sends a message to the server to request help with a forgotten code.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleForgotCode(ActionEvent event) {
		Message msg = new Message(FORGOT_CODE_KIOSK, loggedInUserID);
		BParkKioskScenes.sendMessage(msg);
	}

	/**
	 * Activates a reservation using a code entered by the user.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleActivateReservation(ActionEvent event) {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Activate Reservation");
		dialog.setHeaderText(null);
		dialog.setContentText("Enter your reservation code:");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(codeStr -> {
			try {
				int parkingInfoID = Integer.parseInt(codeStr);
				Message msg = new Message(ACTIVATE_RESERVATION_KIOSK, parkingInfoID);
				BParkKioskScenes.sendMessage(msg);
			} catch (NumberFormatException e) {
				showInfo("Invalid Input", "Reservation code must be numeric.");
			}
		});
	}

	/**
	 * Exits the dashboard screen and returns to the main kiosk screen.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleExit(ActionEvent event) {
		try {

			FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/KioskMain.fxml"));
			Parent mainRoot = loader.load();
			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			Scene scene = new Scene(mainRoot);
			scene.getStylesheets().add(getClass().getResource("/css/BParkStyle.css").toExternalForm());
			stage.setScene(scene);

		} catch (Exception e) {
			e.printStackTrace();
			showInfo("Error", "Could not return to main screen.");
		}
	}

	/**
	 * Logs out the current user, notifies the server, and returns to the main
	 * screen.
	 * 
	 * @param event the button click event
	 */
	@FXML
	private void handleLogout(ActionEvent event) {
		try {
			// Send logout notification to server
			if (loggedInUserID != 0) {
				// Send logout message in the format used by client app
				BParkKioskScenes.sendStringMessage("LoggedOut " + loggedInUsername);
			}

			// Reset logged in user
			resetLoggedInUser();

			// Return to main kiosk screen
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/KioskMain.fxml"));
			Parent mainRoot = loader.load();
			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			Scene scene = new Scene(mainRoot);
			scene.getStylesheets().add(getClass().getResource("/css/BParkStyle.css").toExternalForm());
			stage.setScene(scene);

			// Show logout confirmation
			showInfo("Logged Out", "You have been successfully logged out.");

		} catch (Exception e) {
			e.printStackTrace();
			showInfo("Error", "Could not complete logout process.");
		}
	}

	/**
	 * Shows a simple alert message with the given title and content.
	 * 
	 * @param title   the title of the alert
	 * @param content the message to display
	 */
	private void showInfo(String title, String content) {
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle(title);
			alert.setHeaderText(null);
			alert.setContentText(content);
			alert.showAndWait();
		});
	}
}