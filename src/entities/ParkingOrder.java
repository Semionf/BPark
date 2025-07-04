package entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Represents a parking order/session in the ParkB system.
 * 
 * A parking order can be either a reservation or a spontaneous (non-reserved)
 * parking. It holds data such as entry and exit times, parking code, spot
 * number, subscriber name, and various status flags (e.g., late, extended).
 */
public class ParkingOrder implements Serializable {
	private static final long serialVersionUID = 1L;

	/** Unique identifier for the parking order. */
	private int orderID;

	/** The unique parking code associated with this order. */
	private String parkingCode;

	/** The name of the subscriber associated with the order. */
	private String subscriberName;

	/**
	 * The type of order: "ordered" for reservations, "not ordered" for spontaneous.
	 */
	private String orderType;

	/** Time the car entered the parking. */
	private LocalDateTime entryTime;

	/** Time the car exited the parking. May be null if still parked. */
	private LocalDateTime exitTime;

	/** The expected exit time (used for reservations or timing). */
	private LocalDateTime expectedExitTime;

	/** Indicates whether the user exited the parking late. */
	private boolean isLate;

	/** Indicates whether the parking time was extended. */
	private boolean isExtended;

	/** Status of the order: "Active" or "Completed". */
	private String status;

	/** The assigned parking spot number. */
	private String spotNumber;

	/**
	 * Default constructor.
	 */
	public ParkingOrder() {
	}

	/**
	 * Constructs a new ParkingOrder with the essential information.
	 *
	 * @param orderID          the unique ID of the parking order
	 * @param parkingCode      the code for this parking session
	 * @param subscriberName   the name of the subscriber
	 * @param orderType        the type of order ("ordered" or "not ordered")
	 * @param entryTime        the time of parking entry
	 * @param expectedExitTime the expected time of exit
	 */
	public ParkingOrder(int orderID, String parkingCode, String subscriberName, String orderType,
			LocalDateTime entryTime, LocalDateTime expectedExitTime) {
		this.orderID = orderID;
		this.parkingCode = parkingCode;
		this.subscriberName = subscriberName;
		this.orderType = orderType;
		this.entryTime = entryTime;
		this.expectedExitTime = expectedExitTime;
		this.isLate = false;
		this.isExtended = false;
		this.status = "Active";
	}

	// Getters and setters with Javadoc for each

	/**
	 * Returns the order ID.
	 *
	 * @return the order ID
	 */
	public int getOrderID() {
		return orderID;
	}

	/**
	 * Sets the order ID.
	 *
	 * @param orderID the new order ID
	 */
	public void setOrderID(int orderID) {
		this.orderID = orderID;
	}

	/**
	 * Returns the parking code.
	 *
	 * @return the parking code
	 */
	public String getParkingCode() {
		return parkingCode;
	}

	/**
	 * Sets the parking code.
	 *
	 * @param parkingCode the new parking code
	 */
	public void setParkingCode(String parkingCode) {
		this.parkingCode = parkingCode;
	}

	/**
	 * Returns the subscriber name.
	 *
	 * @return the subscriber name
	 */
	public String getSubscriberName() {
		return subscriberName;
	}

	/**
	 * Sets the subscriber name.
	 *
	 * @param subscriberName the new subscriber name
	 */
	public void setSubscriberName(String subscriberName) {
		this.subscriberName = subscriberName;
	}

	/**
	 * Returns the order type ("ordered" or "not ordered").
	 *
	 * @return the order type
	 */
	public String getOrderType() {
		return orderType;
	}

	/**
	 * Sets the order type.
	 *
	 * @param orderType the new order type
	 */
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	/**
	 * Returns the entry time.
	 *
	 * @return the entry time
	 */
	public LocalDateTime getEntryTime() {
		return entryTime;
	}

	/**
	 * Sets the entry time.
	 *
	 * @param entryTime the new entry time
	 */
	public void setEntryTime(LocalDateTime entryTime) {
		this.entryTime = entryTime;
	}

	/**
	 * Returns the exit time.
	 *
	 * @return the exit time
	 */
	public LocalDateTime getExitTime() {
		return exitTime;
	}

	/**
	 * Sets the exit time.
	 *
	 * @param exitTime the new exit time
	 */
	public void setExitTime(LocalDateTime exitTime) {
		this.exitTime = exitTime;
	}

