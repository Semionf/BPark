package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import entities.Message;
import static entities.Message.MessageType.*;

import java.util.Optional;

import client.BParkKioskApp;

public class KioskDashboardController {

    private static String loggedInUsername;
    private static int loggedInUserID;

    public static void setLoggedInUser(String username, int userID) {
        loggedInUsername = username;
        loggedInUserID = userID;
    }

    public static void resetLoggedInUser() {
        loggedInUsername = null;
        loggedInUserID = 0;
    }

    @FXML
    private void handleEnterParking(ActionEvent event) {
        Message msg = new Message(ENTER_PARKING_KIOSK, loggedInUserID);
        BParkKioskApp.sendMessage(msg);
    }

    @FXML
    private void handleRetrieveCar(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Retrieve Car");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter your parking code:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(codeStr -> {
            try {
                int parkingInfoID = Integer.parseInt(codeStr);
                Message msg = new Message(RETRIEVE_CAR_KIOSK, parkingInfoID);
                BParkKioskApp.sendMessage(msg);
            } catch (NumberFormatException e) {
                showInfo("Invalid Input", "Parking code must be numeric.");
            }
        });
    }

    @FXML
    private void handleForgotCode(ActionEvent event) {
        Message msg = new Message(FORGOT_CODE_KIOSK, loggedInUserID);
        BParkKioskApp.sendMessage(msg);
    }

    @FXML
    private void handleActivateReservation(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Activate Reservation");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter your reservation code:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(codeStr -> {
            try {
                int parkingInfoID = Integer.parseInt(codeStr);
                Message msg = new Message(ACTIVATE_RESERVATION_KIOSK, parkingInfoID);
                BParkKioskApp.sendMessage(msg);
            } catch (NumberFormatException e) {
                showInfo("Invalid Input", "Reservation code must be numeric.");
            }
        });
    }
    
    @FXML
    private void handleExit(ActionEvent event) {
        try {
            resetLoggedInUser();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/KioskMain.fxml"));
            Parent mainRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(mainRoot));
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Error", "Could not return to main screen.");
        }
    }

    private void showInfo(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}