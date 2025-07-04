/**
 * Represents a parking report in the ParkB system.
 * Contains statistical data about parking usage,
 * subscriber status, and system performance.
 * Used in generating dashboard data, monthly summaries, and system insights.
 */
package entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ParkingReport implements Serializable {

	private static final long serialVersionUID = 1L;

	/** The type of the report: "PARKING_TIME" or "SUBSCRIBER_STATUS" */
	private String reportType;

	/** The date the report was generated or references */
	private LocalDate reportDate;

	// Parking Time Report Fields
	/** Total number of parking sessions */
	private int totalParkings;

	/** Average parking duration in minutes */
	private double averageParkingTime;

	/** Number of sessions with late exits */
	private int lateExits;

	/** Number of extended parking sessions */
	private int extensions;

	/** Minimum recorded parking time in minutes */
	private int minParkingTime;

	/** Maximum recorded parking time in minutes */
	private int maxParkingTime;

	/** Number of immediate parking sessions */
	private int imidiateParkings;

	// Subscriber Status Report Fields
	/** Number of currently active subscribers */
	private int activeSubscribers;

	/** Total number of parking orders */
	private int totalOrders;

	/** Number of reserved parkings */
	private int reservations;

	/** Number of immediate entry parkings */
	private int immediateEntries;

	/** Number of cancelled reservations */
	private int cancelledReservations;

	/** Average duration of sessions in minutes */
	private double averageSessionDuration;

	// Graph-related fields
	/** Total parking time per day: date -> minutes */
	private Map<String, Integer> totalParkingTimePerDay;

	/** Hourly distribution of parkings: hour -> count */
	private Map<String, Integer> hourlyDistribution;

	/** Number of parkings without extensions */
	private int noExtensions;

	/** Late exits grouped by hour: hour -> count */
	private Map<String, Integer> lateExitsByHour;

	/** Number of subscribers with at least one late exit */
	private int lateSubscribers;

	/** Total number of subscribers in the system */
	private int totalSubscribers;

	/** Number of subscribers per day: date -> count */
	private Map<String, Integer> subscribersPerDay;

	/** Number of used reservations */
	private int usedReservations;

	/** Number of pre-ordered reservations */
	private int preOrderReservations;

	/** Total hours of parking recorded in the month */
	private int totalMonthHours;

	/** Number of currently occupied parking spots */
	private int occupied;

	private int totalSpots;

	/** Default constructor */
	public ParkingReport() {
	}

	/**
	 * Constructs a parking report with given type and date
	 * 
	 * @param reportType the type of report
	 * @param reportDate the date of the report
	 */
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

	public Map<String, Integer> getTotalParkingTimePerDay() {
		return totalParkingTimePerDay;
	}

	public void setTotalParkingTimePerDay(Map<String, Integer> m) {
		this.totalParkingTimePerDay = m;
	}

	public Map<String, Integer> getHourlyDistribution() {
		return hourlyDistribution;
	}

	public void setHourlyDistribution(Map<String, Integer> m) {
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

	public void setLateExitsByHour(Map<String, Integer> m) {
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

	public void setSubscribersPerDay(Map<String, Integer> m) {
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

	public int getOccupied() {
		return occupied;
	}

	public void setOccupied(int occupied) {
		this.occupied = occupied;
	}

	public int getImidiateParkings() {
		return imidiateParkings;
	}

	public void setImidiateParkings(int imidiateParkings) {
		this.imidiateParkings = imidiateParkings;
	}

	public int getTotalSpots() {
		return totalSpots;
	}

	public void setTotalSpots(int totalSpots) {
		this.totalSpots = totalSpots;
	}
	// Utility methods

	/**
	 * Returns the report date in yyyy-MM-dd format
	 * 
	 * @return formatted date string
	 */
	public String getFormattedReportDate() {
		if (reportDate != null) {
			return reportDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}
		return "";
	}

	/**
	 * Returns average parking time formatted as "X hours, Y minutes"
	 * 
	 * @return formatted time string
	 */
	public String getFormattedAverageParkingTime() {
		long hours = (long) (averageParkingTime / 60);
		long minutes = (long) (averageParkingTime % 60);
		return String.format("%d hours, %d minutes", hours, minutes);
	}

	/**
	 * Calculates the percentage of late exits among total parkings
	 * 
	 * @return percentage from 0 to 100
	 */
	public double getLateExitPercentage() {
		if (totalParkings > 0) {
			return (double) lateExits / totalParkings * 100;
		}
		return 0.0;
	}

	/**
	 * Calculates the percentage of extended sessions among total parkings
	 * 
	 * @return percentage from 0 to 100
	 */
	public double getExtensionPercentage() {
		if (totalParkings > 0) {
			return (double) extensions / totalParkings * 100;
		}
		return 0.0;
	}

	/**
	 * Calculates the percentage of reservations among total orders
	 * 
	 * @return percentage from 0 to 100
	 */
	public double getReservationPercentage() {
		if (totalOrders > 0) {
			return (double) reservations / totalOrders * 100;
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
}