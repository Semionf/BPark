package controllers;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;

import client.BParkClientApp;
import entities.Message;
import entities.Message.MessageType;
import entities.ParkingOrder;
import entities.ParkingReport;
import entities.ParkingSubscriber;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;

public class ManagerController implements Initializable {

	// Dashboard Labels
	@FXML
	private Label lblTotalSpots;
	@FXML
	private Label lblOccupied;
	@FXML
	private Label lblAvailable;
	@FXML
	private Label lblReservations;
	@FXML
	private Label lblSystemStatus;
	@FXML
	private Label lblManagerInfo;
	@FXML
	private Label lblLastUpdate;
	@FXML
	private Label lbltotalusedreservation;
	@FXML
	private Label lblimidiateParkings;

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

	private ObservableList<ParkingOrder> activeParkings = FXCollections.observableArrayList();

	// Charts
	@FXML
	private LineChart<String, Number> occupancyChart;
	@FXML
	private PieChart parkingTypesChart;
	@FXML
	private BarChart<String, Number> parkingTimeChart;
	@FXML
	private AreaChart<String, Number> subscriberActivityChart;

	// Report Controls
	@FXML
	private ComboBox<String> comboReportType;
	@FXML
	private DatePicker datePickerFrom;
	@FXML
	private DatePicker datePickerTo;

	// --- Parking Report ---
	@FXML
	private BarChart<String, Number> chartTotalParkingTimePerDay;
	@FXML
	private BarChart<String, Number> chartHourlyDistribution;
	@FXML
	private PieChart chartExtensionsPercentage;
	@FXML
	private BarChart<String, Number> chartLateExitsByHour;
	@FXML
	private PieChart chartLateSubscribersRate;
	@FXML
	private Label lblTotalExtensions;
	@FXML
	private Label lblTotalLateExits;
	@FXML
	private Label lblTotalMonthHours;

	@FXML
	private Label lblExtensionsPercent;
	@FXML
	private Label lblLateSubscriberPercent;

	// --- Subscribers Report ---
	@FXML
	private BarChart<String, Number> chartSubscribersPerDay;
	@FXML
	private PieChart chartReservationUsage;
	@FXML
	private Label lblTotalReservations;
	@FXML
	private Label lblCancelledReservations;
	@FXML
	private Label lblPreOrderReservations;
	@FXML
	private Label lblUsedReservations;

	// Report Labels
	@FXML
	private Label lblAvgDuration;
	@FXML
	private Label lblTotalParkings;
	@FXML
	private Label lblLateExits;
	@FXML
	private Label lblExtensions;
	@FXML
	private Label lblActiveSubscribers;
	@FXML
	private Label lblTotalOrders;
	@FXML
	private Label lblReservationCount;
	@FXML
	private Label lblCancelled;

	// System Management
	@FXML
	private Label lblAutoCancelStatus;
	@FXML
	private ComboBox<String> comboMonth;
	@FXML
	private ComboBox<String> comboYear;
	@FXML
	private Label lblTotalUsers;
	@FXML
	private Label lblPeakHours;
	@FXML
	private Label lblAvgDailyUsage;

	// Attendant Controller (if using include)
	@FXML
	private AttendantController attendantController;

