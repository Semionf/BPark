package controllers;



import client.BParkClientApp;


import entities.Message;
import entities.Message.MessageType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;


/**
 * Controller responsible for managing the "Update Profile" screen of the application.
 * <p>
 * Allows subscribers to update their email, phone number, and car number.
 * This controller handles user interactions, performs validation, and sends
 * formatted messages to the server through {@link BParkClientApp}.
 */
public class UpdateProfileController{
	
	  /**
     * Singleton-static instance reference to this controller.
     * <p>
     * Used by external classes to update UI elements.
     */
	public static UpdateProfileController instance;
	
	 /**
     * Text field where the user can enters or updates their email address.
     */
    @FXML
    private TextField emailField;
    
    /**
     * Text field where the user can enters or updates their phone number.
     */
    @FXML
    private TextField phoneField;
    
    /**
     * Text field where the user can enters or updates their car number.
     */
    @FXML
    private TextField carNumberField;
    
    
    /**
     * Label used to display status messages.
     */
    @FXML
    private Label statusLabel;
    
    
    /**
     * Initializes the controller.
     * <p>
     * This method is called automatically after the FXML file is loaded.
     * It sets the static instance reference and sends a request to the server
     * to fetch the current subscriber's data using the logged-in user ID.
     */
    @FXML
    public void initialize() {
    	 instance = this;
    	 String userId = BParkClientApp.getCurrentUser();
    	 Message requestMsg = new Message(MessageType.REQUEST_SUBSCRIBER_DATA, userId);
         BParkClientApp.sendMessage(requestMsg);
    }
    

    
    /**
     * Handles the "Update" button click.
     * <p>
     * This method:
     * <ul>
     *     <li>Reads and trims user input from all fields.</li>
     *     <li>Uses prompt text values as fallback if fields are left empty.</li>
     *     <li>Ensures at least one field has content before sending a request.</li>
     *     <li>Sends the updated information to the server via {@link Message}.</li>
     *     <li>Displays a success or error message in {@code statusLabel}.</li>
     * </ul>
     */
    @FXML
    private void handleUpdate() {
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String carNumber = carNumberField.getText().trim();
        String userId = BParkClientApp.getCurrentUser();

        // Use prompt text as fallback if field is empty
        if (email.isEmpty()) email = emailField.getPromptText();
        if (phone.isEmpty()) phone = phoneField.getPromptText();
        if (carNumber.isEmpty()) carNumber = carNumberField.getPromptText();

        // If all fields are still empty, don't send
        if (email.isEmpty() && phone.isEmpty() && carNumber.isEmpty()) {
            statusLabel.setText("Please fill in at least one field.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // Format: userId,phone,email,carNumber
        String data = userId + "," + phone + "," + email + "," + carNumber;

        // Send the update
        Message msg = new Message(MessageType.UPDATE_SUBSCRIBER_INFO, data);
        BParkClientApp.sendMessage(msg);

        statusLabel.setText("Profile update sent.");
        statusLabel.setStyle("-fx-text-fill: green;");
    }
    
    
    /**
     * Sets prompt text for all input fields.
     * <p>
     * This is typically called by the response handler after retrieving the
     * current subscriber details from the server.
     *
     * @param email   The subscriber's email to show as a placeholder.
     * @param phone   The subscriber's phone number to show as a placeholder.
     * @param carNum  The subscriber's car number to show as a placeholder.
     */
    public void setFieldPrompts(String email, String phone, String carNum) {
        emailField.setPromptText(email);
        phoneField.setPromptText(phone);
        carNumberField.setPromptText(carNum);
    }

    /**
     * Sets the text of the email field.
     * <p>
     * Add public setters so the ClientMessageHandler can set fields
     *
     * @param email The email address to set in the text field.
     */
public void setEmail(String email) {
    emailField.setText(email);
}


/**
 * Sets the text of the phone field.
 *
 * @param phone The phone number to set in the text field.
 */
public void setPhone(String phone) {
    phoneField.setText(phone);
}


}