	/**
	 * Returns the expected exit time.
	 *
	 * @return the expected exit time
	 */
	public LocalDateTime getExpectedExitTime() {
		return expectedExitTime;
	}

	/**
	 * Sets the expected exit time.
	 *
	 * @param expectedExitTime the new expected exit time
	 */
	public void setExpectedExitTime(LocalDateTime expectedExitTime) {
		this.expectedExitTime = expectedExitTime;
	}

	/**
	 * Returns whether the parking session ended late.
	 *
	 * @return {@code true} if the exit was late, otherwise {@code false}
	 */
	public boolean isLate() {
		return isLate;
	}

	/**
	 * Sets the late status.
	 *
	 * @param late whether the parking was late
	 */
	public void setLate(boolean late) {
		isLate = late;
	}

	/**
	 * Returns whether the session was extended.
	 *
	 * @return {@code true} if extended, otherwise {@code false}
	 */
	public boolean isExtended() {
		return isExtended;
	}

	/**
	 * Sets the extended flag.
	 *
	 * @param extended whether the session was extended
	 */
	public void setExtended(boolean extended) {
		isExtended = extended;
	}

	/**
	 * Returns the current status ("Active", "Completed").
	 *
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Returns the assigned spot number.
	 *
	 * @return the spot number
	 */
	public String getSpotNumber() {
		return spotNumber;
	}

	/**
	 * Sets the spot number.
	 *
	 * @param spotNumber the new spot number
	 */
	public void setSpotNumber(String spotNumber) {
		this.spotNumber = spotNumber;
	}

	// Utility Methods

	/**
	 * Returns a formatted string of the entry time.
	 *
	 * @return the formatted entry time, or "N/A" if not set
	 */
	public String getFormattedEntryTime() {
		if (entryTime != null) {
			return entryTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		}
		return "N/A";
	}

	/**
	 * Returns a formatted string of the exit time.
	 *
	 * @return the formatted exit time, or "Still parked" if not set
	 */
	public String getFormattedExitTime() {
		if (exitTime != null) {
			return exitTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		}
		return "Still parked";
	}

	/**
	 * Returns a formatted string of the expected exit time.
	 *
	 * @return the formatted expected exit time, or "N/A" if not set
	 */
	public String getFormattedExpectedExitTime() {
		if (expectedExitTime != null) {
			return expectedExitTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		}
		return "N/A";
	}

	/**
	 * Returns the total parking duration in minutes.
	 *
	 * @return duration in minutes, or 0 if entry time is not set
	 */
	public long getParkingDurationMinutes() {
		if (entryTime != null) {
			LocalDateTime endTime = exitTime != null ? exitTime : LocalDateTime.now();
			return ChronoUnit.MINUTES.between(entryTime, endTime);
		}
		return 0;
	}

	/**
	 * Returns the parking duration in a human-readable format.
	 *
	 * @return duration as "X hours, Y minutes"
	 */
	public String getParkingDurationFormatted() {
		long minutes = getParkingDurationMinutes();
		long hours = minutes / 60;
		long remainingMinutes = minutes % 60;
		return String.format("%d hours, %d minutes", hours, remainingMinutes);
	}

	/**
	 * Checks if the car is currently parked.
	 *
	 * @return {@code true} if the car hasn't exited and status is active
	 */
	public boolean isCurrentlyParked() {
		return exitTime == null && "Active".equals(status);
	}

	/**
	 * Checks if the parking order is a reservation.
	 *
	 * @return {@code true} if the order type is "ordered"
	 */
	public boolean isReservation() {
		return "ordered".equals(orderType);
	}

	/**
	 * Returns a string representation of the ParkingOrder object.
	 *
	 * @return string with all key fields of the object
	 */
	@Override
	public String toString() {
		return "ParkingOrder{" + "orderID=" + orderID + ", parkingCode='" + parkingCode + '\'' + ", subscriberName='"
				+ subscriberName + '\'' + ", orderType='" + orderType + '\'' + ", entryTime=" + entryTime
				+ ", exitTime=" + exitTime + ", expectedExitTime=" + expectedExitTime + ", isLate=" + isLate
				+ ", isExtended=" + isExtended + ", status='" + status + '\'' + ", spotNumber='" + spotNumber + '\''
				+ '}';
	}
}