	private Timeline refreshTimeline;
	private ObservableList<ParkingReport> currentReports = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		BParkClientApp.setManagerController(this);
		setupUI();
		loadInitialData();
		startAutoRefresh();
		loadSubscribers();

	}

	private void setupUI() {
		setupTableColumns();

		// Initialize report types
		if (comboReportType != null) {
			comboReportType.getItems().addAll("Parking Time Report", "Subscriber Status Report", "All Reports");
			comboReportType.setValue("All Reports");
		}

		// Initialize month/year combos for monthly reports
		if (comboMonth != null && comboYear != null) {
			comboMonth.getItems().addAll("January", "February", "March", "April", "May", "June", "July", "August",
					"September", "October", "November", "December");

			int currentYear = LocalDate.now().getYear();
			for (int year = currentYear - 2; year <= currentYear; year++) {
				comboYear.getItems().add(String.valueOf(year));
			}

			comboMonth.setValue(LocalDate.now().getMonth().toString());
			comboYear.setValue(String.valueOf(currentYear));
		}

		// Set date picker defaults
		if (datePickerFrom != null && datePickerTo != null) {
			datePickerTo.setValue(LocalDate.now());
			datePickerFrom.setValue(LocalDate.now().minusDays(30));
		}

		// Initialize charts
		initializeCharts();

		// Set manager info
		if (lblManagerInfo != null) {
			lblManagerInfo.setText("Manager: " + BParkClientApp.getCurrentUser());
		}

		if (tableSubscribers != null) {
			colSubName.setCellValueFactory(
					cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFirstName()));
			colSubPhone.setCellValueFactory(
					cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPhoneNumber()));
			colSubEmail.setCellValueFactory(
					cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
			colSubCar.setCellValueFactory(
					cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCarNumber()));
			colSubUsername.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
					cellData.getValue().getSubscriberCode()));
		}

	}

	private void initializeCharts() {
		// Initialize occupancy chart with sample data
		if (occupancyChart != null) {
			XYChart.Series<String, Number> series = new XYChart.Series<>();
			series.setName("Occupancy");

			// Add hourly data points
			for (int hour = 6; hour <= 22; hour++) {
				series.getData().add(new XYChart.Data<>(hour + ":00", 0));
			}

			occupancyChart.getData().add(series);
			occupancyChart.setCreateSymbols(false);
		}

		// Initialize parking types pie chart
		if (parkingTypesChart != null) {
			ObservableList<PieChart.Data> pieChartData = FXCollections
					.observableArrayList(new PieChart.Data("Immediate", 60), new PieChart.Data("Reserved", 40));
			parkingTypesChart.setData(pieChartData);
		}
	}

	private void loadInitialData() {
		// Load parking availability
		checkParkingStatus();

		// Load initial reports
		loadReports("ALL");

		// Update timestamp
		updateLastRefreshTime();
	}

	private void startAutoRefresh() {
		// Refresh dashboard every 30 seconds
		refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
			checkParkingStatus();
			updateLastRefreshTime();
		}));
		refreshTimeline.setCycleCount(Timeline.INDEFINITE);
		refreshTimeline.play();
	}

	// ===== Action Handlers =====

	@FXML
	private void handleGenerateReports() {
		loadReports("ALL");
	}

	@FXML
	private void handleGenerateSelectedReport() {
		String reportType = comboReportType.getValue();
		LocalDate fromDate = datePickerFrom.getValue();
		LocalDate toDate = datePickerTo.getValue();

		if (reportType == null) {
			showAlert("Error", "Please select a report type");
			return;
		}

		String type = reportType.contains("Time") ? "PARKING_TIME"
				: reportType.contains("Subscriber") ? "SUBSCRIBER_STATUS" : "ALL";

		loadReports(type);
	}

	@FXML
	private void handleGenerateMonthlyReports() {
		String month = comboMonth.getValue();
		String year = comboYear.getValue();

		if (month == null || year == null) {
			showAlert("Error", "Please select month and year");
			return;
		}

		// Convert month name to number
		int monthNum = comboMonth.getSelectionModel().getSelectedIndex() + 1;
		String monthYear = String.format("%s-%02d", year, monthNum);

		Message msg = new Message(MessageType.GENERATE_MONTHLY_REPORTS, monthYear);
		BParkClientApp.sendMessage(msg);
	}

	@FXML
	private void checkParkingStatus() {
//		Message msg = new Message(MessageType.CHECK_PARKING_AVAILABILITY, null);
//		BParkClientApp.sendMessage(msg);

		// Also get active parkings for statistics
		Message activeMsg = new Message(MessageType.GET_ACTIVE_PARKINGS, null);
		BParkClientApp.sendMessage(activeMsg);
	}

	private void loadReports(String type) {
		Message msg = new Message(MessageType.MANAGER_GET_REPORTS, type);
		BParkClientApp.sendMessage(msg);
	}

	// ===== UI Update Methods =====

	public void updateParkingStatus(int availableSpots) {
		Platform.runLater(() -> {
			int occupied = 10 - availableSpots;

			if (lblOccupied != null) {
				lblOccupied.setText(String.valueOf(occupied));
			}

			if (lblAvailable != null) {
				lblAvailable.setText(String.valueOf(availableSpots));
			}

			// Update system status based on availability
			if (lblSystemStatus != null) {
				if (availableSpots < 10) {
					lblSystemStatus.setText("System Status: Nearly Full");
					lblSystemStatus.setStyle("-fx-text-fill: #E74C3C;");
				} else if (availableSpots < 40) {
					lblSystemStatus.setText("System Status: Limited Availability");
					lblSystemStatus.setStyle("-fx-text-fill: #F39C12;");
				} else {
					lblSystemStatus.setText("System Status: Operational");
					lblSystemStatus.setStyle("-fx-text-fill: #27AE60;");
				}
			}

			updateOccupancyChart(occupied);
		});
	}

	public void updateReports(ArrayList<ParkingReport> reports) {
		Platform.runLater(() -> {
			currentReports.clear();
			currentReports.addAll(reports);

			for (ParkingReport report : reports) {
				if (report.getReportType().equals("PARKING_TIME")) {
					updateParkingTimeReport(report);
				} else if (report.getReportType().equals("SUBSCRIBER_STATUS")) {
					updateSubscriberStatusReport(report);
				}
			}
		});
	}

	private void updateParkingTimeReport(ParkingReport report) {

		lblTotalSpots.setText("10");
		lblOccupied.setText(report.getOccupied() + "");
		lblAvailable.setText(10 - report.getOccupied() + "");
		lblReservations.setText(report.getpreOrderReservations() + "");

		if (lblAvgDuration != null) {
			lblAvgDuration.setText(report.getFormattedAverageParkingTime());
		}
		if (lblTotalParkings != null) {
			lblTotalParkings.setText(String.valueOf(report.getTotalParkings()));
		}
		if (lblLateExits != null) {
			lblLateExits.setText(String.format("%d (%.1f%%)", report.getLateExits(), report.getLateExitPercentage()));
		}
		if (lblExtensions != null) {
			lblExtensions
					.setText(String.format("%d (%.1f%%)", report.getExtensions(), report.getExtensionPercentage()));
		}
		if (report.getTotalParkingTimePerDay() != null)
			updateChartTotalParkingTimePerDay(report.getTotalParkingTimePerDay());

		if (report.getHourlyDistribution() != null)
			updateChartHourlyDistribution(report.getHourlyDistribution());

		updateChartExtensionsPercentage(report.getExtensions(), report.getNoExtensions(), report.getTotalSubscribers());

		if (report.getLateExitsByHour() != null)
			updateChartLateExitsByHour(report.getLateExitsByHour());

		updateChartLateSubscribersRate(report.getLateSubscribers(), report.getTotalSubscribers());

		if (lblTotalLateExits != null)
			lblTotalLateExits.setText("Total Late Exits: " + report.getLateExits());

		if (lblTotalMonthHours != null) {
			int totalMonthHours = report.getTotalParkingTimePerDay().values().stream().mapToInt(Integer::intValue)
					.sum();
			report.setTotalMonthHours(totalMonthHours);
			lblTotalMonthHours.setText("Total Hours This Month: " + report.getTotalMonthHours());

		}

		updateChartReservationUsage(report.getpreOrderReservations(), report.getUsedReservations(),
				report.getCancelledReservations());

		// Update parking time chart
		updateParkingTimeChart(report);
	}

	private void updateSubscriberStatusReport(ParkingReport report) {
		if (lblActiveSubscribers != null) {
			lblActiveSubscribers.setText(String.valueOf(report.getActiveSubscribers()));
		}
		if (lblTotalOrders != null) {
			lblTotalOrders.setText(String.valueOf(report.getTotalOrders()));
		}
		if (lblReservationCount != null) {
			lblReservationCount
					.setText(String.format("%d (%.1f%%)", report.getReservations(), report.getReservationPercentage()));
		}
		if (lblCancelled != null) {
			lblCancelled.setText(String.valueOf(report.getCancelledReservations()));
		}

		// Update subscriber activity chart
		updateSubscriberActivityChart(report);
	}

	public void updateActiveParkings(ArrayList<ParkingOrder> activeParkings) {
		Platform.runLater(() -> {
			// Count reservations
			long reservationCount = activeParkings.stream().filter(p -> "ordered".equals(p.getOrderType())).count();

			if (lblReservations != null) {
				lblReservations.setText(String.valueOf(reservationCount));
			}

			// Update parking types pie chart
			updateParkingTypesChart(activeParkings);

			// Calculate peak hours
			calculatePeakHours(activeParkings);
		});
	}

	private void updateOccupancyChart(int currentOccupancy) {
		if (occupancyChart != null && !occupancyChart.getData().isEmpty()) {
			XYChart.Series<String, Number> series = occupancyChart.getData().get(0);

			// Update current hour's data
			LocalDateTime now = LocalDateTime.now();
			String hourLabel = now.getHour() + ":00";

			// Find and update the data point
			for (XYChart.Data<String, Number> data : series.getData()) {
				if (data.getXValue().equals(hourLabel)) {
					data.setYValue(currentOccupancy);
					break;
				}
			}
		}
	}

	private void updateParkingTimeChart(ParkingReport report) {
		if (parkingTimeChart != null) {
			parkingTimeChart.getData().clear();

			XYChart.Series<String, Number> series = new XYChart.Series<>();
			series.setName("Average Duration");

			// Add sample data (would be populated from actual report data)
			series.getData().add(new XYChart.Data<>("Today", report.getAverageParkingTime()));

			parkingTimeChart.getData().add(series);
		}
	}

	private void updateSubscriberActivityChart(ParkingReport report) {
		if (subscriberActivityChart != null) {
			subscriberActivityChart.getData().clear();

			XYChart.Series<String, Number> series = new XYChart.Series<>();
			series.setName("Daily Activity");

			// Add sample data
			series.getData().add(new XYChart.Data<>("Today", report.getTotalOrders()));

			subscriberActivityChart.getData().add(series);
		}
	}

	private void updateParkingTypesChart(ArrayList<ParkingOrder> activeParkings) {
		if (parkingTypesChart != null) {
			long immediate = activeParkings.stream().filter(p -> "not ordered".equals(p.getOrderType())).count();
			long reserved = activeParkings.stream().filter(p -> "ordered".equals(p.getOrderType())).count();

			ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
					new PieChart.Data("Immediate", immediate), new PieChart.Data("Reserved", reserved));
			parkingTypesChart.setData(pieChartData);
		}
	}

	private void calculatePeakHours(ArrayList<ParkingOrder> parkings) {
		// Simple peak hour calculation
		// In real implementation, would analyze entry times
		if (lblPeakHours != null) {
			lblPeakHours.setText("9:00-11:00, 14:00-16:00");
		}
	}

	private void updateLastRefreshTime() {
		if (lblLastUpdate != null) {
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
			lblLastUpdate.setText("Last Update: " + timestamp);
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

	@FXML
	private void handleLogout() {
		BParkClientApp.disconnect();
		System.exit(0);
	}

	@FXML
	private void loadActiveParkings() {
		Message msg = new Message(MessageType.GET_ACTIVE_PARKINGS, null);
		BParkClientApp.sendMessage(msg);
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

	public void updateActiveParkings(ObservableList<ParkingOrder> parkings) {
		Platform.runLater(() -> {
			tableActiveParkings.setItems(parkings);
		});
	}

	private void setupTableColumns() {
		colParkingCode.setCellValueFactory(
				cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getParkingCode()));

		colSubscriberName.setCellValueFactory(
				cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSubscriberName()));

		colSpot.setCellValueFactory(
				cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSpotNumber()));

		colEntryTime.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
				cellData.getValue().getFormattedEntryTime()));

		colExpectedExit.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
				cellData.getValue().getFormattedExpectedExitTime()));

		colType.setCellValueFactory(
				cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOrderType()));
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

	// =======================
	// Parking Report
	// =======================

	private void updateChartTotalParkingTimePerDay(java.util.Map<String, Integer> data) {
		Platform.runLater(() -> {
			chartTotalParkingTimePerDay.getData().clear();
			XYChart.Series<String, Number> series = new XYChart.Series<>();
			series.setName("Total Parking Time Per Day");
			for (Map.Entry<String, Integer> entry : data.entrySet()) {
				series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
			}
			chartTotalParkingTimePerDay.getData().add(series);
		});
	}

	private void updateChartHourlyDistribution(java.util.Map<String, Integer> data) {
		Platform.runLater(() -> {
			chartHourlyDistribution.getData().clear();
			XYChart.Series<String, Number> series = new XYChart.Series<>();
			series.setName("Hourly Distribution");
			for (Map.Entry<String, Integer> entry : data.entrySet()) {
				series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
			}
			chartHourlyDistribution.getData().add(series);
		});
	}

	private void updateChartExtensionsPercentage(int extensions, int noExtensions, int totalSubscribers) {
		Platform.runLater(() -> {
			ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
					new PieChart.Data("Extensions", extensions), new PieChart.Data("No Extensions", noExtensions));
			chartExtensionsPercentage.setData(pieChartData);
			lblTotalExtensions.setText("Total Extensions: " + extensions);

			int total = extensions + noExtensions;
			double percent = (total == 0) ? 0 : (extensions * 100.0 / totalSubscribers);
			lblExtensionsPercent
					.setText(String.format("%.1f", percent) + "%" + " of Subscribers Extended Their Parking!");
		});
	}

	private void updateChartLateExitsByHour(java.util.Map<String, Integer> data) {
		Platform.runLater(() -> {
			chartLateExitsByHour.getData().clear();
			XYChart.Series<String, Number> series = new XYChart.Series<>();
			series.setName("Late Exits By Hour (from End Time)");
			for (Map.Entry<String, Integer> entry : data.entrySet()) {
				series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
			}
			chartLateExitsByHour.getData().add(series);
		});
	}

	private void updateChartLateSubscribersRate(int lateSubscribers, int totalSubscribers) {
		Platform.runLater(() -> {
			int onTime = totalSubscribers - lateSubscribers;
			ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
					new PieChart.Data("Late", lateSubscribers), new PieChart.Data("On Time", onTime));
			chartLateSubscribersRate.setData(pieChartData);
			double percent = (totalSubscribers == 0) ? 0 : (lateSubscribers * 100.0 / totalSubscribers);
			lblLateSubscriberPercent.setText(String.format("%.1f", percent) + "%" + " of Subscribers are late!");
		});
	}

	private void updateChartReservationUsage(int preOrderReservations, int used, int cancelled) {
		Platform.runLater(() -> {
			ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
					new PieChart.Data("Open Reservations", preOrderReservations),
					new PieChart.Data("Used Reservations", used),
					new PieChart.Data("Cancelled Reservations", cancelled));
			chartReservationUsage.setData(pieChartData);
			lblTotalReservations.setText("Total Reservations: " + (preOrderReservations + used + cancelled));
			lblUsedReservations.setText("used Reservations: " + used);
			lblPreOrderReservations.setText("Open Reservations: " + preOrderReservations);
			lblCancelledReservations.setText("Cancelled Reservations: " + cancelled);

		});
	}

}