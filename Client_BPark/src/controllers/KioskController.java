package controllers;

import static common.Message.MessageType.KIOSK_ID_LOGIN;
import static common.Message.MessageType.KIOSK_RF_LOGIN;

import java.util.Optional;

import client.BParkKioskScenes;
import common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * Controller class for the kiosk login screen. Handles user interactions such
 * as login by ID, login by RF, check availability, and exit.
 */
public class KioskController {

	/**
	 * Default constructor for KioskController. Used by the JavaFX framework to
	 * instantiate the controller.
	 */
	public KioskController() {
		// Default constructor
	}

	/** Button to log in by entering username and user ID */
	@FXML
	private Button btnLoginByID;

	/** Button to log in using RF */
	@FXML
	private Button btnLoginByRF;

	/** Button to check parking availability */
	@FXML
	private Button btnCheckAvailability;

	/** Button to exit the kiosk application */
	@FXML
	private Button btnExitKiosk;

	/** Main stage of the kiosk application */
	private static Stage mainStage;

	/**
	 * Sets the main application stage so other scenes can be loaded into it.
	 * 
	 * @param stage The main application stage.
	 */
	public static void setMainStage(Stage stage) {
		mainStage = stage;
	}

	/**
	 * Handles the click event for checking parking availability. Sends a message to
	 * the server to request current availability status.
	 */
	@FXML
	private void handleCheckAvailability() {
		// Send a request to the server
		Message checkMsg = new Message(Message.MessageType.CHECK_PARKING_AVAILABILITY, null);
		BParkKioskScenes.sendMessage(checkMsg);

	}

	/**
	 * Handles the kiosk exit button click. Shows a confirmation dialog and exits
	 * the app if the user confirms.
	 */
	@FXML
	private void handleExitKiosk() {
		// Show confirmation dialog
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Exit Kiosk");
		alert.setHeaderText(null);
		alert.setContentText("Are you sure you want to exit the kiosk application?");

		// Custom buttons
		ButtonType exitButton = new ButtonType("Exit", ButtonBar.ButtonData.OK_DONE);
		ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(exitButton, cancelButton);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.isPresent() && result.get() == exitButton) {
			try {
				// Disconnect from server if connected
				if (BParkKioskScenes.isConnected()) {
					BParkKioskScenes.sendStringMessage("KioskTerminating");
					BParkKioskScenes.disconnect();
				}

				// Close the application
				Platform.exit();
				System.exit(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Handles login by manually entering username and user ID. Opens a dialog to
	 * collect user input and sends it to the server.
	 */
	@FXML
	private void handleLoginByID() {
		Platform.runLater(() -> {
			Dialog<Pair<String, String>> dialog = new Dialog<>();
			dialog.setTitle("Login");
			dialog.setHeaderText("Please enter your Username and User ID");

			ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20, 150, 10, 10));

			TextField usernameField = new TextField();
			usernameField.setPromptText("Username");
			TextField userIDField = new TextField();
			userIDField.setPromptText("User ID");

			grid.add(new Label("Username:"), 0, 0);
			grid.add(usernameField, 1, 0);
			grid.add(new Label("User ID:"), 0, 1);
			grid.add(userIDField, 1, 1);

			dialog.getDialogPane().setContent(grid);

			var loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
			loginButton.setDisable(true);

			usernameField.textProperty().addListener((obs, oldVal, newVal) -> loginButton
					.setDisable(newVal.trim().isEmpty() || userIDField.getText().trim().isEmpty()));
			userIDField.textProperty().addListener((obs, oldVal, newVal) -> loginButton
					.setDisable(newVal.trim().isEmpty() || usernameField.getText().trim().isEmpty()));

			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == loginButtonType) {
					return new Pair<>(usernameField.getText(), userIDField.getText());
				}
				return null;
			});

			Optional<Pair<String, String>> result = dialog.showAndWait();

			result.ifPresent(pair -> {
				String username = pair.getKey();
				String userIDStr = pair.getValue();
				try {
					int userID = Integer.parseInt(userIDStr);
					Message msg = new Message(KIOSK_ID_LOGIN, username + "," + userID);
					BParkKioskScenes.sendMessage(msg);
				} catch (NumberFormatException e) {
					showAlert("Invalid Input", "User ID must be numeric.");
				}
			});
		});
	}

	/**
	 * Handles login by RF Prompts the user for a numeric ID and sends it to the
	 * server.
	 */
	@FXML
	private void handleLoginByRF() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Login by RF");
		dialog.setHeaderText(null);
		dialog.setContentText("Enter your User ID:");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(idStr -> {
			try {
				int userID = Integer.parseInt(idStr);
				Message msg = new Message(KIOSK_RF_LOGIN, userID);
				BParkKioskScenes.sendMessage(msg);
			} catch (NumberFormatException e) {
				showAlert("Invalid Input", "Please enter a valid numeric User ID.");
			}
		});
	}

	/**
	 * Handles the login result sent back from the server. If login is successful,
	 * shows a welcome message and loads the dashboard screen.
	 * 
	 * @param content The login result content from the server.
	 */
	public static void handleKioskLoginResult(Object content) {
		if (content instanceof String response) {
			if (!response.isEmpty()) {
				// Split "John Doe,4"
				String[] parts = response.split(",");
				if (parts.length == 2) {
					String name = parts[0].trim();
					int userID = Integer.parseInt(parts[1].trim());

					// Store for future operations
					KioskDashboardController.setLoggedInUser(name, userID);

					// Welcome message and load dashboard
					showWelcomeAndLoadDashboard(name);
				} else {
					showAlertStatic("Login Failed", "Invalid login data received from server.");
				}
			} else {
				showAlertStatic("Login Failed", "Invalid credentials or user not found.");
			}
		}
	}

	/**
	 * Shows an alert with a given title and message.
	 * 
	 * @param title   The title of the alert.
	 * @param message The message to display.
	 */
	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	/**
	 * Shows a static alert from any thread.
	 * 
	 * @param title   The title of the alert.
	 * @param message The message to display.
	 */
	private static void showAlertStatic(String title, String message) {
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle(title);
			alert.setHeaderText(null);
			alert.setContentText(message);
			alert.showAndWait();
		});
	}

	/**
	 * Shows a welcome message after successful login and loads the dashboard scene.
	 * 
	 * @param name The name of the logged-in user.
	 */
	private static void showWelcomeAndLoadDashboard(String name) {
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Login Success");
			alert.setHeaderText(null);
			alert.setContentText("Welcome " + name + "!");
			alert.showAndWait();

			try {
				FXMLLoader loader = new FXMLLoader(KioskController.class.getResource("/client/KioskDashboard.fxml"));
				Parent dashboardRoot = loader.load();
				Scene dashboardScene = new Scene(dashboardRoot);
				dashboardScene.getStylesheets()
						.add(KioskController.class.getResource("/css/BParkStyle.css").toExternalForm());
				mainStage.setScene(dashboardScene);
			} catch (Exception e) {
				e.printStackTrace();
				showAlertStatic("Error", "Failed to load dashboard screen.");
			}
		});
	}
}