/**
 * Represents a parking report in the ParkB system.
 * Contains statistical data about parking usage,
 * subscriber status, and system performance.
 * Used in generating dashboard data, monthly summaries, and system insights.
 */
package common;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Represents a parking report in the ParkB system. Contains statistical data
 * about parking usage, subscriber status, and system performance. Used in
 * generating dashboard data, monthly summaries, and system insights.
 */
public class ParkingReport implements Serializable {

	/**
	 * Serial version UID for Serializable interface. Ensures compatibility during
	 * the deserialization process.
	 */
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

	/** Number of total parking spots */
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
	/**
	 * Returns the report type (e.g., "PARKING_TIME", "SUBSCRIBER_STATUS").
	 *
	 * @return the report type as string
	 */
	public String getReportType() {
		return reportType;
	}

	/**
	 * Sets the type of the report.
	 *
	 * @param reportType the report type to set
	 */
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	/**
	 * Returns the date associated with the report.
	 *
	 * @return the report date
	 */
	public LocalDate getReportDate() {
		return reportDate;
	}

	/**
	 * Sets the date of the report.
	 *
	 * @param reportDate the report date to set
	 */
	public void setReportDate(LocalDate reportDate) {
		this.reportDate = reportDate;
	}

	/**
	 * Returns the total number of parking sessions in the report.
	 *
	 * @return total number of parkings
	 */
	public int getTotalParkings() {
		return totalParkings;
	}

	/**
	 * Sets the total number of parking sessions.
	 *
	 * @param totalParkings total number of parkings to set
	 */
	public void setTotalParkings(int totalParkings) {
		this.totalParkings = totalParkings;
	}

	/**
	 * Returns the average parking time in minutes.
	 *
	 * @return average parking time in minutes
	 */
	public double getAverageParkingTime() {
		return averageParkingTime;
	}

	/**
	 * Sets the average parking time in minutes.
	 *
	 * @param averageParkingTime average parking time to set
	 */
	public void setAverageParkingTime(double averageParkingTime) {
		this.averageParkingTime = averageParkingTime;
	}

	/**
	 * Returns the number of late exits recorded.
	 *
	 * @return number of late exits
	 */
	public int getLateExits() {
		return lateExits;
	}

	/**
	 * Sets the number of late exits.
	 *
	 * @param lateExits number of late exits to set
	 */
	public void setLateExits(int lateExits) {
		this.lateExits = lateExits;
	}

	/**
	 * Returns the number of parking sessions with extensions.
	 *
	 * @return number of extensions
	 */
	public int getExtensions() {
		return extensions;
	}

	/**
	 * Sets the number of parking extensions.
	 *
	 * @param extensions number of extensions to set
	 */
	public void setExtensions(int extensions) {
		this.extensions = extensions;
	}

	/**
	 * Returns the minimum recorded parking time in minutes.
	 *
	 * @return minimum parking time
	 */
	public int getMinParkingTime() {
		return minParkingTime;
	}

	/**
	 * Sets the minimum parking time recorded.
	 *
	 * @param minParkingTime minimum parking time to set
	 */
	public void setMinParkingTime(int minParkingTime) {
		this.minParkingTime = minParkingTime;
	}

	/**
	 * Returns the maximum recorded parking time in minutes.
	 *
	 * @return maximum parking time
	 */
	public int getMaxParkingTime() {
		return maxParkingTime;
	}

	/**
	 * Sets the maximum parking time recorded.
	 *
	 * @param maxParkingTime maximum parking time to set
	 */
	public void setMaxParkingTime(int maxParkingTime) {
		this.maxParkingTime = maxParkingTime;
	}

	/**
	 * Returns the number of currently active subscribers.
	 *
	 * @return number of active subscribers
	 */
	public int getActiveSubscribers() {
		return activeSubscribers;
	}

	/**
	 * Sets the number of currently active subscribers.
	 *
	 * @param activeSubscribers number of active subscribers to set
	 */
	public void setActiveSubscribers(int activeSubscribers) {
		this.activeSubscribers = activeSubscribers;
	}

