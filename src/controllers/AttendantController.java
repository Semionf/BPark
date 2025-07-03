package controllers;

import java.net.URL;
import java.util.ResourceBundle;

import client.BParkClientApp;
import entities.Message;
import entities.Message.MessageType;
import entities.ParkingOrder;
import entities.ParkingSubscriber;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.util.Duration;

public class AttendantController implements Initializable {

	
	// Registration form fields
	@FXML
	private TextField txtName;
	@FXML
	private TextField txtPhone;
	@FXML
	private TextField txtEmail;
	@FXML
	private TextField txtCarNumber;
	@FXML
	private TextField txtUsername;
	@FXML
	private Label lblRegistrationStatus;


	@FXML
	private Button btnLogout;

	// Active parkings table
	@FXML
	private TableView<ParkingOrder> tableActiveParkings;
	@FXML
	private TableColumn<ParkingOrder, String> colParkingCode;
	@FXML
	private TableColumn<ParkingOrder, String> colSubscriberName;
	@FXML
	private TableColumn<ParkingOrder, String> colSpot;
	@FXML
	private TableColumn<ParkingOrder, String> colEntryTime;
	@FXML
	private TableColumn<ParkingOrder, String> colExpectedExit;
	@FXML
	private TableColumn<ParkingOrder, String> colType;

	@FXML
	private TableView<ParkingSubscriber> tableSubscribers;
	@FXML
	private TableColumn<ParkingSubscriber, String> colSubName;
	@FXML
	private TableColumn<ParkingSubscriber, String> colSubPhone;
	@FXML
	private TableColumn<ParkingSubscriber, String> colSubEmail;
	@FXML
	private TableColumn<ParkingSubscriber, String> colSubCar;
	@FXML
	private TableColumn<ParkingSubscriber, String> colSubUsername;

	// System Status
	@FXML
	private ProgressBar progressOccupancy;
	@FXML
	private Label lblOccupancyDetails;
	@FXML
	private Label lblParkingStatus;
	@FXML
	private Label lblAttendantInfo;

	// Quick Assist Controls
	@FXML
	private TextField txtAssistCode;
	@FXML
	private ComboBox<String> comboAssistAction;

	private ObservableList<ParkingOrder> activeParkings = FXCollections.observableArrayList();
	
	public void setUserName(String userName) {
	    if (lblAttendantInfo != null) {
	        lblAttendantInfo.setText("Attendant: " + userName);
	    }
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		BParkClientApp.setAttendantController(this);
		setupUI();
		loadActiveParkings();
		loadSubscribers();
	}

