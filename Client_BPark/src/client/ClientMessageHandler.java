package client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import common.Message;
import common.ParkingOrder;
import common.ParkingReport;
import common.ParkingSubscriber;
import controllers.AttendantController;
import controllers.ExtendParkingController;
import controllers.KioskController;
import controllers.LoginController;
import controllers.ManagerController;
import controllers.UpdateProfileController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;

/**
 * Handles incoming messages from the server and dispatches them to the
 * appropriate UI controllers or handlers in the client application.
 */
public class ClientMessageHandler {

	/**
	 * Default constructor for ClientMessageHandler. Required to support
	 * instantiation and reflection when needed.
	 */
	public ClientMessageHandler() {
		// Default constructor
	}

	/**
	 * Handles an incoming structured { Message} object from the server, determining
	 * its type and routing it to the appropriate handler method.
	 *
	 * @param message the message received from the server
	 */
	public static void handleMessage(Message message) {
		switch (message.getType()) {
		case SUBSCRIBER_LOGIN_RESPONSE:
			handleLoginResponse(message);
			break;

		case KIOSK_LOGIN_RESPONSE:
			handleKioskLoginResponse(message);
			break;

		case ENTER_PARKING_KIOSK_RESPONSE:
			handleEnterParkingKioskResponse(message);
			break;

		case RETRIEVE_CAR_KIOSK_RESPONSE:
			handleRetrieveCarKioskResponse(message);
			break;

		case FORGOT_CODE_KIOSK_RESPONSE:
			handleForgotCodeKioskResponse(message);
			break;

		case ACTIVATE_RESERVATION_KIOSK_RESPONSE:
			handleActivateReservationKioskResponse(message);
			break;

		case PARKING_AVAILABILITY_RESPONSE:
			handleParkingAvailability(message);
			break;

		case RESERVATION_RESPONSE:
			handleReservationResponse(message);
			break;

		case REGISTRATION_RESPONSE:
			handleRegistrationResponse(message);
			break;

		case LOST_CODE_RESPONSE:
			handleLostCodeResponse(message);
			break;

		case PARKING_HISTORY_RESPONSE:
			handleParkingHistory(message);
			break;

		case MANAGER_SEND_REPORTS:
			handleReports(message);
			break;

		case ACTIVE_PARKINGS_RESPONSE:
			handleActiveParkings(message);
			break;

		case UPDATE_SUBSCRIBER_RESPONSE:
			handleUpdateResponse(message);
			break;

		case SUBSCRIBER_DATA_RESPONSE:
			handleSubscriberDataResponse(message);
			break;

		case ACTIVATION_RESPONSE:
			handleActivationResponse(message);
			break;

		case CANCELLATION_RESPONSE:
			handleCancellationResponse(message);
			break;

		case EXTENSION_RESPONSE:
			handleExtendParkingResponse(message);
			break;

		case EXIT_PARKING_RESPONSE:
			handleExitParkingResponse(message);
			break;

		case SHOW_SUBSCRIBER_DETAILS:
			ParkingSubscriber subscriber = (ParkingSubscriber) message.getContent();
			Platform.runLater(() -> {
				if (BParkClientScenes.getAttendantController() != null)
					BParkClientScenes.getAttendantController().showSubscriberDetails(subscriber);
				else if (BParkClientScenes.getManagerController() != null)
					BParkClientScenes.getManagerController().showSubscriberDetails(subscriber);
			});
			break;

		case SHOW_ALL_SUBSCRIBERS:
			List<ParkingSubscriber> subs = (List<ParkingSubscriber>) message.getContent();
			if (BParkClientScenes.getAttendantController() != null)
				BParkClientScenes.getAttendantController().updateSubscriberTable(subs);
			if (BParkClientScenes.getManagerController() != null)
				BParkClientScenes.getManagerController().updateSubscriberTable(subs);
			break;

		default:
			System.out.println("Unknown message type: " + message.getType());
		}
	}

	/**
	 * Handles a plain string message received from the server, using a custom
	 * command-based protocol.
	 *
	 * @param message the raw string message from the server
	 */
	public static void handleStringMessage(String message) {
		String[] parts = message.split(" ", 2);
		String command = parts[0];
		String data = parts.length > 1 ? parts[1] : "";

		switch (command) {
		case "login:":
			handleStringLoginResponse(data);
			break;

		case "availableSpots":
			showAlert("Available Spots", "Current available spots: " + data);
			break;

		case "enterResult":
			showAlert("Entry Result", data);
			break;

		case "exitResult":
			showAlert("Exit Result", data);
			break;

		case "parkingCode":
			showAlert("Lost Code", "Your parking code is: " + data);
			break;

		case "reservationResult":
			showAlert("Reservation", data);
			break;

		default:
			System.out.println("Unknown string command: " + command);
		}
	}

