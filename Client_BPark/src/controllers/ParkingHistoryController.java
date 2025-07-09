package controllers;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;

import common.ParkingOrder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * Controller for the Parking History window. Displays a table of all past and
 * current parking sessions for the user, including filtering by status,
 * color-coded display, and session statistics.
 */
public class ParkingHistoryController implements Initializable {

	/**
	 * Default constructor for ParkingHistoryController. Required for JavaFX
	 * controller instantiation via FXML.
	 */
	public ParkingHistoryController() {
		// No initialization required here
	}

	// ===================== UI Controls =====================

	/** Label showing the current user's name */
	@FXML
	private Label lblUserName;

	/** Label displaying the total number of parking sessions */
	@FXML
	private Label lblTotalSessions;

	/** Label displaying the number of currently active sessions */
	@FXML
	private Label lblActiveSessions;

	/** Label displaying the number of completed parking sessions */
	@FXML
	private Label lblCompletedSessions;

	/** Label for showing error/status messages */
	@FXML
	private Label lblStatus;

	/** ComboBox for filtering parking sessions by their status */
	@FXML
	private ComboBox<String> comboStatusFilter;

	// ===================== Table and Columns =====================

	/** TableView displaying the user's parking history */
	@FXML
	private TableView<ParkingOrder> tableHistory;

	/** Column for displaying the unique order code of each session */
	@FXML
	private TableColumn<ParkingOrder, String> colCode;

	/** Column for displaying the date of the parking session */
	@FXML
	private TableColumn<ParkingOrder, String> colDate;

	/** Column for displaying the time of entry */
	@FXML
	private TableColumn<ParkingOrder, String> colEntryTime;

	/** Column for displaying the time of exit */
	@FXML
	private TableColumn<ParkingOrder, String> colExitTime;

	/** Column for displaying the parking spot number */
	@FXML
	private TableColumn<ParkingOrder, String> colSpotNumber;

	/** Column for displaying the type of parking order (immediate/reserved) */
	@FXML
	private TableColumn<ParkingOrder, String> colOrderType;

	/** Column for displaying the current status of the parking session */
	@FXML
	private TableColumn<ParkingOrder, String> colStatus;

	/** Column for displaying the duration of the parking session */
	@FXML
	private TableColumn<ParkingOrder, String> colDuration;

	/** Column indicating whether the session ended late */
	@FXML
	private TableColumn<ParkingOrder, String> colLate;

	/** Column showing whether the parking session was extended */
	@FXML
	private TableColumn<ParkingOrder, String> colExtended;

	// Data

	/** List containing all historical parking records */
	private ObservableList<ParkingOrder> allHistory = FXCollections.observableArrayList();
	/** List containing filtered parking records based on status */
	private ObservableList<ParkingOrder> filteredHistory = FXCollections.observableArrayList();

	/**
	 * Initializes the controller: sets up the table and the status filter combo
	 * box.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setupTable();
		setupFilter();
		updateStatus("Ready");
	}

	/**
	 * Configures the table columns, cell factories, value formatters, and color
	 * coding.
	 */
	private void setupTable() {
		// Configure basic columns
		colCode.setCellValueFactory(new PropertyValueFactory<>("parkingCode"));
		colSpotNumber.setCellValueFactory(new PropertyValueFactory<>("spotNumber"));

		// Order Type with custom formatting
		colOrderType.setCellValueFactory(cellData -> {
			ParkingOrder order = cellData.getValue();
			String type = order.getOrderType();
			String status = order.getStatus();

			// Map database values to display values
			if ("yes".equalsIgnoreCase(type)) {
				// "yes" in IsOrderedEnum means it was a preorder/reservation
				return new javafx.beans.property.SimpleStringProperty("Preorder");
			} else if ("no".equalsIgnoreCase(type)) {
				// "no" in IsOrderedEnum means it was immediate parking
				return new javafx.beans.property.SimpleStringProperty("Immediate");
			}
			// Fallback for other potential values
			else if ("PREORDER".equalsIgnoreCase(type) || "PRE_ORDER".equalsIgnoreCase(type)
					|| "preorder".equalsIgnoreCase(status)) {
				return new javafx.beans.property.SimpleStringProperty("Preorder");
			} else if ("IMMEDIATE".equalsIgnoreCase(type) || "REGULAR".equalsIgnoreCase(type)) {
				return new javafx.beans.property.SimpleStringProperty("Immediate");
			}

			// Default fallback
			return new javafx.beans.property.SimpleStringProperty("Unknown");
		});

		// Date column - For cancelled and preorder: show estimated start date, others:
		// show actual entry date
		colDate.setCellValueFactory(cellData -> {
			ParkingOrder order = cellData.getValue();
			String status = order.getStatus();

			// For cancelled and preorder reservations, show estimated start date
			if ("cancelled".equalsIgnoreCase(status) || "canceled".equalsIgnoreCase(status)
					|| "preorder".equalsIgnoreCase(status)) {
				if (order.getEstimatedStartTime() != null) {
					String dateStr = order.getEstimatedStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
					return new javafx.beans.property.SimpleStringProperty(dateStr);
				}
			}

			// For all other statuses, show actual entry date if available
			if (order.getEntryTime() != null) {
				String dateStr = order.getEntryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				return new javafx.beans.property.SimpleStringProperty(dateStr);
			}

			return new javafx.beans.property.SimpleStringProperty("N/A");
		});

		// Entry Time (actual) - For preorder: show estimated start time, others: show
		// actual entry time
		colEntryTime.setCellValueFactory(cellData -> {
			ParkingOrder order = cellData.getValue();
			String status = order.getStatus();

			// For preorder status, show estimated start time
			if ("preorder".equalsIgnoreCase(status)) {
				if (order.getEstimatedStartTime() != null) {
					String formatted = order.getEstimatedStartTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
					return new javafx.beans.property.SimpleStringProperty(formatted);
				}
			}

			// For all other statuses, show actual entry time if available
			if (order.getEntryTime() != null) {
				String formatted = order.getEntryTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
				return new javafx.beans.property.SimpleStringProperty(formatted);
			}

			return new javafx.beans.property.SimpleStringProperty("N/A");
		});

		// Exit Time with custom logic based on status
		colExitTime.setCellValueFactory(cellData -> {
			ParkingOrder order = cellData.getValue();
			String status = order.getStatus();

			if ("Active".equalsIgnoreCase(status)) {
				return new javafx.beans.property.SimpleStringProperty("Still Parked");
			} else if ("Canceled".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)) {
				return new javafx.beans.property.SimpleStringProperty("Canceled");
			} else if ("Finished".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
				if (order.getExitTime() != null) {
					String formatted = order.getExitTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
					return new javafx.beans.property.SimpleStringProperty(formatted);
				}
			}

			return new javafx.beans.property.SimpleStringProperty("N/A");
		});

