package entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Represents a parking report in the ParkB system. Contains statistical data
 * about parking usage, subscriber status, and system performance. The report
 * can represent different types such as "PARKING_TIME" or "SUBSCRIBER_STATUS".
 * Implements {@link Serializable} for object serialization.
 */
public class ParkingReport implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Type of the report, e.g. "PARKING_TIME", "SUBSCRIBER_STATUS". */
	private String reportType;

	/** The date of the report. */
	private LocalDate reportDate;

	// Parking Time Report fields
	/** Total number of parkings recorded. */
	private int totalParkings;

	/** Average parking time in minutes. */
	private double averageParkingTime;

	/** Number of late exits recorded. */
	private int lateExits;

	/** Number of parking time extensions granted. */
	private int extensions;

	/** Minimum parking time recorded (in minutes). */
	private int minParkingTime;

	/** Maximum parking time recorded (in minutes). */
	private int maxParkingTime;

	/** Number of immediate parkings (entered immediately). */
	private int imidiateParkings;

	// Subscriber Status Report fields
	/** Number of active subscribers at the time of the report. */
	private int activeSubscribers;

	/** Total number of orders made by subscribers. */
	private int totalOrders;

	/** Number of reservations made. */
	private int reservations;

	/** Number of immediate entries (without reservation). */
	private int immediateEntries;

	/** Number of cancelled reservations. */
	private int cancelledReservations;

	/** Average session duration in minutes for subscribers. */
	private double averageSessionDuration;

	// --- Fields for graphs ---
	/**
	 * Map of total parking time per day, keyed by day string (e.g. "2025-07-01").
	 */
	private Map<String, Integer> totalParkingTimePerDay;

	/** Hourly distribution of parkings, keyed by hour string (e.g. "08", "14"). */
	private Map<String, Integer> hourlyDistribution;

	/** Number of parkings without extensions (totalParkings - extensions). */
	private int noExtensions;

	/** Map of late exits by hour, keyed by hour string. */
	private Map<String, Integer> lateExitsByHour;

	/** Number of subscribers who were late. */
	private int lateSubscribers;

	/** Total number of subscribers in the system. */
	private int totalSubscribers;

	/** Map of subscribers per day, keyed by day string. */
	private Map<String, Integer> subscribersPerDay;

	/** Number of reservations that were used. */
	private int usedReservations;

	/** Number of pre-order reservations made. */
	private int preOrderReservations;

	/** Total number of hours in the month (for utilization calculations). */
	private int totalMonthHours;

	/** Number of occupied parking spots. */
	private int occupied;

	/**
	 * Default constructor.
	 */
	public ParkingReport() {
	}

	/**
	 * Constructor with report type and report date.
	 * 
	 * @param reportType The type of report (e.g. "PARKING_TIME").
	 * @param reportDate The date of the report.
	 */
	public ParkingReport(String reportType, LocalDate reportDate) {
		this.reportType = reportType;
		this.reportDate = reportDate;
	}

	/**
	 * Gets the type of the report.
	 * 
	 * @return The report type.
	 */
	public String getReportType() {
		return reportType;
	}

	/**
	 * Sets the type of the report.
	 * 
	 * @param reportType The report type to set.
	 */
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	/**
	 * Gets the date of the report.
	 * 
	 * @return The report date.
	 */
	public LocalDate getReportDate() {
		return reportDate;
	}

	/**
	 * Sets the date of the report.
	 * 
	 * @param reportDate The report date to set.
	 */
	public void setReportDate(LocalDate reportDate) {
		this.reportDate = reportDate;
	}

	/**
	 * Gets the total number of parkings.
	 * 
	 * @return The total parkings.
	 */
	public int getTotalParkings() {
		return totalParkings;
	}

	/**
	 * Sets the total number of parkings.
	 * 
	 * @param totalParkings Total parkings to set.
	 */
	public void setTotalParkings(int totalParkings) {
		this.totalParkings = totalParkings;
	}

	/**
	 * Gets the average parking time in minutes.
	 * 
	 * @return The average parking time.
	 */
	public double getAverageParkingTime() {
		return averageParkingTime;
	}

	/**
	 * Sets the average parking time in minutes.
	 * 
	 * @param averageParkingTime Average parking time to set.
	 */
	public void setAverageParkingTime(double averageParkingTime) {
		this.averageParkingTime = averageParkingTime;
	}

	/**
	 * Gets the count of late exits.
	 * 
	 * @return The number of late exits.
	 */
	public int getLateExits() {
		return lateExits;
	}

	/**
	 * Sets the count of late exits.
	 * 
	 * @param lateExits Number of late exits to set.
	 */
	public void setLateExits(int lateExits) {
		this.lateExits = lateExits;
	}

	/**
	 * Gets the number of parking extensions.
	 * 
	 * @return The number of extensions.
	 */
	public int getExtensions() {
		return extensions;
	}

	/**
	 * Sets the number of parking extensions.
	 * 
	 * @param extensions Number of extensions to set.
	 */
	public void setExtensions(int extensions) {
		this.extensions = extensions;
	}

	/**
	 * Gets the minimum parking time recorded (minutes).
	 * 
	 * @return Minimum parking time.
	 */
	public int getMinParkingTime() {
		return minParkingTime;
	}

	/**
	 * Sets the minimum parking time recorded (minutes).
	 * 
	 * @param minParkingTime Minimum parking time to set.
	 */
	public void setMinParkingTime(int minParkingTime) {
		this.minParkingTime = minParkingTime;
	}

	/**
	 * Gets the maximum parking time recorded (minutes).
	 * 
	 * @return Maximum parking time.
	 */
	public int getMaxParkingTime() {
		return maxParkingTime;
	}

	/**
	 * Sets the maximum parking time recorded (minutes).
	 * 
	 * @param maxParkingTime Maximum parking time to set.
	 */
	public void setMaxParkingTime(int maxParkingTime) {
		this.maxParkingTime = maxParkingTime;
	}

	/**
	 * Gets the number of active subscribers.
	 * 
	 * @return Number of active subscribers.
	 */
	public int getActiveSubscribers() {
		return activeSubscribers;
	}

	/**
	 * Sets the number of active subscribers.
	 * 
	 * @param activeSubscribers Number of active subscribers to set.
	 */
	public void setActiveSubscribers(int activeSubscribers) {
		this.activeSubscribers = activeSubscribers;
	}

	/**
	 * Gets the total number of orders made by subscribers.
	 * 
	 * @return Total orders count.
	 */
	public int getTotalOrders() {
		return totalOrders;
	}

	/**
	 * Sets the total number of orders made by subscribers.
	 * 
	 * @param totalOrders Total orders to set.
	 */
	public void setTotalOrders(int totalOrders) {
		this.totalOrders = totalOrders;
	}

	/**
	 * Gets the number of reservations.
	 * 
	 * @return Number of reservations.
	 */
	public int getReservations() {
		return reservations;
	}

	/**
	 * Sets the number of reservations.
	 * 
	 * @param reservations Number of reservations to set.
	 */
	public void setReservations(int reservations) {
		this.reservations = reservations;
	}

	/**
	 * Gets the number of immediate entries.
	 * 
	 * @return Number of immediate entries.
	 */
	public int getImmediateEntries() {
		return immediateEntries;
	}

	/**
	 * Sets the number of immediate entries.
	 * 
	 * @param immediateEntries Number of immediate entries to set.
	 */
	public void setImmediateEntries(int immediateEntries) {
		this.immediateEntries = immediateEntries;
	}

	/**
	 * Gets the number of cancelled reservations.
	 * 
	 * @return Number of cancelled reservations.
	 */
	public int getCancelledReservations() {
		return cancelledReservations;
	}

	/**
	 * Sets the number of cancelled reservations.
	 * 
	 * @param cancelledReservations Number of cancelled reservations to set.
	 */
	public void setCancelledReservations(int cancelledReservations) {
		this.cancelledReservations = cancelledReservations;
	}

	/**
	 * Gets the average session duration in minutes.
	 * 
	 * @return Average session duration.
	 */
	public double getAverageSessionDuration() {
		return averageSessionDuration;
	}

	/**
	 * Sets the average session duration in minutes.
	 * 
	 * @param averageSessionDuration Average session duration to set.
	 */
	public void setAverageSessionDuration(double averageSessionDuration) {
		this.averageSessionDuration = averageSessionDuration;
	}

	/**
	 * Returns the report date formatted as "yyyy-MM-dd".
	 * 
	 * @return Formatted report date or empty string if date is null.
	 */
	public String getFormattedReportDate() {
		if (reportDate != null) {
			return reportDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}
		return "";
	}

	/**
	 * Returns the average parking time formatted as hours and minutes.
	 * 
	 * @return Average parking time formatted string.
	 */
	public String getFormattedAverageParkingTime() {
		long hours = (long) (averageParkingTime / 60);
		long minutes = (long) (averageParkingTime % 60);
		return String.format("%d hours, %d minutes", hours, minutes);
	}

	/**
	 * Calculates the percentage of late exits out of total parkings.
	 * 
	 * @return Late exit percentage (0 if no parkings).
	 */
	public double getLateExitPercentage() {
		if (totalParkings > 0) {
			return (double) lateExits / totalParkings * 100;
		}
		return 0.0;
	}

	/**
	 * Calculates the percentage of extensions out of total parkings.
	 * 
	 * @return Extension percentage (0 if no parkings).
	 */
	public double getExtensionPercentage() {
		if (totalParkings > 0) {
			return (double) extensions / totalParkings * 100;
		}
		return 0.0;
	}

	/**
	 * Calculates the percentage of reservations out of total orders.
	 * 
	 * @return Reservation percentage (0 if no orders).
	 */
	public double getReservationPercentage() {
		if (totalOrders > 0) {
			return (double) reservations / totalOrders * 100;
		}
		return 0.0;
	}

	/**
	 * Returns a string representation of the ParkingReport.
	 * 
	 * @return String with key fields.
	 */
	@Override
	public String toString() {
		return "ParkingReport{" + "reportType='" + reportType + '\'' + ", reportDate=" + reportDate + ", totalParkings="
				+ totalParkings + ", averageParkingTime=" + averageParkingTime + ", lateExits=" + lateExits
				+ ", extensions=" + extensions + ", activeSubscribers=" + activeSubscribers + ", totalOrders="
				+ totalOrders + ", reservations=" + reservations + ", immediateEntries=" + immediateEntries + '}';
	}

	/**
	 * Gets the total parking time per day map.
	 * 
	 * @return Map of day to total parking time in minutes.
	 */
	public Map<String, Integer> getTotalParkingTimePerDay() {
		return totalParkingTimePerDay;
	}

	/**
	 * Sets the total parking time per day map.
	 * 
	 * @param totalParkingTimePerDay Map of day to total parking time.
	 */
	public void setTotalParkingTimePerDay(Map<String, Integer> totalParkingTimePerDay) {
		this.totalParkingTimePerDay = totalParkingTimePerDay;
	}

	/**
	 * Gets the hourly distribution map.
	 * 
	 * @return Map of hour to count of parkings.
	 */
	public Map<String, Integer> getHourlyDistribution() {
		return hourlyDistribution;
	}

	/**
	 * Sets the hourly distribution map.
	 * 
	 * @param hourlyDistribution Map of hour to parking count.
	 */
	public void setHourlyDistribution(Map<String, Integer> hourlyDistribution) {
		this.hourlyDistribution = hourlyDistribution;
	}

	/**
	 * Gets the count of parkings without extensions.
	 * 
	 * @return Number of parkings with no extensions.
	 */
	public int getNoExtensions() {
		return noExtensions;
	}

	/**
	 * Sets the count of parkings without extensions.
	 * 
	 * @param noExtensions Number of no-extension parkings.
	 */
	public void setNoExtensions(int noExtensions) {
		this.noExtensions = noExtensions;
	}

	/**
	 * Gets the map of late exits by hour.
	 * 
	 * @return Map of hour to late exit count.
	 */
	public Map<String, Integer> getLateExitsByHour() {
		return lateExitsByHour;
	}

	/**
	 * Sets the map of late exits by hour.
	 * 
	 * @param lateExitsByHour Map of hour to late exit count.
	 */
	public void setLateExitsByHour(Map<String, Integer> lateExitsByHour) {
		this.lateExitsByHour = lateExitsByHour;
	}

	/**
	 * Gets the number of late subscribers.
	 * 
	 * @return Number of late subscribers.
	 */
	public int getLateSubscribers() {
		return lateSubscribers;
	}

	/**
	 * Sets the number of late subscribers.
	 * 
	 * @param lateSubscribers Number of late subscribers to set.
	 */
	public void setLateSubscribers(int lateSubscribers) {
		this.lateSubscribers = lateSubscribers;
	}

	/**
	 * Gets the total number of subscribers.
	 * 
	 * @return Total subscribers count.
	 */
	public int getTotalSubscribers() {
		return totalSubscribers;
	}

	/**
	 * Sets the total number of subscribers.
	 * 
	 * @param totalSubscribers Total subscribers to set.
	 */
	public void setTotalSubscribers(int totalSubscribers) {
		this.totalSubscribers = totalSubscribers;
	}

	/**
	 * Gets the map of subscribers per day.
	 * 
	 * @return Map of day to subscribers count.
	 */
	public Map<String, Integer> getSubscribersPerDay() {
		return subscribersPerDay;
	}

	/**
	 * Sets the map of subscribers per day.
	 * 
	 * @param subscribersPerDay Map of day to subscribers count.
	 */
	public void setSubscribersPerDay(Map<String, Integer> subscribersPerDay) {
		this.subscribersPerDay = subscribersPerDay;
	}

	/**
	 * Gets the number of used reservations.
	 * 
	 * @return Used reservations count.
	 */
	public int getUsedReservations() {
		return usedReservations;
	}

	/**
	 * Sets the number of used reservations.
	 * 
	 * @param usedReservations Number of used reservations to set.
	 */
	public void setUsedReservations(int usedReservations) {
		this.usedReservations = usedReservations;
	}

	/**
	 * Gets the total number of hours in the month.
	 * 
	 * @return Total month hours.
	 */
	public int getTotalMonthHours() {
		return totalMonthHours;
	}

	/**
	 * Sets the total number of hours in the month.
	 * 
	 * @param totalMonthHours Total month hours to set.
	 */
	public void setTotalMonthHours(int totalMonthHours) {
		this.totalMonthHours = totalMonthHours;
	}

	/**
	 * Gets the number of pre-order reservations.
	 * 
	 * @return Pre-order reservations count.
	 */
	public int getpreOrderReservations() {
		return preOrderReservations;
	}

	/**
	 * Sets the number of pre-order reservations.
	 * 
	 * @param preOrderReservations Pre-order reservations to set.
	 */
	public void setpreOrderReservations(int preOrderReservations) {
		this.preOrderReservations = preOrderReservations;
	}

	/**
	 * Gets the number of immediate parkings.
	 * 
	 * @return Number of immediate parkings.
	 */
	public int getImidiateParkings() {
		return imidiateParkings;
	}

	/**
	 * Sets the number of immediate parkings.
	 * 
	 * @param imidiateParkings Number of immediate parkings to set.
	 */
	public void setImidiateParkings(int imidiateParkings) {
		this.imidiateParkings = imidiateParkings;
	}

	/**
	 * Gets the number of occupied parking spots.
	 * 
	 * @return Number of occupied spots.
	 */
	public int getOccupied() {
		return occupied;
	}

	/**
	 * Sets the number of occupied parking spots.
	 * 
	 * @param occupied Number of occupied spots to set.
	 */
	public void setOccupied(int occupied) {
		this.occupied = occupied;
	}

}
