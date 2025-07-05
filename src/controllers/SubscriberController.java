package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import java.io.IOException;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;

import java.util.Optional;
import java.util.ResourceBundle;

import client.BParkClientApp;
import entities.Message;
import entities.Message.MessageType;
import entities.ParkingOrder;

/**
 * Controller class for managing subscriber-related functionality
 * in the BPark client application.
 * 
 * This class manages UI interactions such as checking availability,
 * making and canceling reservations,
 * updating profiles, and viewing history. It communicates with the
 * server via the BParkClientApp and updates the UI accordingly.
 */

public class SubscriberController implements Initializable {
    
	 /** Buttons for navigation and main actions. */
    @FXML private Button btnCheckAvailability;
    @FXML private Button btnMakeReservation;
    @FXML private Button btnViewHistory;
    @FXML private Button btnUpdateProfile;
    @FXML private Button btnLogout;
    
    
    /** Fields for parking and reservation input. */
    @FXML private TextField txtParkingCode;
    @FXML private TextField txtCancelCode;
    
    /** Label to show available parking spots and current user. */
    @FXML private Label lblAvailableSpots;
    @FXML private Label lblUserInfo;
    
    /** Reservation form controls. */
    @FXML private DatePicker datePickerReservation;
    @FXML private ComboBox<String> comboTimeSlot;
    @FXML private Label lblReservationStatus;
    
    /** Table to show past parking history. */
    @FXML private TableView<ParkingOrder> tableParkingHistory;
    @FXML private TableColumn<ParkingOrder, String> colDate;
    @FXML private TableColumn<ParkingOrder, String> colEntry;
    @FXML private TableColumn<ParkingOrder, String> colExit;
    @FXML private TableColumn<ParkingOrder, String> colSpot;
    @FXML private TableColumn<ParkingOrder, String> colStatus;
    
   
    
    /** Main content container to dynamically load views. */
    @FXML private VBox mainContent;
    
    /** Whether the user requested manual check for spots. */
    private static boolean manualCheckRequested = false;
    
    /** Observable list for storing and displaying parking history. */
    private ObservableList<ParkingOrder> parkingHistory = FXCollections.observableArrayList();
    
