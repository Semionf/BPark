package controllers;

import java.net.URL;
import java.util.ResourceBundle;

import client.BParkLauncherApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for the launcher window. Allows user to test connection and choose
 * between client and kiosk applications.
 */
public class LauncherController implements Initializable {
	/**
	 * Default constructor for LauncherController. Required for JavaFX framework
	 * initialization.
	 */
	public LauncherController() {
		// Default constructor
	}

	/** Text field for entering the server IP address */
	@FXML
	private TextField txtServerIP;

	/** Text field for entering the server port number */
	@FXML
	private TextField txtServerPort;

	/** Button to test the connection to the server */
	@FXML
	private Button btnTestConnection;

	/** Label for displaying the current connection status */
	@FXML
	private Label lblStatus;

	/** VBox container for application selection buttons */
	@FXML
	private VBox vboxAppSelection;

	/** Button to launch the Client application */
	@FXML
	private Button btnClientApp;

	/** Button to launch the Kiosk application */
	@FXML
	private Button btnKioskApp;

	/** Main application stage */
	private Stage mainStage;
	/** Flag to prevent duplicate connection attempts */
	private boolean isConnecting = false;

	/**
	 * Initializes the launcher screen. Sets default values and hides the app
	 * selection section.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Set default values
		txtServerIP.setText("localhost");
		txtServerPort.setText("5555");

		// Initially hide app selection
		vboxAppSelection.setVisible(false);
		vboxAppSelection.setManaged(false);
	}

	/**
	 * Sets the main stage for later scene changes.
	 * 
	 * @param stage the main application stage
	 */
	public void setMainStage(Stage stage) {
		this.mainStage = stage;
	}

	/**
	 * Handles connection testing when the user clicks the button. Validates input,
	 * attempts to connect, and updates the UI accordingly.
	 */
	@FXML
	private void handleTestConnection() {
		if (isConnecting) {
			return; // Prevent multiple connection attempts
		}

		String serverIP = txtServerIP.getText().trim();
		String serverPortText = txtServerPort.getText().trim();

		// Validate input
		if (serverIP.isEmpty()) {
			showError("Please enter server IP address");
			txtServerIP.requestFocus();
			return;
		}

		if (serverPortText.isEmpty()) {
			showError("Please enter server port");
			txtServerPort.requestFocus();
			return;
		}

		int serverPort;
		try {
			serverPort = Integer.parseInt(serverPortText);
			if (serverPort < 1 || serverPort > 65535) {
				throw new NumberFormatException("Port out of range");
			}
		} catch (NumberFormatException e) {
			showError("Please enter a valid port number (1-65535)");
			txtServerPort.requestFocus();
			return;
		}

		// Update UI for connection attempt
		isConnecting = true;
		btnTestConnection.setDisable(true);
		btnTestConnection.setText("Testing...");
		lblStatus.setText("Testing connection...");
		lblStatus.getStyleClass().removeAll("status-success", "status-error");
		lblStatus.getStyleClass().add("status-info");

		// Test connection in background thread
		Platform.runLater(() -> {
			new Thread(() -> {
				boolean connected = BParkLauncherApp.testConnection(serverIP, serverPort);

				Platform.runLater(() -> {
					isConnecting = false;
					btnTestConnection.setDisable(false);
					btnTestConnection.setText("Test Connection");

					if (connected) {
						showSuccess("Connection successful! Select application mode.");
						showAppSelection();
					} else {
						showError("Connection failed. Please check server address and port.");
						hideAppSelection();
					}
				});
			}).start();
		});
	}

	/**
	 * Launches the client application.
	 */

	@FXML
	private void handleClientApp() {
		try {
			BParkLauncherApp.showClientScene();
		} catch (Exception e) {
			e.printStackTrace();
			showError("Failed to launch client application: " + e.getMessage());
		}
	}

	/**
	 * Launches the kiosk application.
	 */
	@FXML
	private void handleKioskApp() {
		try {
			BParkLauncherApp.showKioskScene();
		} catch (Exception e) {
			e.printStackTrace();
			showError("Failed to launch kiosk application: " + e.getMessage());
		}
	}

	/**
	 * Shows a success message and updates the label style.
	 * 
	 * @param message the message to display
	 */
	private void showSuccess(String message) {
		lblStatus.setText("✓ " + message);
		lblStatus.getStyleClass().removeAll("status-error", "status-info");
		lblStatus.getStyleClass().add("status-success");
	}

	/**
	 * Shows an error message and updates the label style.
	 * 
	 * @param message the message to display
	 */
	private void showError(String message) {
		lblStatus.setText("✗ " + message);
		lblStatus.getStyleClass().removeAll("status-success", "status-info");
		lblStatus.getStyleClass().add("status-error");
	}

	/**
	 * Displays the application selection panel.
	 */
	private void showAppSelection() {
		vboxAppSelection.setVisible(true);
		vboxAppSelection.setManaged(true);
	}

	/**
	 * Hides the application selection panel.
	 */
	private void hideAppSelection() {
		vboxAppSelection.setVisible(false);
		vboxAppSelection.setManaged(false);
	}
}