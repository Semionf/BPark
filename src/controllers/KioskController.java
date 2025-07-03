package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.geometry.Insets;

import java.util.Optional;

import entities.Message;
import static entities.Message.MessageType.*;
import client.BParkKioskApp;

public class KioskController {

    @FXML
    private Button btnLoginByID;

    @FXML
    private Button btnLoginByRF;

    private static Stage mainStage;

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

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

            usernameField.textProperty().addListener((obs, oldVal, newVal) ->
                loginButton.setDisable(newVal.trim().isEmpty() || userIDField.getText().trim().isEmpty())
            );
            userIDField.textProperty().addListener((obs, oldVal, newVal) ->
                loginButton.setDisable(newVal.trim().isEmpty() || usernameField.getText().trim().isEmpty())
            );

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
                    BParkKioskApp.sendMessage(msg);
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "User ID must be numeric.");
                }
            });
        });
    }

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
                BParkKioskApp.sendMessage(msg);
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid numeric User ID.");
            }
        });
    }

    public static void handleKioskLoginResult(Object content) {
        if (content instanceof String response) {
            if (response.equals("FULL")) {
                showAlertStatic("Parking Full", "Parking is full, try later.");
            } else if (!response.isEmpty()) {
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void showAlertStatic(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

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
                mainStage.setScene(dashboardScene);
            } catch (Exception e) {
                e.printStackTrace();
                showAlertStatic("Error", "Failed to load dashboard screen.");
            }
        });
    }
}