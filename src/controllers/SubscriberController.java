package controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import client.BParkClientApp;
import entities.Message;
import entities.Message.MessageType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;


public class SubscriberController implements Initializable {
    
    // Main menu buttons
    @FXML private Button btnCheckAvailability;
    @FXML private Button btnEnterParking;
    @FXML private Button btnExitParking;
    @FXML private Button btnMakeReservation;
    @FXML private Button btnViewHistory;
    @FXML private Button btnLostCode;
    @FXML private Button btnUpdateProfile;
    @FXML private Button btnLogout;
    
    
    // Parking entry/exit controls
    @FXML private TextField txtParkingCode;
    @FXML private TextField txtSubscriberCode;
    @FXML private Label lblAvailableSpots;
    @FXML private Label lblUserInfo;
    
    // Reservation controls
    @FXML private DatePicker datePickerReservation;
    @FXML private ComboBox<String> comboTimeSlot;
    @FXML private Label lblReservationStatus;
    
    
    // Profile update
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    
    // Current view container
    @FXML private VBox mainContent;
    
    private static boolean manualCheckRequested = false;
    
    public static void setManualCheckRequested(boolean value) {
        manualCheckRequested = value;
    }
    
    public void setUserName(String userName) {
        lblUserInfo.setText("User: " + userName);
    }
    
  
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        if (BParkClientApp.getCurrentUser() != null) {
            setUserName(BParkClientApp.getCurrentUser());
        }
       
    }
    
    private void setupUI() {
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
        
    }
    

    
    // ===== Action Handlers =====
    
    @FXML
    private void handleShowAvailableSpots() {
    	setManualCheckRequested(true);
        Message checkMsg = new Message(Message.MessageType.CHECK_PARKING_AVAILABILITY, null);
        BParkClientApp.sendMessage(checkMsg);
    }
    
    @FXML
    private void checkParkingAvailability() {
        Message msg = new Message(MessageType.CHECK_PARKING_AVAILABILITY, null);
        BParkClientApp.sendMessage(msg);
    }
    
    @FXML
    private void handleImmediateParking() {
        String subscriberCode = BParkClientApp.getCurrentUser();
        if (subscriberCode != null && !subscriberCode.isEmpty()) {
            Message msg = new Message(MessageType.ENTER_PARKING, subscriberCode);
            BParkClientApp.sendMessage(msg);
        } else {
            showAlert("Error", "Subscriber code not found");
        }
    }
    
    @FXML
    private void handleExitParking() {
        String parkingCode = txtParkingCode.getText().trim();
        if (parkingCode.isEmpty()) {
            showAlert("Error", "Please enter your parking code");
            return;
        }
        
        Message msg = new Message(MessageType.EXIT_PARKING, parkingCode);
        BParkClientApp.sendMessage(msg);
    }
    
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
    
    @FXML
    private void handleActivateReservation() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Activate Reservation");
        dialog.setHeaderText("Enter your reservation code:");
        dialog.setContentText("Code:");
        
        dialog.showAndWait().ifPresent(code -> {
            if (!code.trim().isEmpty()) {
                String activationData = BParkClientApp.getCurrentUser() + "," + code;
                Message msg = new Message(MessageType.ACTIVATE_RESERVATION, activationData);
                BParkClientApp.sendMessage(msg);
            }
        });
    }
    
    @FXML
    private void handleCancelReservation() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancel Reservation");
        dialog.setHeaderText("Enter reservation code to cancel:");
        dialog.setContentText("Code:");
        
        dialog.showAndWait().ifPresent(code -> {
            if (!code.trim().isEmpty()) {
                String cancellationData = BParkClientApp.getCurrentUser() + "," + code;
                Message msg = new Message(MessageType.CANCEL_RESERVATION, cancellationData);
                BParkClientApp.sendMessage(msg);
            }
        });
    }
    
    @FXML
    private void handleViewHistory() {
        try {
            // Load the parking history view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/ParkingHistoryView.fxml"));
            Node historyView = loader.load();
            
            // Replace the main content with history view
            mainContent.getChildren().setAll(historyView);
            
            // The controller will automatically request data on initialization
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Failed to load parking history view");
        }
    }
    
    @FXML
    private void handleLostCode() {
        Message msg = new Message(MessageType.REQUEST_LOST_CODE, BParkClientApp.getCurrentUser());
        BParkClientApp.sendMessage(msg);
    }
    
    @FXML
    private void handleExtendParking() { //***
    	   try {
    	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/ExtendParkingView.fxml"));
    	        Node extendParkingView = loader.load();
    	        mainContent.getChildren().setAll(extendParkingView); 
    	    } catch (IOException e) {
    	        e.printStackTrace();
    	    }
    }
   


    
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
    
    @FXML
    private void showExitParkingView() {
        // Show exit parking section
        showAlert("Exit Parking", "Enter your parking code in the field below and click Exit");
    }
    
    @FXML
    private void showReservationView() {
        // Show reservation section
        showAlert("Make Reservation", "Select date and time for your reservation");
    }
    
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
    
   
    // ===== UI Update Methods =====
    
    public void updateAvailableSpots(int spots) {
        if (lblAvailableSpots != null) {
            lblAvailableSpots.setText("Available Spots: " + spots);
            
            // Update UI based on availability
            boolean canReserve = spots >= (100 * 0.4); // 40% rule
            if (lblReservationStatus != null && !canReserve) {
                lblReservationStatus.setText("Reservations unavailable (less than 40% spots free)");
            }
        }
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
}