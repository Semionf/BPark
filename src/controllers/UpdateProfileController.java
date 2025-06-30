package controllers;



import client.BParkClientApp;
import entities.Message;
import entities.Message.MessageType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class UpdateProfileController{
	
public static UpdateProfileController instance;
    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;
    
    @FXML
    private TextField carNumberField;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
    	 instance = this;
    	 String userId = BParkClientApp.getCurrentUser();
    	 Message requestMsg = new Message(MessageType.REQUEST_SUBSCRIBER_DATA, userId);
         BParkClientApp.sendMessage(requestMsg);
    }
    

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

        // ✅ Send the update
        Message msg = new Message(MessageType.UPDATE_SUBSCRIBER_INFO, data);
        BParkClientApp.sendMessage(msg);

        statusLabel.setText("Profile update sent.");
        statusLabel.setStyle("-fx-text-fill: green;");
    }
    
    

    public void setFieldPrompts(String email, String phone, String carNum) {
        emailField.setPromptText(email);
        phoneField.setPromptText(phone);
        carNumberField.setPromptText(carNum);
    }

// Add public setters so the ClientMessageHandler can set fields
public void setEmail(String email) {
    emailField.setText(email);
}

public void setPhone(String phone) {
    phoneField.setText(phone);
}


}

