package client;

import common.Message;
import controllers.LauncherController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ocsf.client.ObservableClient;

/**
 * Main launcher class for the BPark application. Initializes the JavaFX UI and
 * handles client-server communication setup.
 */
public class BParkLauncherApp extends Application {

	/**
	 * Default constructor for BParkLauncherApp. Required for JavaFX initialization
	 * and class loading.
	 */
	public BParkLauncherApp() {
		// Default constructor
	}

	/**
	 * The client instance responsible for server communication.
	 */
	private static BParkClient client;
	/**
	 * The IP address of the server to connect to. Default is "localhost".
	 */
	private static String serverIP = "localhost";
	/**
	 * The port number of the server to connect to. Default is 5555.
	 */
	private static int serverPort = 5555;
	/**
	 * The primary JavaFX stage used throughout the application.
	 */
	private static Stage primaryStage;

	/**
	 * Starts the JavaFX application and shows the launcher screen.
	 *
	 * @param stage The primary stage.
	 * @throws Exception if the launcher screen cannot be loaded.
	 */
	@Override
	public void start(Stage stage) throws Exception {
		primaryStage = stage;
		showLauncherScreen(stage);
	}

	/**
	 * Shows the initial connection launcher screen.
	 *
	 * @param stage The JavaFX stage.
	 * @throws Exception if FXML loading fails.
	 */
	private void showLauncherScreen(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/Launcher.fxml"));
		Parent root = loader.load();

		// Set main stage in the controller
		LauncherController controller = loader.getController();
		controller.setMainStage(stage);

		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/css/LauncherStyles.css").toExternalForm());
		stage.setTitle("BPark - Connection Launcher");
		stage.setScene(scene);
		stage.setResizable(false);
		stage.show();
	}

	/**
	 * Tests the connection to the server with given IP and port.
	 *
	 * @param ip   The server IP address.
	 * @param port The server port number.
	 * @return true if the connection is successful, false otherwise.
	 */
	public static boolean testConnection(String ip, int port) {
		try {
			// Close existing connection if any
			if (client != null && client.isConnected()) {
				client.closeConnection();
			}

			client = new BParkClient(ip, port);
			client.openConnection();

			// Update stored connection settings if successful
			serverIP = ip;
			serverPort = port;

			return client.isConnected();
		} catch (Exception e) {

			return false;
		}
	}

	/**
	 * Connects to the server using the current stored IP and port.
	 */
	public static void connectToServer() {
		try {
			if (client == null || !client.isConnected()) {
				client = new BParkClient(serverIP, serverPort);
				client.openConnection();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the client is currently connected to the server.
	 *
	 * @return true if connected, false otherwise.
	 */
	public static boolean isConnected() {
		return client != null && client.isConnected();
	}

	/**
	 * Shows the client login scene after successful connection.
	 */
	public static void showClientScene() {
		try {
			BParkClientScenes.showLoginScreen(primaryStage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Shows the kiosk terminal scene after successful connection.
	 */
	public static void showKioskScene() {
		try {
			BParkKioskScenes.showKioskScreen(primaryStage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Inner client class for handling communication with the server.
	 */
	static class BParkClient extends ObservableClient {
		/**
		 * Constructs a client connection to the specified host and port.
		 *
		 * @param host The server host name or IP.
		 * @param port The server port number.
		 */
		public BParkClient(String host, int port) {
			super(host, port);
		}

		/**
		 * Handles messages received from the server.
		 *
		 * @param msg The incoming message object.
		 */
		@Override
		protected void handleMessageFromServer(Object msg) {
			Platform.runLater(() -> {
				try {
					Object message = msg;
					if (message instanceof byte[]) {
						message = ClientMessageHandler.deserialize(msg);
					}

					if (message instanceof Message) {
						ClientMessageHandler.handleMessage((Message) message);
					} else if (message instanceof String) {
						ClientMessageHandler.handleStringMessage((String) message);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}

		/**
		 * Called when the connection to the server is closed.
		 */
		@Override
		protected void connectionClosed() {
			System.out.println("Connection closed");
		}

		/**
		 * Called when there is a connection exception.
		 *
		 * @param exception The thrown exception.
		 */
		@Override
		protected void connectionException(Exception exception) {
			System.out.println("Connection error: " + exception.getMessage());
		}
	}

	/**
	 * Sends a serialized message object to the server.
	 *
	 * @param msg The message to send.
	 */
	public static void sendMessage(Message msg) {
		try {
			if (client != null && client.isConnected()) {
				client.sendToServer(ClientMessageHandler.serialize(msg));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a plain string message to the server.
	 *
	 * @param msg The string to send.
	 */
	public static void sendStringMessage(String msg) {
		try {
			if (client != null && client.isConnected()) {
				client.sendToServer(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * get client
	 * 
	 * @return the BParkClient instance
	 */
	public static BParkClient getClient() {
		return client;
	}

	/**
	 * get server IP
	 *
	 * @return the current server IP address
	 */
	public static String getServerIP() {
		return serverIP;
	}

	/**
	 * get server port
	 *
	 * @return the current server port number
	 */
	public static int getServerPort() {
		return serverPort;
	}

	/**
	 * get primary stage
	 * 
	 * @return the main JavaFX stage
	 */
	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	/**
	 * Disconnects the client from the server and sends disconnect notification.
	 */
	public static void disconnect() {
		try {
			if (client != null && client.isConnected()) {
				client.sendToServer("ClientDisconnect");
				client.closeConnection();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called when the application is closing. Performs cleanup and disconnects from
	 * the server.
	 *
	 * @throws Exception if any error occurs.
	 */
	@Override
	public void stop() throws Exception {
		// Clean up when application closes
		disconnect();
		super.stop();
	}

	/**
	 * Launches the JavaFX application.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		launch(args);
	}
}