	/**
	 * Returns the total number of parking orders.
	 *
	 * @return total number of orders
	 */
	public int getTotalOrders() {
		return totalOrders;
	}

	/**
	 * Sets the total number of parking orders.
	 *
	 * @param totalOrders total number of orders to set
	 */
	public void setTotalOrders(int totalOrders) {
		this.totalOrders = totalOrders;
	}

	/**
	 * Returns the number of reserved parking sessions.
	 *
	 * @return number of reservations
	 */
	public int getReservations() {
		return reservations;
	}

	/**
	 * Sets the number of reserved parking sessions.
	 *
	 * @param reservations number of reservations to set
	 */
	public void setReservations(int reservations) {
		this.reservations = reservations;
	}

	/**
	 * Returns the number of immediate entry parkings.
	 *
	 * @return number of immediate entries
	 */
	public int getImmediateEntries() {
		return immediateEntries;
	}

	/**
	 * Sets the number of immediate entry parkings.
	 *
	 * @param immediateEntries number of immediate entries to set
	 */
	public void setImmediateEntries(int immediateEntries) {
		this.immediateEntries = immediateEntries;
	}

	/**
	 * Returns the number of cancelled reservations.
	 *
	 * @return number of cancelled reservations
	 */
	public int getCancelledReservations() {
		return cancelledReservations;
	}

	/**
	 * Sets the number of cancelled reservations.
	 *
	 * @param cancelledReservations number of cancelled reservations to set
	 */
	public void setCancelledReservations(int cancelledReservations) {
		this.cancelledReservations = cancelledReservations;
	}

	/**
	 * Returns the average duration of parking sessions in minutes.
	 *
	 * @return average session duration
	 */
	public double getAverageSessionDuration() {
		return averageSessionDuration;
	}

	/**
	 * Sets the average duration of parking sessions.
	 *
	 * @param averageSessionDuration average session duration to set
	 */
	public void setAverageSessionDuration(double averageSessionDuration) {
		this.averageSessionDuration = averageSessionDuration;
	}

	/**
	 * Gets the total parking time per day. Map format: date (String) -> total
	 * minutes parked (Integer).
	 *
	 * @return map of total parking time per day
	 */
	public Map<String, Integer> getTotalParkingTimePerDay() {
		return totalParkingTimePerDay;
	}

	/**
	 * Sets the total parking time per day.
	 *
	 * @param m map of date to total minutes parked
	 */
	public void setTotalParkingTimePerDay(Map<String, Integer> m) {

		this.totalParkingTimePerDay = m;
	}

	/**
	 * Returns the hourly distribution of parking sessions.
	 *
	 * @return hourly distribution
	 */
	public Map<String, Integer> getHourlyDistribution() {
		return hourlyDistribution;
	}

	/**
	 * Sets the hourly distribution of parkings. Map format: hour (String) -> number
	 * of parkings (Integer).
	 *
	 * @param m map of hour to parking count
	 */
	public void setHourlyDistribution(Map<String, Integer> m) {
		this.hourlyDistribution = m;
	}

	/**
	 * Returns the number of parkings without extensions.
	 *
	 * @return number of parkings without extensions
	 */
	public int getNoExtensions() {
		return noExtensions;
	}

	/**
	 * Sets the number of parkings without extensions.
	 *
	 * @param noExtensions number of no-extensions to set
	 */
	public void setNoExtensions(int noExtensions) {
		this.noExtensions = noExtensions;
	}

	/**
	 * Returns a map of late exits grouped by hour.
	 *
	 * @return late exits by hour
	 */
	public Map<String, Integer> getLateExitsByHour() {
		return lateExitsByHour;
	}

	/**
	 * Sets the number of late exits grouped by hour. Map format: hour (String) ->
	 * count of late exits (Integer).
	 *
	 * @param m map of hour to late exit count
	 */
	public void setLateExitsByHour(Map<String, Integer> m) {
		this.lateExitsByHour = m;
	}

	/**
	 * Returns the number of late subscribers in the system.
	 *
	 * @return number of late subscribers
	 */
	public int getLateSubscribers() {
		return lateSubscribers;
	}

