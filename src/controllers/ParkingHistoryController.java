package controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;

import client.BParkClientApp;
import entities.Message;
import entities.Message.MessageType;
import entities.ParkingOrder;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ParkingHistoryController implements Initializable {
    
    // Table and columns
    @FXML private TableView<ParkingOrder> historyTable;
    @FXML private TableColumn<ParkingOrder, String> colCode;
    @FXML private TableColumn<ParkingOrder, String> colDate;
    @FXML private TableColumn<ParkingOrder, String> colEntryTime;
    @FXML private TableColumn<ParkingOrder, String> colExitTime;
    @FXML private TableColumn<ParkingOrder, String> colDuration;
    @FXML private TableColumn<ParkingOrder, String> colSpot;
    @FXML private TableColumn<ParkingOrder, String> colType;
    @FXML private TableColumn<ParkingOrder, String> colStatus;
    @FXML private TableColumn<ParkingOrder, String> colLate;
    @FXML private TableColumn<ParkingOrder, String> colExtended;
    
    // Summary labels
    @FXML private Label lblTotalSessions;
    @FXML private Label lblReservations;
    @FXML private Label lblLateExits;
    @FXML private Label lblExtended;
    @FXML private Label lblLastUpdate;
    
    // Filter controls
    @FXML private ComboBox<String> filterCombo;
    @FXML private ComboBox<String> sortCombo;
    
    private ObservableList<ParkingOrder> allHistory = FXCollections.observableArrayList();
    private FilteredList<ParkingOrder> filteredData;
    private SortedList<ParkingOrder> sortedData;
    
    private static ParkingHistoryController instance;
    
    public ParkingHistoryController() {
        instance = this;
    }
    
    public static ParkingHistoryController getInstance() {
        return instance;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupFiltersAndSorting();
        loadParkingHistory();
    }
    
    private void setupTableColumns() {
        // Set up column value factories
        colCode.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getParkingCode()));
            
        colDate.setCellValueFactory(cellData -> {
            LocalDateTime entryTime = cellData.getValue().getEntryTime();
            if (entryTime != null) {
                return new SimpleStringProperty(
                    entryTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                );
            }
            return new SimpleStringProperty("N/A");
        });
        
        // Add comparator for date column
        colDate.setComparator((date1, date2) -> {
            try {
                LocalDate d1 = LocalDate.parse(date1, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                LocalDate d2 = LocalDate.parse(date2, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return d1.compareTo(d2);
            } catch (Exception e) {
                return date1.compareTo(date2);
            }
        });
        
        colEntryTime.setCellValueFactory(cellData -> {
            LocalDateTime entryTime = cellData.getValue().getEntryTime();
            if (entryTime != null) {
                return new SimpleStringProperty(
                    entryTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                );
            }
            return new SimpleStringProperty("Not started");
        });
        
        colExitTime.setCellValueFactory(cellData -> {
            LocalDateTime exitTime = cellData.getValue().getExitTime();
            if (exitTime != null) {
                return new SimpleStringProperty(
                    exitTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                );
            }
            return new SimpleStringProperty(
                cellData.getValue().getStatus().equals("active") ? "Still active" : "N/A"
            );
        });
        
        colDuration.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getParkingDurationFormatted()));
        
        // Add comparator for duration column (sort by actual minutes, not string)
        colDuration.setComparator((duration1, duration2) -> {
            // Extract numbers from "X hours, Y minutes" format
            int minutes1 = extractMinutesFromDuration(duration1);
            int minutes2 = extractMinutesFromDuration(duration2);
            return Integer.compare(minutes1, minutes2);
        });
            
        colSpot.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getSpotNumber()));
            
        colType.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getOrderType();
            return new SimpleStringProperty(
                "yes".equals(type) ? "Reservation" : "Immediate"
            );
        });
        
        colStatus.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus();
            // Format status for display
            switch (status) {
                case "preorder":
                    return new SimpleStringProperty("Pre-order");
                case "active":
                    return new SimpleStringProperty("Active");
                case "finished":
                    return new SimpleStringProperty("Completed");
                case "cancelled":
                    return new SimpleStringProperty("Cancelled");
                default:
                    return new SimpleStringProperty(status);
            }
        });
        
        colLate.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isLate() ? "Yes" : "No"));
            
        colExtended.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isExtended() ? "Yes" : "No"));
        
        // Add cell factories for styling
        colStatus.setCellFactory(column -> new TableCell<ParkingOrder, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Apply different colors based on status
                    switch (item) {
                        case "Active":
                            setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
                            break;
                        case "Cancelled":
                            setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                            break;
                        case "Pre-order":
                            setStyle("-fx-text-fill: #3498DB; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
        
        colLate.setCellFactory(column -> new TableCell<ParkingOrder, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Yes".equals(item)) {
                        setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }
    
    private void setupFiltersAndSorting() {
        // Populate filter combo items
        filterCombo.getItems().addAll(
            "All",
            "Active", 
            "Completed",
            "Cancelled",
            "Reservations Only",
            "Immediate Only"
        );
        
        // Populate sort combo items
        sortCombo.getItems().addAll(
            "Date (Newest First)",
            "Date (Oldest First)",
            "Duration",
            "Status"
        );
        
        // Initialize filter and sort combos
        filterCombo.setValue("All");
        sortCombo.setValue("Date (Newest First)");
        
        // Create filtered list
        filteredData = new FilteredList<>(allHistory, p -> true);
        
        // Add listener to filter combo
        filterCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(order -> {
                if (newValue == null || newValue.equals("All")) {
                    return true;
                }
                
                switch (newValue) {
                    case "Active":
                        return "active".equals(order.getStatus());
                    case "Completed":
                        return "finished".equals(order.getStatus());
                    case "Cancelled":
                        return "cancelled".equals(order.getStatus());
                    case "Reservations Only":
                        return "yes".equals(order.getOrderType());
                    case "Immediate Only":
                        return "no".equals(order.getOrderType());
                    default:
                        return true;
                }
            });
            updateSummary();
        });
        
        // Create sorted list
        sortedData = new SortedList<>(filteredData);
        
        // Bind the SortedList comparator to the TableView comparator
        sortedData.comparatorProperty().bind(historyTable.comparatorProperty());
        
        // Add listener to sort combo
        sortCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Clear existing table sort
                historyTable.getSortOrder().clear();
                
                switch (newValue) {
                    case "Date (Newest First)":
                        colDate.setSortType(TableColumn.SortType.DESCENDING);
                        historyTable.getSortOrder().add(colDate);
                        break;
                    case "Date (Oldest First)":
                        colDate.setSortType(TableColumn.SortType.ASCENDING);
                        historyTable.getSortOrder().add(colDate);
                        break;
                    case "Duration":
                        colDuration.setSortType(TableColumn.SortType.DESCENDING);
                        historyTable.getSortOrder().add(colDuration);
                        break;
                    case "Status":
                        colStatus.setSortType(TableColumn.SortType.ASCENDING);
                        historyTable.getSortOrder().add(colStatus);
                        break;
                }
            }
        });
        
        // Bind sorted data to table
        historyTable.setItems(sortedData);
        
        // Set initial sort order (newest first)
        colDate.setSortType(TableColumn.SortType.DESCENDING);
        historyTable.getSortOrder().add(colDate);
    }
    
    @FXML
    private void loadParkingHistory() {
        // Request parking history from server
        Message msg = new Message(MessageType.GET_PARKING_HISTORY, BParkClientApp.getCurrentUser());
        BParkClientApp.sendMessage(msg);
    }
    
    @FXML
    private void refreshHistory() {
        loadParkingHistory();
    }
    
    @FXML
    private void goBack() {
        try {
            // Load the subscriber main view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/SubscriberMain.fxml"));
            Node subscriberView = loader.load();
            
            // Get the parent container and replace content
            historyTable.getScene().setRoot((javafx.scene.Parent) subscriberView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public void updateParkingHistory(ArrayList<ParkingOrder> history) {
        allHistory.clear();
        allHistory.addAll(history);
        updateSummary();
        updateLastRefreshTime();
    }
    
    private void updateSummary() {
        // Calculate summary statistics
        int totalSessions = filteredData.size();
        long reservations = filteredData.stream()
            .filter(o -> "yes".equals(o.getOrderType()))
            .count();
        long lateExits = filteredData.stream()
            .filter(ParkingOrder::isLate)
            .count();
        long extended = filteredData.stream()
            .filter(ParkingOrder::isExtended)
            .count();
        
        // Update labels
        lblTotalSessions.setText(String.valueOf(totalSessions));
        lblReservations.setText(String.valueOf(reservations));
        lblLateExits.setText(String.valueOf(lateExits));
        lblExtended.setText(String.valueOf(extended));
    }
    
    private void updateLastRefreshTime() {
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
        lblLastUpdate.setText("Last updated: " + timestamp);
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Helper method to extract total minutes from duration string
     */
    private int extractMinutesFromDuration(String duration) {
        try {
            // Format is "X hours, Y minutes"
            String[] parts = duration.split(",");
            int hours = 0;
            int minutes = 0;
            
            for (String part : parts) {
                part = part.trim();
                if (part.contains("hour")) {
                    hours = Integer.parseInt(part.split(" ")[0]);
                } else if (part.contains("minute")) {
                    minutes = Integer.parseInt(part.split(" ")[0]);
                }
            }
            
            return hours * 60 + minutes;
        } catch (Exception e) {
            return 0;
        }
    }
}