	/**
	 * Handles the login response for a subscriber, displaying success or error
	 * alert.
	 *
	 * @param message the message containing login result
	 */
	private static void handleLoginResponse(Message message) {
		ParkingSubscriber subscriber = (ParkingSubscriber) message.getContent();

		if (subscriber != null) {
			BParkClientScenes.setCurrentUser(subscriber.getSubscriberCode());
			BParkClientScenes.setCurrentUserID(subscriber.getSubscriberID());
			BParkClientScenes.setUserType(subscriber.getUserType());
			BParkClientScenes.switchToMainScreen(subscriber.getUserType());

			Platform.runLater(() -> LoginController.getInstance().handleLoginSuccess(subscriber.getUserType()));
		} else {
			Platform.runLater(() -> {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Login Failed");
				alert.setHeaderText(null);
				alert.setContentText("Invalid username/userCode or user not found.");
				alert.showAndWait();
				LoginController.getInstance().handleLoginFailed(null);
			});
		}
	}

	/**
	 * Handles the kiosk login result from the server.
	 *
	 * @param message the message containing kiosk login data
	 */
	private static void handleKioskLoginResponse(Message message) {
		KioskController.handleKioskLoginResult(message.getContent());
	}

	/**
	 * Handles the response for entering a parking lot via kiosk.
	 *
	 * @param message the message containing the result string
	 */
	private static void handleEnterParkingKioskResponse(Message message) {
		String response = (String) message.getContent();
		showAlert("Enter Parking", response);
	}

	/**
	 * Handles the response for entering a parking lot via kiosk.
	 *
	 * @param message the message containing the result string
	 */
	private static void handleRetrieveCarKioskResponse(Message message) {
		String response = (String) message.getContent();
		showAlert("Retrieve Car", response);
	}

	/**
	 * Handles the response for forgotten code via kiosk.
	 *
	 * @param message the message containing the parking code
	 */
	private static void handleForgotCodeKioskResponse(Message message) {
		String response = (String) message.getContent();
		showAlert("Parking Code", response);
	}

	/**
	 * Handles the activation response for a reservation from the kiosk.
	 *
	 * @param message the message containing the activation result
	 */
	private static void handleActivateReservationKioskResponse(Message message) {
		String response = (String) message.getContent();
		showAlert("Activate Reservation", response);
	}

	/**
	 * Handles the server's response regarding current parking availability.
	 *
	 * @param message the message containing the number of available spots
	 */
	private static void handleParkingAvailability(Message message) {
		Integer availableSpots = (Integer) message.getContent();
		showAlert("Parking Availability", "Available spots: " + availableSpots);
	}

	/**
	 * Handles the response to a parking reservation request.
	 *
	 * @param message the message containing reservation result
	 */
	private static void handleReservationResponse(Message message) {
		String response = (String) message.getContent();
		if (response.startsWith("SUCCESS") || response.contains("confirmed")) {
			showAlert("Reservation Success", response);
		} else {
			showAlert("Reservation Failed", response);
		}
	}

	/**
	 * Handles the result of a registration attempt.
	 *
	 * @param message the message containing registration result
	 */
	private static void handleRegistrationResponse(Message message) {
		String response = (String) message.getContent();
		if (response.startsWith("SUCCESS")) {
			showAlert("Registration Success", response);
		} else {
			showAlert("Registration Failed", response);
		}
	}

	/**
	 * Handles the response to a lost code recovery request.
	 *
	 * @param message the message containing either the code or an error message
	 */
	private static void handleLostCodeResponse(Message message) {
		String code = (String) message.getContent();
		if (code.matches("\\d+")) {
			showAlert("Parking Code Recovery",
					"Your parking code is: " + code + "\n" + "This has also been sent to your email/SMS");
		} else {
			showAlert("Code Recovery Failed", code);
		}
	}

	/**
	 * Handles a request for the user's parking history.
	 *
	 * @param message the message containing a list of ParkingOrder}
	 */
	@SuppressWarnings("unchecked")
	private static void handleParkingHistory(Message message) {
		ArrayList<ParkingOrder> history = (ArrayList<ParkingOrder>) message.getContent();

		// Open the parking history window with the received data
		Platform.runLater(() -> {
			BParkClientScenes.showParkingHistoryWindow(history);
		});
	}

	/**
	 * Handles a report broadcast from the server to the manager screen.
	 *
	 * @param message the message containing a list ofParkingReport}
	 */
	@SuppressWarnings("unchecked")
	private static void handleReports(Message message) {
		ArrayList<ParkingReport> reports = (ArrayList<ParkingReport>) message.getContent();

		ManagerController managerController = BParkClientScenes.getManagerController();
		if (managerController != null) {
			managerController.updateReports(reports);
		}
	}

