package client;

import common.Message;
import controllers.KioskController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Utility class to manage and display scenes for the BPark Kiosk application.
 * Provides methods for screen transitions and server communication.
 */
public class BParkKioskScenes {
	/**
	 * Default constructor for BParkKioskScenes. Required for JavaFX or utility
	 * access.
	 */
	public BParkKioskScenes() {
		// Default constructor
	}

	/**
	 * Displays the main kiosk screen on the given stage.
	 *
	 * @param stage The primary stage where the kiosk screen will be shown.
	 * @throws Exception if the FXML file cannot be loaded.
	 */
	public static void showKioskScreen(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(BParkKioskScenes.class.getResource("/client/KioskMain.fxml"));
		Parent root = loader.load();

		// Set main stage in the controller to allow screen switching
		KioskController.setMainStage(stage);

		Scene scene = new Scene(root);
		scene.getStylesheets().add(BParkKioskScenes.class.getResource("/css/BParkStyle.css").toExternalForm());
		stage.setTitle("BPark - Kiosk Terminal");
		stage.setScene(scene);
		stage.setResizable(false);
		stage.show();
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
	 * Connects to the server if not already connected.
	 */
	public static void connectToServer() {
		BParkLauncherApp.connectToServer();
	}

	/**
	 * Disconnects from the server if currently connected.
	 */
	public static void disconnect() {
		BParkLauncherApp.disconnect();
	}
}