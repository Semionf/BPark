package entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Represents a parking report in the ParkB system. Contains statistical data
 * about parking usage, subscriber status, and system performance.
 */
public class ParkingReport implements Serializable {

	private static final long serialVersionUID = 1L;

	private String reportType; // "PARKING_TIME", "SUBSCRIBER_STATUS"
	private LocalDate reportDate;

	// Parking Time Report fields
	private int totalParkings;
	private double averageParkingTime; // in minutes
	private int lateExits;
	private int extensions;
	private int minParkingTime;
	private int maxParkingTime;

	// Subscriber Status Report fields
	private int activeSubscribers;
	private int totalOrders;
	private int reservations;
	private int immediateEntries;
	private int cancelledReservations;
	private double averageSessionDuration;

	// --- Fields for graphs ---
	private Map<String, Integer> totalParkingTimePerDay; // day -> total minutes
	private Map<String, Integer> hourlyDistribution; // hour -> count
	private int noExtensions; // totalParkings - extensions
	private Map<String, Integer> lateExitsByHour; // hour -> count
	private int lateSubscribers;
	private int totalSubscribers;
	private Map<String, Integer> subscribersPerDay; // day -> count
	private int usedReservations;
	private int preOrderReservations;
	private int totalMonthHours;

	// Constructors
	public ParkingReport() {
	}

	public ParkingReport(String reportType, LocalDate reportDate) {
		this.reportType = reportType;
		this.reportDate = reportDate;
	}

	// Getters and Setters
	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public LocalDate getReportDate() {
		return reportDate;
	}

	public void setReportDate(LocalDate reportDate) {
		this.reportDate = reportDate;
	}

	public int getTotalParkings() {
		return totalParkings;
	}

	public void setTotalParkings(int totalParkings) {
		this.totalParkings = totalParkings;
	}

	public double getAverageParkingTime() {
		return averageParkingTime;
	}

	public void setAverageParkingTime(double averageParkingTime) {
		this.averageParkingTime = averageParkingTime;
	}

	public int getLateExits() {
		return lateExits;
	}

	public void setLateExits(int lateExits) {
		this.lateExits = lateExits;
	}

	public int getExtensions() {
		return extensions;
	}

	public void setExtensions(int extensions) {
		this.extensions = extensions;
	}

	public int getMinParkingTime() {
		return minParkingTime;
	}

	public void setMinParkingTime(int minParkingTime) {
		this.minParkingTime = minParkingTime;
	}

	public int getMaxParkingTime() {
		return maxParkingTime;
	}

	public void setMaxParkingTime(int maxParkingTime) {
		this.maxParkingTime = maxParkingTime;
	}

	public int getActiveSubscribers() {
		return activeSubscribers;
	}

	public void setActiveSubscribers(int activeSubscribers) {
		this.activeSubscribers = activeSubscribers;
	}

	public int getTotalOrders() {
		return totalOrders;
	}

	public void setTotalOrders(int totalOrders) {
		this.totalOrders = totalOrders;
	}

	public int getReservations() {
		return reservations;
	}

	public void setReservations(int reservations) {
		this.reservations = reservations;
	}

	public int getImmediateEntries() {
		return immediateEntries;
	}

	public void setImmediateEntries(int immediateEntries) {
		this.immediateEntries = immediateEntries;
	}

	public int getCancelledReservations() {
		return cancelledReservations;
	}

	public void setCancelledReservations(int cancelledReservations) {
		this.cancelledReservations = cancelledReservations;
	}

	public double getAverageSessionDuration() {
		return averageSessionDuration;
	}

	public void setAverageSessionDuration(double averageSessionDuration) {
		this.averageSessionDuration = averageSessionDuration;
	}

	// Utility methods
	public String getFormattedReportDate() {
		if (reportDate != null) {
			return reportDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}
		return "";
	}

	public String getFormattedAverageParkingTime() {
		long hours = (long) (averageParkingTime / 60);
		long minutes = (long) (averageParkingTime % 60);
		return String.format("%d hours, %d minutes", hours, minutes);
	}

	public double getLateExitPercentage() {
		if (totalParkings > 0) {
			return (double) lateExits / totalParkings * 10;
		}
		return 0.0;
	}

	public double getExtensionPercentage() {
		if (totalParkings > 0) {
			return (double) extensions / totalParkings * 10;
		}
		return 0.0;
	}

	public double getReservationPercentage() {
		if (totalOrders > 0) {
			return (double) reservations / totalOrders * 10;
		}
		return 0.0;
	}

	@Override
	public String toString() {
		return "ParkingReport{" + "reportType='" + reportType + '\'' + ", reportDate=" + reportDate + ", totalParkings="
				+ totalParkings + ", averageParkingTime=" + averageParkingTime + ", lateExits=" + lateExits
				+ ", extensions=" + extensions + ", activeSubscribers=" + activeSubscribers + ", totalOrders="
				+ totalOrders + ", reservations=" + reservations + ", immediateEntries=" + immediateEntries + '}';
	}

	public Map<String, Integer> getTotalParkingTimePerDay() {
		return totalParkingTimePerDay;
	}

	public void setTotalParkingTimePerDay(java.util.Map<String, Integer> m) {
		this.totalParkingTimePerDay = m;
	}

	public Map<String, Integer> getHourlyDistribution() {
		return hourlyDistribution;
	}

	public void setHourlyDistribution(java.util.Map<String, Integer> m) {
		this.hourlyDistribution = m;
	}

	public int getNoExtensions() {
		return noExtensions;
	}

	public void setNoExtensions(int noExtensions) {
		this.noExtensions = noExtensions;
	}

	public Map<String, Integer> getLateExitsByHour() {
		return lateExitsByHour;
	}

	public void setLateExitsByHour(java.util.Map<String, Integer> m) {
		this.lateExitsByHour = m;
	}

	public int getLateSubscribers() {
		return lateSubscribers;
	}

	public void setLateSubscribers(int lateSubscribers) {
		this.lateSubscribers = lateSubscribers;
	}

	public int getTotalSubscribers() {
		return totalSubscribers;
	}

	public void setTotalSubscribers(int totalSubscribers) {
		this.totalSubscribers = totalSubscribers;
	}

	public Map<String, Integer> getSubscribersPerDay() {
		return subscribersPerDay;
	}

	public void setSubscribersPerDay(java.util.Map<String, Integer> m) {
		this.subscribersPerDay = m;
	}

	public int getUsedReservations() {
		return usedReservations;
	}

	public void setUsedReservations(int usedReservations) {
		this.usedReservations = usedReservations;
	}

	public int getTotalMonthHours() {
		return totalMonthHours;
	}

	public void setTotalMonthHours(int totalMonthHours) {
		this.totalMonthHours = totalMonthHours;
	}

	public int getpreOrderReservations() {
		return preOrderReservations;
	}

	public void setpreOrderReservations(int preOrderReservations) {
		this.preOrderReservations = preOrderReservations;
	}


}