		// Duration with custom logic
		colDuration.setCellValueFactory(cellData -> {
			ParkingOrder order = cellData.getValue();
			String status = order.getStatus();

			if ("Canceled".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)
					|| "preorder".equalsIgnoreCase(status)) {
				return new javafx.beans.property.SimpleStringProperty("0 hours 0 minutes");
			} else if ("Active".equalsIgnoreCase(status) || "Finished".equalsIgnoreCase(status)
					|| "Completed".equalsIgnoreCase(status)) {
				// Calculate duration manually
				if (order.getEntryTime() != null) {
					LocalDateTime startTime = order.getEntryTime();
					LocalDateTime endTime = order.getExitTime();

					// For active sessions, use current time as end time
					if (endTime == null || "Active".equalsIgnoreCase(status)) {
						endTime = LocalDateTime.now();
					}

					// Calculate duration
					long totalMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
					long hours = totalMinutes / 60;
					long minutes = totalMinutes % 60;

					return new javafx.beans.property.SimpleStringProperty(hours + " hours and " + minutes + " minutes");
				}
			}

			return new javafx.beans.property.SimpleStringProperty("N/A");
		});

		// Late column
		colLate.setCellValueFactory(cellData -> {
			ParkingOrder order = cellData.getValue();
			// Assuming ParkingOrder has an isLate() method or late property
			boolean isLate = order.isLate(); // You may need to add this method to ParkingOrder
			return new javafx.beans.property.SimpleStringProperty(isLate ? "Yes" : "No");
		});

		// Extended column
		colExtended.setCellValueFactory(cellData -> {
			ParkingOrder order = cellData.getValue();
			// Assuming ParkingOrder has an isExtended() method or extended property
			boolean isExtended = order.isExtended(); // You may need to add this method to ParkingOrder
			return new javafx.beans.property.SimpleStringProperty(isExtended ? "Yes" : "No");
		});

		// Set up color coding for Status column
		colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
		colStatus.setCellFactory(column -> {
			return new TableCell<ParkingOrder, String>() {
				@Override
				protected void updateItem(String status, boolean empty) {
					super.updateItem(status, empty);

					if (empty || status == null) {
						setText(null);
						setStyle("");
					} else {
						setText(status);

						// Status Color Coding: Preorder (Purple), Active (Green), Canceled (Red),
						// Finished (Blue)
						switch (status.toLowerCase()) {
						case "preorder":
							setStyle("-fx-text-fill: purple;");
							break;
						case "active":
							setStyle("-fx-text-fill: green;");
							break;
						case "canceled":
						case "cancelled":
							setStyle("-fx-text-fill: red;");
							break;
						case "finished":
						case "completed":
							setStyle("-fx-text-fill: blue;");
							break;
						default:
							setStyle("-fx-text-fill: black;");
							break;
						}
					}
				}
			};
		});

		// Set up color coding for Late column
		colLate.setCellFactory(column -> {
			return new TableCell<ParkingOrder, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (empty || item == null) {
						setText(null);
						setStyle("");
					} else {
						setText(item);

						// Late Column: Yes (Red), No (Black)
						if ("Yes".equals(item)) {
							setStyle("-fx-text-fill: red;");
						} else {
							setStyle("-fx-text-fill: black;");
						}
					}
				}
			};
		});

		// Set up color coding for Extended column
		colExtended.setCellFactory(column -> {
			return new TableCell<ParkingOrder, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (empty || item == null) {
						setText(null);
						setStyle("");
					} else {
						setText(item);

						// Extended Column: Yes (Blue), No (Black)
						if ("Yes".equals(item)) {
							setStyle("-fx-text-fill: blue;");
						} else {
							setStyle("-fx-text-fill: black;");
						}
					}
				}
			};
		});

		// Special formatting for Exit Time to show "Canceled" in red
		colExitTime.setCellFactory(column -> {
			return new TableCell<ParkingOrder, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (empty || item == null) {
						setText(null);
						setStyle("");
					} else {
						setText(item);

						// Show "Canceled" in red text
						if ("Canceled".equals(item)) {
							setStyle("-fx-text-fill: red;");
						} else {
							setStyle("-fx-text-fill: black;");
						}
					}
				}
			};
		});

		// Bind the filtered list to the table
		tableHistory.setItems(filteredHistory);
	}

	/**
	 * Set up the status filter combo box
	 */
	private void setupFilter() {
		comboStatusFilter.getItems().addAll("All Sessions", "Active", "Completed", "Preorder", "Canceled");
		comboStatusFilter.setValue("All Sessions");
	}

	/**
	 * Sets the user name label on the UI thread.
	 *
	 * @param userName The name of the current user to display.
	 */

	public void setUserName(String userName) {
		Platform.runLater(() -> {
			lblUserName.setText("User: " + userName);
		});
	}

	/**
	 * Loads the parking history data into the table and updates UI elements.
	 *
	 * @param history A list of parking records to display.
	 */

	public void loadHistory(ArrayList<ParkingOrder> history) {
		Platform.runLater(() -> {
			allHistory.clear();
			allHistory.addAll(history);
			applyFilter();
			updateStatistics();
			updateStatus("Loaded " + history.size() + " parking records");
		});
	}

	/**
	 * Apply the selected filter to the data
	 */
	@FXML
	private void handleFilterChange() {
		applyFilter();
	}

	/**
	 * Apply the current filter selection
	 */
	private void applyFilter() {
		String selectedFilter = comboStatusFilter.getValue();
		filteredHistory.clear();

		switch (selectedFilter) {
		case "Active":
			allHistory.stream().filter(order -> "active".equalsIgnoreCase(order.getStatus()))
					.forEach(filteredHistory::add);
			break;
		case "Completed":
			allHistory.stream().filter(order -> "finished".equalsIgnoreCase(order.getStatus())
					|| "completed".equalsIgnoreCase(order.getStatus())).forEach(filteredHistory::add);
			break;
		case "Preorder":
			allHistory.stream().filter(order -> "preorder".equalsIgnoreCase(order.getStatus()))
					.forEach(filteredHistory::add);
			break;
		case "Canceled":
			allHistory.stream().filter(order -> "cancelled".equalsIgnoreCase(order.getStatus())
					|| "canceled".equalsIgnoreCase(order.getStatus())).forEach(filteredHistory::add);
			break;
		default: // "All Sessions"
			filteredHistory.addAll(allHistory);
			break;
		}
	}

	/**
	 * Updates the labels showing total, active, and completed sessions.
	 */
	private void updateStatistics() {
		int total = allHistory.size();
		int active = (int) allHistory.stream().filter(order -> "active".equalsIgnoreCase(order.getStatus())).count();
		int completed = (int) allHistory.stream().filter(order -> "finished".equalsIgnoreCase(order.getStatus())
				|| "completed".equalsIgnoreCase(order.getStatus())).count();

		lblTotalSessions.setText(String.valueOf(total));
		lblActiveSessions.setText(String.valueOf(active));
		lblCompletedSessions.setText(String.valueOf(completed));
	}

	/**
	 * Updates the status label at the bottom of the window.
	 * 
	 * @param message Status message to display.
	 */
	private void updateStatus(String message) {
		Platform.runLater(() -> {
			lblStatus.setText(message);
		});
	}

	/**
	 * Handles the "Refresh" button click to reload the parking history from the
	 * server.
	 */
	@FXML
	private void handleRefresh() {
		updateStatus("Refreshing...");
		// Send request for updated parking history
		client.BParkClientScenes.sendMessage(new common.Message(common.Message.MessageType.GET_PARKING_HISTORY,
				client.BParkClientScenes.getCurrentUser()));
	}

	/**
	 * Handles the "Close" button click to close the current window.
	 */
	@FXML
	private void handleClose() {
		Stage stage = (Stage) lblUserName.getScene().getWindow();
		stage.close();
	}

	/**
	 * Checks if the window associated with this controller is currently visible.
	 * 
	 * @return true if the window is showing, false otherwise
	 */
	public boolean isWindowShowing() {
		try {
			Stage stage = (Stage) lblUserName.getScene().getWindow();
			return stage.isShowing();
		} catch (Exception e) {
			return false;
		}
	}
}