	/**
	 * Sets the number of late subscribers.
	 *
	 * @param lateSubscribers number of late subscribers to set
	 */
	public void setLateSubscribers(int lateSubscribers) {
		this.lateSubscribers = lateSubscribers;
	}

	/**
	 * Returns the total number of subscribers in the system.
	 *
	 * @return total number of subscribers
	 */
	public int getTotalSubscribers() {
		return totalSubscribers;
	}

	/**
	 * Sets the total number of subscribers.
	 *
	 * @param totalSubscribers total subscribers to set
	 */
	public void setTotalSubscribers(int totalSubscribers) {
		this.totalSubscribers = totalSubscribers;
	}

	/**
	 * Returns a map of number of subscribers per day.
	 *
	 * @return subscribers per day
	 */
	public Map<String, Integer> getSubscribersPerDay() {
		return subscribersPerDay;
	}

	/**
	 * Sets the number of subscribers per day.
	 *
	 * @param m map of date to subscriber count
	 */
	public void setSubscribersPerDay(Map<String, Integer> m) {
		this.subscribersPerDay = m;
	}

	/**
	 * Returns the number of used reservations.
	 *
	 * @return used reservations
	 */
	public int getUsedReservations() {
		return usedReservations;
	}

	/**
	 * Sets the number of used reservations.
	 *
	 * @param usedReservations used reservations to set
	 */
	public void setUsedReservations(int usedReservations) {
		this.usedReservations = usedReservations;
	}

	/**
	 * Returns the total number of parking hours in the month.
	 *
	 * @return total month hours
	 */
	public int getTotalMonthHours() {
		return totalMonthHours;
	}

	/**
	 * Sets the total number of parking hours in the month.
	 *
	 * @param totalMonthHours total month hours to set
	 */
	public void setTotalMonthHours(int totalMonthHours) {
		this.totalMonthHours = totalMonthHours;
	}

	/**
	 * Returns the number of pre-ordered reservations.
	 *
	 * @return pre-order reservations
	 */
	public int getpreOrderReservations() {
		return preOrderReservations;
	}

	/**
	 * Sets the number of pre-ordered reservations.
	 *
	 * @param preOrderReservations pre-order reservations to set
	 */
	public void setpreOrderReservations(int preOrderReservations) {
		this.preOrderReservations = preOrderReservations;
	}

	/**
	 * Returns the number of currently occupied parking spots.
	 *
	 * @return occupied spots
	 */
	public int getOccupied() {
		return occupied;
	}

	/**
	 * Sets the number of currently occupied parking spots.
	 *
	 * @param occupied number of occupied spots
	 */
	public void setOccupied(int occupied) {
		this.occupied = occupied;
	}

	/**
	 * Returns the number of immediate parkings.
	 *
	 * @return number of immediate parkings
	 */
	public int getImidiateParkings() {
		return imidiateParkings;
	}

	/**
	 * Sets the number of immediate parkings.
	 *
	 * @param imidiateParkings number of immediate parkings to set
	 */
	public void setImidiateParkings(int imidiateParkings) {
		this.imidiateParkings = imidiateParkings;
	}

	/**
	 * Returns the total number of parking spots in the system.
	 *
	 * @return total number of parking spots
	 */
	public int getTotalSpots() {
		return totalSpots;
	}

	/**
	 * Sets the total number of parking spots in the system.
	 *
	 * @param totalSpots total number of spots to set
	 */
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

	/**
	 * Returns a string representation of the ParkingReport object. Includes key
	 * statistical fields for logging or debugging purposes.
	 *
	 * @return string representation of the report
	 */
	@Override
	public String toString() {
		return "ParkingReport{" + "reportType='" + reportType + '\'' + ", reportDate=" + reportDate + ", totalParkings="
				+ totalParkings + ", averageParkingTime=" + averageParkingTime + ", lateExits=" + lateExits
				+ ", extensions=" + extensions + ", activeSubscribers=" + activeSubscribers + ", totalOrders="
				+ totalOrders + ", reservations=" + reservations + ", immediateEntries=" + immediateEntries + '}';
	}
}