    /** Called when the controller is loaded. Sets up the UI. */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	setupUIreservation();
    }
    
    /** Sets the label with the current user's name. */
    public void setUserName(String userName) {
        lblUserInfo.setText("User: " + userName);
    }
    
    /** Changes the manual check request flag. */
    public static void setManualCheckRequested(boolean value) {
        manualCheckRequested = value;
    }
    

    /** Loads the home view UI into the main content area. */
    public void loadHomeView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/HomeView.fxml"));
            Parent homeView = loader.load();
            mainContent.getChildren().clear();
            mainContent.getChildren().add(homeView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    /** Loads the Reservation view FXML. */
    @FXML
    private void showReservationView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/ReservationView.fxml"));
            Parent reservationView = loader.load();

            mainContent.getChildren().clear();
            mainContent.getChildren().add(reservationView);

            setupUIreservation();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    /** Loads the profile view for updating user details. */
    @FXML
    private void showProfileView() {
    	  try {
    		// Load the FXML file for the profile update screen
              FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/UpdateProfileView.fxml"));
           // Load the actual UI components from the FXML file into a Node object
              Node profileView = loader.load();
              mainContent.getChildren().setAll(profileView);  // This replaces the center of the UI
          } catch (IOException e) {
              e.printStackTrace();
          }
    }

    
    // ===== Action Handlers =====
    
    @FXML
    private void handleGoHome() {
        loadHomeView();
    }
    
    /** Sends a message to check parking availability. */
    @FXML
    private void handleShowAvailableSpots() {
    	setManualCheckRequested(true);
        Message checkMsg = new Message(Message.MessageType.CHECK_PARKING_AVAILABILITY, null);
        BParkClientApp.sendMessage(checkMsg);
        
    }
    
    /** Sends a message to check parking availability. */
    @FXML
    private void checkParkingAvailability() {
        Message msg = new Message(MessageType.CHECK_PARKING_AVAILABILITY, null);
        BParkClientApp.sendMessage(msg);
    }
    

    
    /** Sends reservation data to the server. */
    @FXML
    private void handleMakeReservation() {
        LocalDate selectedDate = datePickerReservation.getValue();
        String selectedTime = comboTimeSlot.getValue();
        
        if (selectedDate == null || selectedTime == null) {
            showAlert("Error", "Please select both date and time");
            return;
        }
        
        // Format: "YYYY-MM-DD HH:MM"
        String dateTimeStr = selectedDate.toString() + " " + selectedTime;
        String reservationData = BParkClientApp.getCurrentUser() + "," + dateTimeStr;
        
        Message msg = new Message(MessageType.RESERVE_PARKING, reservationData);
        BParkClientApp.sendMessage(msg);
    }
    

    
    /** Cancels a reservation using the code from the page's text field. */
    @FXML
    private void handleCancelReservationFromPage() {
        String code = txtCancelCode.getText();

        if (code != null && !code.trim().isEmpty()) {
            // Step 1: Show confirmation alert
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Cancellation");
            confirmAlert.setHeaderText("Are you sure you want to cancel your reservation?");
            confirmAlert.setContentText("Reservation code: " + code);

            // Step 2: Wait for user response
            Optional<ButtonType> result = confirmAlert.showAndWait();

            // Step 3: If user confirms, proceed with cancellation
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String cancellationData = BParkClientApp.getCurrentUser() + "," + code;
                Message msg = new Message(MessageType.CANCEL_RESERVATION, cancellationData);
                BParkClientApp.sendMessage(msg);
                txtCancelCode.clear();
            }
        }
    }
    
    /** Requests parking history data from the server. */
    @FXML
    private void handleViewHistory() {
        Message msg = new Message(MessageType.GET_PARKING_HISTORY, BParkClientApp.getCurrentUser());
        BParkClientApp.sendMessage(msg);
    }
    
    
    /**
     * Sets up reservation UI elements including time slot combo box and date picker.
     * 
     * <p>
     * The combo box is populated with time slots in 15-minute intervals from 06:00 to 22:45.
     * The date picker is restricted to only allow dates between 1 to 7 days from today.
     * </p>
     */
    private void setupUIreservation() {
        // Initialize time slots for reservation (15-minute intervals)
        ObservableList<String> timeSlots = FXCollections.observableArrayList();
        for (int hour = 6; hour <= 22; hour++) {
            for (int minute = 0; minute < 60; minute += 15) {
                timeSlots.add(String.format("%02d:%02d", hour, minute));
            }
        }
        if (comboTimeSlot != null) {
            comboTimeSlot.setItems(timeSlots);
        }
        
        // Set date picker constraints (1-7 days from today)
        if (datePickerReservation != null) {
            datePickerReservation.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate today = LocalDate.now();
                    setDisable(empty || date.isBefore(today.plusDays(1)) || date.isAfter(today.plusDays(7)));
                }
            });
        }
        
        // Setup parking history table
        if (tableParkingHistory != null) {
            tableParkingHistory.setItems(parkingHistory);
            // Setup column cell value factories here
        }
    }
    
  
    
    
    /** Loads the Extend Parking view into the mainContent pane. */
    @FXML
    private void handleExtendParking() { 
    	   try {
    	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/ExtendParkingView.fxml"));
    	        Node extendParkingView = loader.load();
    	        mainContent.getChildren().setAll(extendParkingView); 
    	    } catch (IOException e) {
    	        e.printStackTrace();
    	    }
    }
   


    /** Logs the user out and closes the current window. */
    @FXML
    private void handleLogout() {
        // Send logout notification
        BParkClientApp.sendStringMessage("LoggedOut " + BParkClientApp.getCurrentUser());
        
        // Close connection and return to login
        try {
            // Close current window and show login again
            btnLogout.getScene().getWindow().hide();
            // The main app should handle showing login screen again
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    /**
     * Initializes the reservation form by restricting date selection and populating
     * the time slot combo box with values from 06:00 to 22:45 at 15-minute intervals.
     *
     * <p>
     * This method is called when the reservation screen is displayed, ensuring users can
     * only select valid reservation times and dates.
     * </p>
     */
    private void setupReservationForm() {
        datePickerReservation.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.isBefore(today.plusDays(1)) || date.isAfter(today.plusDays(7)));
            }
        });

        comboTimeSlot.getItems().clear();
        LocalTime time = LocalTime.of(6, 0); // from 06:00
        while (!time.isAfter(LocalTime.of(22, 45))) {
            comboTimeSlot.getItems().add(time.toString());
            time = time.plusMinutes(15);
        }
    }
    

    
   
    // ===== UI Update Methods =====
    /** Updates the UI to reflect number of available spots. */
    public void updateAvailableSpots(int spots) {
        if (lblAvailableSpots != null) {
            lblAvailableSpots.setText("Available Spots: " + spots);
            
            // Update UI based on availability
            boolean canReserve = spots >= (10 * 0.4); // 40% rule
            if (btnMakeReservation != null) {
                btnMakeReservation.setDisable(!canReserve);
            }
            if (lblReservationStatus != null && !canReserve) {
                lblReservationStatus.setText("Reservations unavailable (less than 40% spots free)");
            }
        }
    }
    
    /** Replaces parking history with a new list of records. */
    public void updateParkingHistory(ObservableList<ParkingOrder> history) {
        this.parkingHistory.clear();
        this.parkingHistory.addAll(history);
    }
    
    // ===== Utility Methods =====
    /** Displays an information alert with given title and content. */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    /** Displays an error alert with given title and content. */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}