	/**
	 * Handles a request for the list of currently active parkings.
	 *
	 * @param message the message containing a list of {@link ParkingOrder}
	 */
	@SuppressWarnings("unchecked")
	private static void handleActiveParkings(Message message) {
		ArrayList<ParkingOrder> activeParkings = (ArrayList<ParkingOrder>) message.getContent();

		AttendantController controller = BParkClientScenes.getAttendantController();
		if (controller != null) {
			controller.updateActiveParkings(FXCollections.observableArrayList(activeParkings));
		}

		ManagerController managerController = BParkClientScenes.getManagerController();
		if (managerController != null) {
			managerController.updateActiveParkings(FXCollections.observableArrayList(activeParkings));
		}
	}

	/**
	 * Handles the result of a subscriber profile update request.
	 *
	 * @param message the message containing result string
	 */
	private static void handleUpdateResponse(Message message) {
		String response = (String) message.getContent();
		showAlert("Update Profile", response);
	}

	/**
	 * Handles the retrieval of full subscriber data for display in a profile form.
	 *
	 * @param message the message containing a { ParkingSubscriber} object
	 */
	private static void handleSubscriberDataResponse(Message message) {
		ParkingSubscriber subscriber = (ParkingSubscriber) message.getContent();
		Platform.runLater(() -> {
			UpdateProfileController controller = BParkClientScenes.getUpdateProfileController();
			controller.setFieldPrompts(subscriber.getEmail(), subscriber.getPhoneNumber(), subscriber.getCarNumber());
		});
	}

	/**
	 * Handles the activation response of a reservation via attendant or manager.
	 *
	 * @param message the message containing result string
	 */
	private static void handleActivationResponse(Message message) {
		String response = (String) message.getContent();
		if (response.contains("successful") || response.contains("activated")) {
			showAlert("Reservation Activated", response);
		} else {
			showAlert("Activation Failed", response);
		}
	}

	/**
	 * Handles string-based login responses in legacy mode.
	 *
	 * @param data the login response string
	 */
	private static void handleStringLoginResponse(String data) {
		if (!data.equals("None")) {
			BParkClientScenes.setUserType(data);
			BParkClientScenes.switchToMainScreen(data);
		} else {
			showAlert("Login Failed", "Invalid credentials");
		}
	}

	/**
	 * Handles AvailableSpots
	 *
	 * @param data the login response string
	 */
	private static void handleStringAvailableSpots(String data) {
		showAlert("Available Spots", "Current available spots: " + data);
	}

	/**
	 * Handles the cancellation response for a reservation.
	 *
	 * @param message the message containing result string
	 */
	private static void handleCancellationResponse(Message message) {
		String response = (String) message.getContent();
		showAlert("Reservation Cancellation", response);
	}

	/**
	 * Handles the response for a request to extend parking time.
	 *
	 * @param message the message containing the extension result
	 */
	private static void handleExtendParkingResponse(Message message) {
		String response = (String) message.getContent();

		// show popup as before
		if (response.contains("Parking time extended")) {

			showAlert("Extension Successful", response);
			ExtendParkingController controller = BParkClientScenes.getExtendParkingController();
			if (controller != null) {
				controller.setStatusMessage("Extension successful!", "green");
			}
		} else {
			showAlert("Extension Failed", response);
			ExtendParkingController controller = BParkClientScenes.getExtendParkingController();
			if (controller != null) {
				controller.setStatusMessage(response, "red");
			}
		}
	}

	/**
	 * Handles the response message received after a parking exit attempt.
	 * Displays an alert to the user indicating whether the exit was successful or not,
	 * based on the message content.
	 *
	 * @param message the message containing the server's response to the exit attempt
	 */
	private static void handleExitParkingResponse(Message message) {
		String response = (String) message.getContent();
		if (response.contains("Exit successful") || response.contains("Thank you")) {
			showAlert("Exit Successful", response);
		} else {
			showAlert("Exit Failed", response);
		}
	}

	/**
	 * Serializes a { Message} object into a byte array for network transmission.
	 *
	 * @param msg the message to serialize
	 * @return a byte array representing the serialized object
	 */
	public static byte[] serialize(Message msg) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bos)) {
			out.writeObject(msg);
			out.flush();
			return bos.toByteArray();
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Deserializes a byte array back into a Java object.
	 *
	 * @param msg the serialized object as byte[]
	 * @return the deserialized object or null if failed
	 */
	public static Object deserialize(Object msg) {
		try (ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) msg);
				ObjectInputStream in = new ObjectInputStream(bis)) {
			return in.readObject();
		} catch (IOException | ClassNotFoundException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Shows a simple JavaFX alert with the provided title and message content.
	 *
	 * @param title   the alert window title
	 * @param content the message to display
	 */
	private static void showAlert(String title, String content) {
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle(title);
			alert.setHeaderText(null);
			alert.setContentText(content);
			alert.showAndWait();
		});
	}
}