	private void setupUI() {

		if (comboAssistAction != null) {
			lblAttendantInfo.setText("Attendant: " + BParkClientApp.getCurrentUser());
			comboAssistAction.getItems().addAll("Help with Entry", "Help with Exit", "Lost Code Recovery",
					"Extend Parking Time");
		}

		if (tableActiveParkings != null) {
			tableActiveParkings.setItems(activeParkings);
			setupTableColumns();
		}

		if (tableSubscribers != null) {
			colSubName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
			colSubPhone.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhoneNumber()));
			colSubEmail.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
			colSubCar.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCarNumber()));
			colSubUsername
					.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSubscriberCode()));
		}
		startAutoRefresh();
	}

	private void setupTableColumns() {
		// Configure table columns to display parking order data
		colParkingCode.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getParkingCode()));
		colSubscriberName
				.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSubscriberName()));
		colSpot.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSpotNumber()));
		colEntryTime
				.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedEntryTime()));
		colExpectedExit.setCellValueFactory(
				cellData -> new SimpleStringProperty(cellData.getValue().getFormattedExpectedExitTime()));
		colType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderType()));
	}

	private void startAutoRefresh() {
		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> loadActiveParkings()));
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();
	}

	// ===== Action Handlers =====

	@FXML
	private void handleRegisterSubscriber() {
		// Validate all fields
		if (!validateRegistrationForm()) {
			return;
		}

		String registrationData = String.format("%s,%s,%s,%s,%s,%s", BParkClientApp.getCurrentUser(), // Attendant
																										// username
				txtName.getText().trim(), txtPhone.getText().trim(), txtEmail.getText().trim(),
				txtCarNumber.getText().trim(), txtUsername.getText().trim());

		Message msg = new Message(MessageType.REGISTER_SUBSCRIBER, registrationData);
		BParkClientApp.sendMessage(msg);
	}

	@FXML
	private void handleGenerateUsername() {
		String baseName = txtName.getText().trim();
		if (baseName.isEmpty()) {
			showAlert("Error", "Please enter subscriber name first");
			return;
		}

		// Generate username suggestion based on name
		String suggestion = baseName.toLowerCase().replaceAll("[^a-z0-9]", "");
		txtUsername.setText(suggestion);
	}

	@FXML
	private void loadActiveParkings() {
		Message msg = new Message(MessageType.GET_ACTIVE_PARKINGS, null);
		BParkClientApp.sendMessage(msg);
	}

	public void updateActiveParkings(ObservableList<ParkingOrder> parkings) {
		this.activeParkings.clear();
		this.activeParkings.addAll(parkings);
		Platform.runLater(() -> {
			if (lblParkingStatus != null) {
				lblParkingStatus.setText(String.format("Active Parking Spots: %d", parkings.size()));
			}
		});
	}

	@FXML
	private void loadSubscribers() {
		Message msg = new Message(MessageType.GET_ALL_SUBSCRIBERS, null);
		BParkClientApp.sendMessage(msg);
	}

	public void updateSubscriberTable(java.util.List<ParkingSubscriber> subscribers) {
		Platform.runLater(() -> {
			ObservableList<ParkingSubscriber> list = FXCollections.observableArrayList(subscribers);
			tableSubscribers.setItems(list);
		});
	}

	@FXML
	private void handleAssistAction() {
		String code = txtAssistCode.getText().trim();
		String action = comboAssistAction.getValue();

		if (code.isEmpty() || action == null) {
			showAlert("Error", "Please enter code and select action");
			return;
		}

		switch (action) {
		case "Help with Entry":
			assistWithEntry(code);
			break;

		case "Help with Exit":
			Message exitMsg = new Message(MessageType.EXIT_PARKING, code);
			BParkClientApp.sendMessage(exitMsg);
			break;

		case "Lost Code Recovery":
			Message lostMsg = new Message(MessageType.REQUEST_LOST_CODE, code);
			BParkClientApp.sendMessage(lostMsg);
			break;

		case "Extend Parking Time":
			showExtensionDialog(code);
			break;
		}
	}

	@FXML
	private void handleManualEntry() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Manual Parking Entry");
		dialog.setHeaderText("Enter subscriber username for immediate parking:");
		dialog.setContentText("Username:");

		dialog.showAndWait().ifPresent(username -> {
			if (!username.trim().isEmpty()) {
				Message msg = new Message(MessageType.ENTER_PARKING, username);
				BParkClientApp.sendMessage(msg);
			}
		});
	}

	@FXML
	private void handleViewSubscriberDetails() {
		ParkingOrder selectedOrder = tableActiveParkings.getSelectionModel().getSelectedItem();
		if (selectedOrder != null) {
			String subscriberName = selectedOrder.getSubscriberName();
			Message msg = new Message(MessageType.GET_SUBSCRIBER_BY_NAME, subscriberName);
			BParkClientApp.sendMessage(msg);
		} else {
			showAlert("Selection Required", "Please select a parking session from the table");
		}
	}

	@FXML
	private void clearRegistrationForm() {
		txtName.clear();
		txtPhone.clear();
		txtEmail.clear();
		txtCarNumber.clear();
		txtUsername.clear();
		lblRegistrationStatus.setText("");
	}

	// ===== Helper Methods =====

	private boolean validateRegistrationForm() {
		if (txtName.getText().trim().isEmpty()) {
			showError("Validation Error", "Name is required");
			return false;
		}

		if (txtPhone.getText().trim().isEmpty()) {
			showError("Validation Error", "Phone number is required");
			return false;
		}

		if (txtEmail.getText().trim().isEmpty()) {
			showError("Validation Error", "Email is required");
			return false;
		}

		if (txtUsername.getText().trim().isEmpty()) {
			showError("Validation Error", "Username is required");
			return false;
		}

		// Basic email validation
		if (!txtEmail.getText().matches(".+@.+\\..+")) {
			showError("Validation Error", "Invalid email format");
			return false;
		}

		// Phone validation (Israeli format)
		if (!txtPhone.getText().matches("0\\d{9}|\\+972\\d{9}")) {
			showError("Validation Error", "Invalid phone format (use 0XXXXXXXXX or +972XXXXXXXXX)");
			return false;
		}

		return true;
	}

	private void assistWithEntry(String subscriberCode) {
		// Check if this is a reservation code or subscriber code
		if (subscriberCode.matches("\\d+") && subscriberCode.length() == 6) {
			// Looks like a reservation code
			Message msg = new Message(MessageType.ACTIVATE_RESERVATION, subscriberCode);
			BParkClientApp.sendMessage(msg);
		} else {
			// Treat as subscriber username
			Message msg = new Message(MessageType.ENTER_PARKING, subscriberCode);
			BParkClientApp.sendMessage(msg);
		}
	}

	private void showExtensionDialog(String parkingCode) {
		ChoiceDialog<String> dialog = new ChoiceDialog<>("1", "1", "2", "3", "4");
		dialog.setTitle("Extend Parking");
		dialog.setHeaderText("Extend parking for code: " + parkingCode);
		dialog.setContentText("Extension hours:");

		dialog.showAndWait().ifPresent(hours -> {
			String extensionData = parkingCode + "," + hours;
			Message msg = new Message(MessageType.EXTEND_PARKING, extensionData);
			BParkClientApp.sendMessage(msg);
		});
	}

//    private void showSubscriberDetails(ParkingOrder parkingOrder) {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Parking Session Details");
//        alert.setHeaderText("Details for " + parkingOrder.getSubscriberName());
//        
//        String details = String.format(
//            "Parking Code: %s\n" +
//            "Spot: %s\n" +
//            "Entry Time: %s\n" +
//            "Expected Exit: %s\n" +
//            "Type: %s\n" +
//            "Status: %s\n" +
//            "Duration: %s",
//            parkingOrder.getParkingCode(),
//            parkingOrder.getSpotNumber(),
//            parkingOrder.getFormattedEntryTime(),
//            parkingOrder.getFormattedExpectedExitTime(),
//            parkingOrder.getOrderType(),
//            parkingOrder.getStatus(),
//            parkingOrder.getParkingDurationFormatted()
//        );
//        
//        alert.setContentText(details);
//        alert.showAndWait();
//    }

	public void showSubscriberDetails(ParkingSubscriber parkingSubscriber) {
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Subscriber Details");
			alert.setHeaderText("Details for " + parkingSubscriber.getFirstName());

			String details = String.format("User ID: %s\nName: %s\nPhone: %s\nEmail: %s\nCar num: %s\ntype: %s",
					parkingSubscriber.getSubscriberID(), parkingSubscriber.getFirstName(),
					parkingSubscriber.getPhoneNumber(), parkingSubscriber.getEmail(), parkingSubscriber.getCarNumber(),
					parkingSubscriber.getUserType());

			alert.setContentText(details);
			alert.showAndWait();
		});
	}

	// ===== UI Update Methods =====

	public void showRegistrationSuccess(String message) {
		Platform.runLater(() -> {
			lblRegistrationStatus.setText("✓ " + message);
			lblRegistrationStatus.setStyle("-fx-text-fill: green;");
			clearRegistrationForm();

			// Clear success message after 5 seconds
			Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> lblRegistrationStatus.setText("")));
			timeline.play();
		});
	}

	public void showRegistrationError(String message) {
		Platform.runLater(() -> {
			lblRegistrationStatus.setText("✗ " + message);
			lblRegistrationStatus.setStyle("-fx-text-fill: red;");
		});
	}

	// ===== Utility Methods =====

	private void showAlert(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}

	private void showError(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}

	@FXML
	private void handleLogout() {
		BParkClientApp.disconnect();
		Platform.exit();
		System.exit(0);
	}
}