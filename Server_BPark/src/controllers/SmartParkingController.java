package controllers;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import common.ParkingOrder;
import common.ParkingSubscriber;
import server.DBController;

/**
 * Controller class for smart parking logic.
 * 
 * Provides configuration constants and algorithms to manage parking spot
 * availability, time slot precision, and allocation rules for the BPark system.
 */
public class SmartParkingController {

	// Configuration constants

	/** Total number of available parking spots in the system. */
	private static final int TOTAL_PARKING_SPOTS = 10;

	/** Threshold (40%) used to determine availability for a given slot. */
	private static final double AVAILABILITY_THRESHOLD = 0.4;

	/** Preferred reservation window in hours. */
	private static final int PREFERRED_WINDOW_HOURS = 8;

	/** Standard reservation duration in hours. */
	private static final int STANDARD_BOOKING_HOURS = 4;

	/** Minimum hours required for a spontaneous reservation. */
	private static final int MINIMUM_SPONTANEOUS_HOURS = 2;

	/** Time slot granularity in minutes (15-minute intervals). */
	private static final int TIME_SLOT_MINUTES = 15;

	/** Display window for available slots (±1 hour). */
	private static final int DISPLAY_WINDOW_HOURS = 1;

	/** Minimum hours allowed for reservation extension. */
	private static final int MINIMUM_EXTENSION_HOURS = 2;

	/** Maximum hours allowed for reservation extension. */
	private static final int MAXIMUM_EXTENSION_HOURS = 4;

	/**
	 * Default constructor for SmartParkingController. Initializes the controller
	 * instance with no custom logic.
	 */
	public SmartParkingController() {
		// Default constructor
	}

	/**
	 * Flag indicating whether the operation was successful (used for result
	 * status).
	 */
	public int successFlag;

	/**
	 * Constructs a SmartParkingController with specified database credentials.
	 *
	 * @param dbname The name of the database to connect to.
	 * @param pass   The password used for authentication.
	 */
	public SmartParkingController(String dbname, String pass) {
		DBController.initializeConnection(dbname, pass);
	}

	// ========== CORE DATA STRUCTURES ==========

	/**
	 * Represents a 15-minute time slot within the smart parking allocation system.
	 * Each time slot stores its start time, availability status, number of
	 * available spots, and whether it satisfies the 40% availability threshold
	 * rule.
	 */
	public static class TimeSlot {
		/** Start time of the 15-minute slot. */
		public LocalDateTime startTime;

		/** Indicates whether the time slot is available for parking. */
		public boolean isAvailable;

		/** Number of available parking spots during this time slot. */
		public int availableSpots;

		/** Indicates if the 40% availability threshold rule is met for this slot. */
		public boolean meetsFortyPercentRule;

		/**
		 * Constructs a TimeSlot object with the given parameters.
		 *
		 * @param startTime             the starting time of the slot
		 * @param isAvailable           whether the slot is available
		 * @param availableSpots        how many spots are available
		 * @param meetsFortyPercentRule whether the 40% rule is met
		 */
		public TimeSlot(LocalDateTime startTime, boolean isAvailable, int availableSpots,
				boolean meetsFortyPercentRule) {
			this.startTime = startTime;
			this.isAvailable = isAvailable;
			this.availableSpots = availableSpots;
			this.meetsFortyPercentRule = meetsFortyPercentRule;
		}

		/**
		 * Returns the formatted time (HH:mm) for display purposes.
		 *
		 * @return the formatted start time as a string
		 */
		public String getFormattedTime() {
			return startTime.format(DateTimeFormatter.ofPattern("HH:mm"));
		}
	}

	/**
	 * Represents a time window during which a specific parking spot is available.
	 * Includes logic to determine whether the spot can accommodate a booking of a
	 * specific duration (e.g., 8 hours).
	 */
	public static class SpotAvailability {
		/** Unique identifier of the parking spot. */
		public int spotId;

		/** Start time of the availability window. */
		public LocalDateTime availableFrom;

		/** End time of the availability window. */
		public LocalDateTime availableUntil;

		/** Duration of availability in hours, precomputed for efficiency. */
		public long availabilityDurationHours;

		/**
		 * Constructs a SpotAvailability object for a given parking spot and time
		 * window.
		 *
		 * @param spotId the ID of the parking spot
		 * @param from   the start time of availability
		 * @param until  the end time of availability
		 */
		public SpotAvailability(int spotId, LocalDateTime from, LocalDateTime until) {
			this.spotId = spotId;
			this.availableFrom = from;
			this.availableUntil = until;
			this.availabilityDurationHours = Duration.between(from, until).toHours();
		}

		/**
		 * Checks whether the parking spot is available for an 8-hour window starting
		 * from the specified booking time.
		 *
		 * @param bookingStart the start time of the desired booking
		 * @return true if the spot is available for the entire 8-hour window, false
		 *         otherwise
		 */
		public boolean hasEightHourWindow(LocalDateTime bookingStart) {
			LocalDateTime eightHourEnd = bookingStart.plusHours(PREFERRED_WINDOW_HOURS);
			return !bookingStart.isBefore(availableFrom) && !eightHourEnd.isAfter(availableUntil);
		}

		/**
		 * Checks whether the spot is available for the full duration between
		 * bookingStart and bookingEnd.
		 *
		 * @param bookingStart the start time of the desired booking
		 * @param bookingEnd   the end time of the desired booking
		 * @return true if the booking fits entirely within the availability window
		 */
		public boolean canAccommodateBooking(LocalDateTime bookingStart, LocalDateTime bookingEnd) {
			return !bookingStart.isBefore(availableFrom) && !bookingEnd.isAfter(availableUntil);
		}
	}

	// ========== ALL EXISTING PARKINGCONTROLLER METHODS ==========

	/**
	 * Validates login credentials and returns the user's type if authenticated.
	 *
	 * @param userName the username entered
	 * @param password the password entered (not used here but may be in future)
	 * @return the user type as a string if found, otherwise "None"
	 */
	public String checkLogin(String userName, String password) {
		String qry = "SELECT UserTypeEnum FROM users WHERE UserName = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setString(1, userName);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("UserTypeEnum");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error checking login: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return "None";
	}

	/**
	 * Retrieves the number of currently available parking spots.
	 *
	 * @return the count of free parking spots
	 */
	public int getAvailableParkingSpots() {
		String qry = "SELECT COUNT(*) as available FROM ParkingSpot WHERE isOccupied = false";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("available");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting available spots: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		return 0;
	}

	/**
	 * Fetches detailed user information by username.
	 *
	 * @param userName the username of the subscriber
	 * @return a ParkingSubscriber object with user details, or null if not found
	 */
	public ParkingSubscriber getUserInfo(String userName) {
		String qry = "SELECT * FROM users WHERE UserName = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setString(1, userName);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					ParkingSubscriber user = new ParkingSubscriber();
					user.setSubscriberID(rs.getInt("User_ID"));
					user.setFirstName(rs.getString("Name"));
					user.setPhoneNumber(rs.getString("Phone"));
					user.setEmail(rs.getString("Email"));
					user.setCarNumber(rs.getString("CarNum"));
					user.setSubscriberCode(userName);
					user.setUserType(rs.getString("UserTypeEnum"));
					return user;
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting user info: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return null;
	}

	/**
	 * Attempts to create a parking reservation for the specified user on the given
	 * date.
	 *
	 * @param userName           the username of the subscriber
	 * @param reservationDateStr the desired reservation date in "YYYY-MM-DD" format
	 * @return a confirmation message or error description
	 */
	public String makeReservation(String userName, String reservationDateStr) {

		if (!canMakeReservation()) {
			return "Not enough available spots for reservation (need 40% available)";
		}
		Connection conn = DBController.getInstance().getConnection();

		try {
			Date reservationDate = Date.valueOf(reservationDateStr);
			LocalDate today = LocalDate.now();
			LocalDate resDate = reservationDate.toLocalDate();

			if (resDate.isBefore(today.plusDays(1)) || resDate.isAfter(today.plusDays(7))) {
				return "Reservation must be between 24 hours and 7 days in advance";
			}

			int userID = getUserID(userName);
			if (userID == -1) {
				return "User not found";
			}

			int parkingSpotID = getAvailableParkingSpotID();
			if (parkingSpotID == -1) {
				return "No available parking spots";
			}

			String qry = "INSERT INTO Reservations (User_ID, parking_ID, reservation_Date, Date_Of_Placing_Order, statusEnum) VALUES (?, ?, ?, ?, 'active')";

			try (PreparedStatement stmt = conn.prepareStatement(qry, PreparedStatement.RETURN_GENERATED_KEYS)) {
				stmt.setInt(1, userID);
				stmt.setInt(2, parkingSpotID);
				stmt.setDate(3, reservationDate);
				stmt.setDate(4, Date.valueOf(LocalDate.now()));
				stmt.executeUpdate();

				try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						int reservationCode = generatedKeys.getInt(1);
						return "Reservation confirmed. Confirmation code: " + reservationCode;
					}
				}
			}
		} catch (IllegalArgumentException e) {
			return "Invalid date format. Use YYYY-MM-DD";
		} catch (SQLException e) {
			System.out.println("Error making reservation: " + e.getMessage());
			return "Reservation failed";
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return "Reservation failed";
	}

	/**
	 * Generates a unique username by appending numbers to the base name if needed.
	 *
	 * @param baseName the preferred base name
	 * @return a unique, sanitized username
	 */
	public String generateUniqueUsername(String baseName) {
		String cleanName = baseName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

		if (isUsernameAvailable(cleanName)) {
			return cleanName;
		}

		for (int i = 1; i <= 999; i++) {
			String candidate = cleanName + i;
			if (isUsernameAvailable(candidate)) {
				return candidate;
			}
		}

		return cleanName + System.currentTimeMillis() % 10000;
	}

	/**
	 * Extends the parking session associated with the given parking code by a
	 * number of hours.
	 *
	 * @param parkingCodeStr  the string representation of the parking code
	 * @param additionalHours number of hours to extend (1 to 4)
	 * @return a message indicating success or failure
	 */
	public String extendParkingTime(String parkingCodeStr, int additionalHours) {
		if (additionalHours < 1 || additionalHours > 4) {
			return "Can only extend parking by 1-4 hours";
		}
		Connection conn = DBController.getInstance().getConnection();

		try {
			int parkingCode = Integer.parseInt(parkingCodeStr);
			String qry = "SELECT pi.* FROM ParkingInfo pi WHERE pi.Code = ? AND pi.Actual_end_time IS NULL";

			try (PreparedStatement stmt = conn.prepareStatement(qry)) {
				stmt.setInt(1, parkingCode);
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						Time currentEstimatedEnd = rs.getTime("Estimated_end_time");
						LocalTime newEstimatedEnd = currentEstimatedEnd.toLocalTime().plusHours(additionalHours);

						String updateQry = "UPDATE ParkingInfo SET Estimated_end_time = ?, IsExtended = true WHERE Code = ?";

						try (PreparedStatement updateStmt = conn.prepareStatement(updateQry)) {
							updateStmt.setTime(1, Time.valueOf(newEstimatedEnd));
							updateStmt.setInt(2, parkingCode);
							updateStmt.executeUpdate();

							return "Parking time extended by " + additionalHours + " hours until " + newEstimatedEnd;
						}
					}
				}
			}
		} catch (NumberFormatException e) {
			return "Invalid parking code format";
		} catch (SQLException e) {
			System.out.println("Error extending parking time: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return "Invalid parking code or parking session not active";
	}

	/**
	 * Retrieves the full parking history for a specific user.
	 *
	 * @param userName the username of the subscriber
	 * @return a list of ParkingOrder records
	 */
	public ArrayList<ParkingOrder> getParkingHistory(String userName) {
		ArrayList<ParkingOrder> history = new ArrayList<>();
		String qry = "SELECT pi.*, ps.ParkingSpot_ID FROM ParkingInfo pi JOIN users u ON pi.User_ID = u.User_ID JOIN ParkingSpot ps ON pi.ParkingSpot_ID = ps.ParkingSpot_ID WHERE u.UserName = ? ORDER BY pi.Date DESC, pi.Actual_start_time DESC";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setString(1, userName);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					ParkingOrder order = new ParkingOrder();
					order.setOrderID(rs.getInt("ParkingInfo_ID"));
					order.setParkingCode(String.valueOf(rs.getInt("Code")));
					order.setOrderType(rs.getString("IsOrderedEnum"));
					order.setSpotNumber("Spot " + rs.getInt("ParkingSpot_ID"));

					Date date = rs.getDate("Date");
					Time startTime = rs.getTime("Actual_start_time");
					Time endTime = rs.getTime("Actual_end_time");
					Time estimatedEnd = rs.getTime("Estimated_end_time");

					if (date != null && startTime != null) {
						order.setEntryTime(LocalDateTime.of(date.toLocalDate(), startTime.toLocalTime()));
					}
					if (date != null && endTime != null) {
						order.setExitTime(LocalDateTime.of(date.toLocalDate(), endTime.toLocalTime()));
					}
					if (date != null && estimatedEnd != null) {
						order.setExpectedExitTime(LocalDateTime.of(date.toLocalDate(), estimatedEnd.toLocalTime()));
					}

					order.setLate(rs.getBoolean("IsLate"));
					order.setExtended(rs.getBoolean("IsExtended"));
					order.setStatus(endTime != null ? "Completed" : "Active");

					history.add(order);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting parking history: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return history;
	}

	/**
	 * Retrieves all currently active parking sessions in the system.
	 *
	 * @return a list of active ParkingOrder objects
	 */
	public ArrayList<ParkingOrder> getActiveParkings() {
		ArrayList<ParkingOrder> activeParkings = new ArrayList<>();
		String qry = "SELECT pi.*, u.Name, ps.ParkingSpot_ID FROM ParkingInfo pi JOIN users u ON pi.User_ID = u.User_ID JOIN ParkingSpot ps ON pi.ParkingSpot_ID = ps.ParkingSpot_ID WHERE pi.Actual_end_time IS NULL ORDER BY pi.Actual_start_time";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					ParkingOrder order = new ParkingOrder();
					order.setOrderID(rs.getInt("ParkingInfo_ID"));
					order.setParkingCode(String.valueOf(rs.getInt("Code")));
					order.setOrderType(rs.getString("IsOrderedEnum"));
					order.setSubscriberName(rs.getString("Name"));
					order.setSpotNumber("Spot " + rs.getInt("ParkingSpot_ID"));

					Date date = rs.getDate("Date");
					Time startTime = rs.getTime("Actual_start_time");
					Time estimatedEnd = rs.getTime("Estimated_end_time");

					if (date != null && startTime != null) {
						order.setEntryTime(LocalDateTime.of(date.toLocalDate(), startTime.toLocalTime()));
					}
					if (date != null && estimatedEnd != null) {
						order.setExpectedExitTime(LocalDateTime.of(date.toLocalDate(), estimatedEnd.toLocalTime()));
					}

					order.setStatus("Active");
					activeParkings.add(order);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting active parkings: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		return activeParkings;
	}

	/**
	 * Updates the phone number and email address of a subscriber.
	 *
	 * @param updateData a comma-separated string: username,phone,email
	 * @return a status message indicating success or failure
	 */
	public String updateSubscriberInfo(String updateData) {
		String[] data = updateData.split(",");
		if (data.length != 3) {
			return "Invalid update data format";
		}

		String userName = data[0];
		String phone = data[1];
		String email = data[2];

		String qry = "UPDATE users SET Phone = ?, Email = ? WHERE UserName = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setString(1, phone);
			stmt.setString(2, email);
			stmt.setString(3, userName);

			int rowsUpdated = stmt.executeUpdate();
			if (rowsUpdated > 0) {
				return "Subscriber information updated successfully";
			}
		} catch (SQLException e) {
			System.out.println("Error updating subscriber info: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return "Failed to update subscriber information";
	}

	/**
	 * Cancels an existing reservation based on the reservation code.
	 *
	 * @param reservationCode the unique code of the reservation
	 * @return a message indicating whether the cancellation was successful
	 */
	public String cancelReservation(int reservationCode) {
		String qry = "UPDATE Reservations SET statusEnum = 'cancelled' WHERE Reservation_code = ? AND statusEnum = 'active'";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setInt(1, reservationCode);
			int rowsUpdated = stmt.executeUpdate();

			if (rowsUpdated > 0) {
				return "Reservation cancelled successfully";
			}
		} catch (SQLException e) {
			System.out.println("Error cancelling reservation: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return "Reservation not found or already cancelled";
	}

	/**
	 * Logs out the user by printing to the server console.
	 *
	 * @param userName the username of the user logging out
	 */
	public void logoutUser(String userName) {
		System.out.println("User logged out: " + userName);
	}

	/**
	 * Initializes parking spots in the database if not already present. Typically
	 * used for setup or system reset.
	 */
	public void initializeParkingSpots() {
		Connection conn = DBController.getInstance().getConnection();

		try {
			String checkQry = "SELECT COUNT(*) FROM ParkingSpot";

			try (PreparedStatement stmt = conn.prepareStatement(checkQry)) {
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next() && rs.getInt(1) == 0) {
						String insertQry = "INSERT INTO ParkingSpot (isOccupied) VALUES (false)";
						try (PreparedStatement insertStmt = conn.prepareStatement(insertQry)) {
							for (int i = 1; i <= TOTAL_PARKING_SPOTS; i++) {
								insertStmt.executeUpdate();
							}
						}
						System.out.println("Successfully initialized " + TOTAL_PARKING_SPOTS + " parking spots");
					} else {
						System.out.println("Parking spots already exist: " + rs.getInt(1) + " spots found");
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Error initializing parking spots: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
	}

	// ========== NEW SMART FEATURES ==========

	/**
	 * Get available 15-minute time slots for a specific date and preferred time.
	 *
	 * @param date          the date to check availability
	 * @param preferredTime the time to center around
	 * @return a list of TimeSlot objects with availability details
	 */
	public List<TimeSlot> getAvailableTimeSlots(LocalDate date, LocalTime preferredTime) {
		List<TimeSlot> timeSlots = new ArrayList<>();

		try {
			if (!dateHasValidBookingWindow(date)) {
				return timeSlots;
			}

			LocalDateTime preferredDateTime = LocalDateTime.of(date, preferredTime);
			LocalDateTime startRange = preferredDateTime.minusHours(DISPLAY_WINDOW_HOURS);
			LocalDateTime endRange = preferredDateTime.plusHours(DISPLAY_WINDOW_HOURS);

			LocalDateTime currentSlot = startRange;
			while (!currentSlot.isAfter(endRange)) {
				LocalDateTime bookingEnd = currentSlot.plusHours(STANDARD_BOOKING_HOURS);
				boolean hasValidWindow = hasValidFourHourWindow(currentSlot, bookingEnd);
				int availableSpots = countAvailableSpotsForWindow(currentSlot, bookingEnd);
				boolean meetsFortyPercent = availableSpots >= (TOTAL_PARKING_SPOTS * AVAILABILITY_THRESHOLD);

				timeSlots.add(new TimeSlot(currentSlot, hasValidWindow && meetsFortyPercent, availableSpots,
						meetsFortyPercent));

				currentSlot = currentSlot.plusMinutes(TIME_SLOT_MINUTES);
			}

		} catch (Exception e) {
			System.out.println("Error getting available time slots: " + e.getMessage());
		}

		return timeSlots;
	}

	/**
	 * Make a pre-booking reservation with 15-minute precision, 24h to 7 days in
	 * advance.
	 *
	 * @param userName    the username of the subscriber
	 * @param dateTimeStr the desired booking start datetime as a string
	 * @return confirmation message or failure reason
	 */
	public String makePreBooking(String userName, String dateTimeStr) {
		try {
			LocalDateTime bookingStart = parseDateTime(dateTimeStr);
			LocalDateTime bookingEnd = bookingStart.plusHours(STANDARD_BOOKING_HOURS);

			if (bookingStart.getMinute() % TIME_SLOT_MINUTES != 0) {
				return "Booking time must be in 15-minute intervals (00, 15, 30, 45)";
			}

			LocalDateTime now = LocalDateTime.now();
			if (bookingStart.isBefore(now.plusHours(24))) {
				return "Pre-booking must be at least 24 hours in advance";
			}
			if (bookingStart.isAfter(now.plusDays(7))) {
				return "Pre-booking cannot be more than 7 days in advance";
			}

			if (!hasValidFourHourWindow(bookingStart, bookingEnd)) {
				return "No available 4-hour window with required capacity at selected time";
			}

			int optimalSpotId = findOptimalSpotForPreBooking(bookingStart, bookingEnd);
			if (optimalSpotId == -1) {
				return "No optimal parking spot available for selected time";
			}

			int userID = getUserID(userName);
			if (userID == -1) {
				return "User not found";
			}

			return createReservationWithDateTime(userID, optimalSpotId, bookingStart, bookingEnd, "prebooking");

		} catch (Exception e) {
			System.out.println("Error making pre-booking: " + e.getMessage());
			return "Pre-booking failed: " + e.getMessage();
		}
	}

	/**
	 * Request to extend a parking session, only allowed in the last hour of
	 * parking.
	 *
	 * @param parkingCodeStr the unique parking code
	 * @return message indicating result of the extension attempt
	 */
	public String requestParkingExtension(String parkingCodeStr) {
		Connection conn = DBController.getInstance().getConnection();

		try {
			int parkingCode = Integer.parseInt(parkingCodeStr);

			String sessionQuery = """
					SELECT pi.*, ps.ParkingSpot_ID
					FROM ParkingInfo pi
					JOIN ParkingSpot ps ON pi.ParkingSpot_ID = ps.ParkingSpot_ID
					WHERE pi.Code = ? AND pi.Actual_end_time IS NULL
					""";

			try (PreparedStatement stmt = conn.prepareStatement(sessionQuery)) {
				stmt.setInt(1, parkingCode);
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						int spotId = rs.getInt("ParkingSpot_ID");
						Time estimatedEndTime = rs.getTime("Estimated_end_time");
						Date date = rs.getDate("Date");

						LocalDateTime currentEndTime = LocalDateTime.of(date.toLocalDate(),
								estimatedEndTime.toLocalTime());
						LocalDateTime now = LocalDateTime.now();

						if (now.isBefore(currentEndTime.minusHours(1))) {
							return "Extensions can only be requested during the last hour of parking";
						}

						if (now.isAfter(currentEndTime)) {
							return "Parking session has already ended";
						}

						int maxExtensionHours = findMaximumExtension(spotId, currentEndTime);
						if (maxExtensionHours < MINIMUM_EXTENSION_HOURS) {
							return "No extension available - spot not free for minimum required time";
						}

						LocalDateTime newEndTime = currentEndTime.plusHours(maxExtensionHours);

						String updateQuery = """
								UPDATE ParkingInfo
								SET Estimated_end_time = ?, IsExtended = true
								WHERE Code = ?
								""";

						try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
							updateStmt.setTime(1, Time.valueOf(newEndTime.toLocalTime()));
							updateStmt.setInt(2, parkingCode);
							updateStmt.executeUpdate();

							return String.format("Extension successful! Parking extended by %d hours until %s",
									maxExtensionHours, newEndTime.format(DateTimeFormatter.ofPattern("HH:mm")));
						}
					}
				}
			}

		} catch (NumberFormatException e) {
			return "Invalid parking code format";
		} catch (Exception e) {
			System.out.println("Error requesting extension: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		return "Invalid parking code or parking session not found";
	}

	/**
	 * Returns a string with current smart parking system status (total, occupied,
	 * available).
	 *
	 * @return human-readable system summary
	 */
	public String getSystemStatus() {
		try {
			int totalSpots = TOTAL_PARKING_SPOTS;
			int occupiedSpots = getCurrentlyOccupiedSpots();
			int availableSpots = totalSpots - occupiedSpots;

			return String.format("Smart Parking Status: %d total spots, %d occupied, %d available (%.1f%% available)",
					totalSpots, occupiedSpots, availableSpots, (double) availableSpots / totalSpots * 10);

		} catch (Exception e) {
			return "Error getting system status: " + e.getMessage();
		}
	}

	// ========== HELPER METHODS ==========

	/**
	 * Represents a parking spot allocation result, including spot ID, allocated
	 * duration, and whether it meets the preferred 8-hour window requirement.
	 */
	private static class SpotAllocation {
		/**
		 * The ID of the allocated parking spot.
		 */
		int spotId;

		/**
		 * The number of hours allocated for the parking session.
		 */
		int allocatedHours;

		/**
		 * Flag indicating if the allocation satisfies the 8-hour preferred window rule.
		 */
		boolean hasEightHourWindow;

		/**
		 * Constructs a new SpotAllocation object.
		 *
		 * @param spotId             the ID of the allocated parking spot
		 * @param allocatedHours     the number of hours allocated
		 * @param hasEightHourWindow true if allocation meets the 8-hour rule, false
		 *                           otherwise
		 */
		SpotAllocation(int spotId, int allocatedHours, boolean hasEightHourWindow) {
			this.spotId = spotId;
			this.allocatedHours = allocatedHours;
			this.hasEightHourWindow = hasEightHourWindow;
		}
	}

	/**
	 * Checks if the given date contains at least one valid booking window that
	 * meets availability rules.
	 *
	 * @param date the date to check
	 * @return true if a valid booking window exists, false otherwise
	 */
	private boolean dateHasValidBookingWindow(LocalDate date) {
		try {
			LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.of(0, 0));
			LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.of(23, 45));

			LocalDateTime currentTime = dayStart;
			while (!currentTime.isAfter(dayEnd.minusHours(STANDARD_BOOKING_HOURS))) {
				LocalDateTime windowEnd = currentTime.plusHours(STANDARD_BOOKING_HOURS);
				if (hasValidFourHourWindow(currentTime, windowEnd)) {
					return true;
				}
				currentTime = currentTime.plusMinutes(TIME_SLOT_MINUTES);
			}
		} catch (Exception e) {
			System.out.println("Error checking date validity: " + e.getMessage());
		}
		return false;
	}

	/**
	 * Determines if a four-hour window starting and ending at the given times has
	 * enough available spots.
	 *
	 * @param startTime the start time of the window
	 * @param endTime   the end time of the window
	 * @return true if the number of available spots meets the threshold, false
	 *         otherwise
	 */
	private boolean hasValidFourHourWindow(LocalDateTime startTime, LocalDateTime endTime) {
		try {
			int availableSpots = countAvailableSpotsForWindow(startTime, endTime);
			return availableSpots >= (TOTAL_PARKING_SPOTS * AVAILABILITY_THRESHOLD);
		} catch (Exception e) {
			System.out.println("Error checking four-hour window: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Counts the number of available parking spots for a specific time window,
	 * based on current occupancy and reservations.
	 *
	 * @param startTime the start time of the window
	 * @param endTime   the end time of the window
	 * @return the number of available parking spots
	 */
	private int countAvailableSpotsForWindow(LocalDateTime startTime, LocalDateTime endTime) {
		try {
			int occupiedSpots = getCurrentlyOccupiedSpots();
			int reservedSpots = countReservationOverlaps(startTime, endTime);
			return Math.max(0, TOTAL_PARKING_SPOTS - occupiedSpots - reservedSpots);
		} catch (Exception e) {
			System.out.println("Error counting available spots: " + e.getMessage());
			return 0;
		}
	}

	/**
	 * Finds the optimal spot for a pre-booking based on the desired start and end
	 * times. Returns the first available spot or -1 if none found.
	 *
	 * @param bookingStart the desired booking start time
	 * @param bookingEnd   the desired booking end time
	 * @return the ID of an available spot, or -1 if none found
	 */
	private int findOptimalSpotForPreBooking(LocalDateTime bookingStart, LocalDateTime bookingEnd) {
		try {
			List<Integer> availableSpots = getAllAvailableSpots(bookingStart, bookingEnd);

			if (availableSpots.isEmpty()) {
				return -1;
			}

			// Return first available spot (simple allocation)
			return availableSpots.get(0);

		} catch (Exception e) {
			System.out.println("Error finding optimal spot: " + e.getMessage());
			return -1;
		}
	}

	/**
	 * Attempts to find a suitable spot and booking duration for a spontaneous
	 * booking starting at the given time. Tries durations from
	 * STANDARD_BOOKING_HOURS down to MINIMUM_SPONTANEOUS_HOURS.
	 *
	 * @param startTime the desired start time for spontaneous booking
	 * @return a SpotAllocation object if a suitable allocation is found, null
	 *         otherwise
	 */
	private SpotAllocation findOptimalSpontaneousAllocation(LocalDateTime startTime) {
		try {
			for (int hours = STANDARD_BOOKING_HOURS; hours >= MINIMUM_SPONTANEOUS_HOURS; hours--) {
				LocalDateTime endTime = startTime.plusHours(hours);
				List<Integer> availableSpots = getAllAvailableSpots(startTime, endTime);

				if (!availableSpots.isEmpty()) {
					return new SpotAllocation(availableSpots.get(0), hours, hours >= PREFERRED_WINDOW_HOURS);
				}
			}

			return null;

		} catch (Exception e) {
			System.out.println("Error finding spontaneous allocation: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Retrieves a list of all currently available parking spots that are free
	 * during the specified time window.
	 *
	 * @param startTime the start time of the window
	 * @param endTime   the end time of the window
	 * @return list of available parking spot IDs
	 * @throws SQLException if a database error occurs
	 */
	private List<Integer> getAllAvailableSpots(LocalDateTime startTime, LocalDateTime endTime) throws SQLException {
		List<Integer> availableSpots = new ArrayList<>();

		String spotsQuery = "SELECT ParkingSpot_ID FROM ParkingSpot WHERE isOccupied = false ORDER BY ParkingSpot_ID";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(spotsQuery)) {
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					int spotId = rs.getInt("ParkingSpot_ID");
					if (isSpotAvailableForPeriod(spotId, startTime, endTime)) {
						availableSpots.add(spotId);
					}
				}
			}
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		return availableSpots;
	}

	/**
	 * Checks if a specific parking spot is available for the entire specified time
	 * range.
	 *
	 * @param spotId    the ID of the parking spot
	 * @param startTime the start time of the window
	 * @param endTime   the end time of the window
	 * @return true if the spot is fully available for the time range, false
	 *         otherwise
	 * @throws SQLException if a database error occurs
	 */
	private boolean isSpotAvailableForPeriod(int spotId, LocalDateTime startTime, LocalDateTime endTime)
			throws SQLException {
		String conflictQuery = """
				SELECT COUNT(*) FROM Reservations
				WHERE assigned_parking_spot_id = ?
				AND statusEnum = 'active'
				AND NOT (reservation_Date < ? OR reservation_Date > ?)
				""";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(conflictQuery)) {
			stmt.setInt(1, spotId);
			stmt.setDate(2, Date.valueOf(endTime.toLocalDate()));
			stmt.setDate(3, Date.valueOf(startTime.toLocalDate()));

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) == 0;
				}
			}
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		return false;
	}

	/**
	 * Finds the maximum number of hours a parking session can be extended for the
	 * given spot, starting from the current end time.
	 *
	 * @param spotId         the parking spot ID
	 * @param currentEndTime the current end time of the session
	 * @return the number of hours it can be extended, or 0 if none
	 */
	private int findMaximumExtension(int spotId, LocalDateTime currentEndTime) {
		try {
			for (int hours = MAXIMUM_EXTENSION_HOURS; hours >= MINIMUM_EXTENSION_HOURS; hours--) {
				LocalDateTime testEndTime = currentEndTime.plusHours(hours);
				if (isSpotAvailableForPeriod(spotId, currentEndTime, testEndTime)) {
					return hours;
				}
			}
		} catch (Exception e) {
			System.out.println("Error finding maximum extension: " + e.getMessage());
		}
		return 0;
	}

	/**
	 * Parses a date-time string into a LocalDateTime object. Supports both
	 * 'yyyy-MM-dd HH:mm:ss' and ISO_LOCAL_DATE_TIME ('yyyy-MM-ddTHH:mm').
	 *
	 * @param dateTimeStr the input string
	 * @return the parsed LocalDateTime object
	 * @throws IllegalArgumentException if the format is invalid
	 */
	private LocalDateTime parseDateTime(String dateTimeStr) {
		try {
			if (dateTimeStr.contains("T")) {
				return LocalDateTime.parse(dateTimeStr);
			} else if (dateTimeStr.contains(" ")) {
				return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			} else {
				throw new IllegalArgumentException("Unsupported DATETIME format: " + dateTimeStr);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Invalid DATETIME format: " + dateTimeStr + ". Use 'YYYY-MM-DD HH:MM:SS' or 'YYYY-MM-DDTHH:MM'");
		}
	}

	/**
	 * Creates a reservation entry in the database for a specified user, spot, and
	 * time range.
	 *
	 * @param userID    the ID of the user making the reservation
	 * @param spotId    the ID of the parking spot being reserved
	 * @param startTime the start time of the reservation
	 * @param endTime   the end time of the reservation
	 * @param type      the type of reservation ("prebooking", "spontaneous", etc.)
	 * @return a success message with reservation details, or failure message if
	 *         creation fails
	 * @throws SQLException if a database error occurs
	 */
	private String createReservationWithDateTime(int userID, int spotId, LocalDateTime startTime, LocalDateTime endTime,
			String type) throws SQLException {
		String insertQuery = """
				INSERT INTO Reservations
				(User_ID, parking_ID, reservation_Date, Date_Of_Placing_Order, statusEnum, assigned_parking_spot_id)
				VALUES (?, ?, ?, NOW(), 'active', ?)
				""";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
			stmt.setInt(1, userID);
			stmt.setInt(2, spotId);
			stmt.setDate(3, Date.valueOf(startTime.toLocalDate()));
			stmt.setInt(4, spotId);
			stmt.executeUpdate();

			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					int reservationCode = generatedKeys.getInt(1);

					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
					return String.format("%s successful! Code: %d, Spot: %d, Time: %s to %s",
							type.substring(0, 1).toUpperCase() + type.substring(1), reservationCode, spotId,
							startTime.format(formatter), endTime.format(formatter));
				}
			}
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return "Reservation creation failed";
	}

	/**
	 * Counts the number of currently active reservations that overlap with the
	 * specified time range.
	 *
	 * @param startTime the start of the time window
	 * @param endTime   the end of the time window
	 * @return the number of overlapping active reservations
	 * @throws SQLException if a database error occurs
	 */
	private int countReservationOverlaps(LocalDateTime startTime, LocalDateTime endTime) throws SQLException {
		String query = """
				SELECT COUNT(DISTINCT assigned_parking_spot_id)
				FROM Reservations
				WHERE assigned_parking_spot_id IS NOT NULL
				AND statusEnum = 'active'
				AND reservation_Date >= ?
				AND reservation_Date <= ?
				""";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setDate(1, Date.valueOf(startTime.toLocalDate()));
			stmt.setDate(2, Date.valueOf(endTime.toLocalDate()));

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return 0;
	}

	/**
	 * Counts the number of parking spots currently marked as occupied.
	 *
	 * @return the number of occupied parking spots
	 * @throws SQLException if a database error occurs
	 */
	private int getCurrentlyOccupiedSpots() throws SQLException {
		String query = "SELECT COUNT(*) FROM ParkingSpot WHERE isOccupied = true";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return 0;
	}

	/**
	 * Generates a random 6-digit parking code.
	 *
	 * @return a random code between 100000 and 999999
	 */
	private int generateParkingCode() {
		Random random = new Random();
		return 100000 + random.nextInt(900000);
	}

	/**
	 * Retrieves the database user ID for a given username.
	 *
	 * @param userName the username to look up
	 * @return the user's ID if found, or -1 if not found or error occurred
	 */
	private int getUserID(String userName) {
		String qry = "SELECT User_ID FROM users WHERE UserName = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setString(1, userName);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("User_ID");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting user ID: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return -1;
	}

	/**
	 * Checks whether a new reservation can be made based on current availability
	 * and system threshold.
	 *
	 * @return true if a reservation is allowed, false otherwise
	 */
	private boolean canMakeReservation() {
		int availableSpots = getAvailableParkingSpots();
		return availableSpots >= (TOTAL_PARKING_SPOTS * AVAILABILITY_THRESHOLD);
	}

	/**
	 * Retrieves the ID of the first available parking spot.
	 *
	 * @return the ID of an available spot, or -1 if none found
	 */
	private int getAvailableParkingSpotID() {
		String qry = "SELECT ParkingSpot_ID FROM ParkingSpot WHERE isOccupied = false LIMIT 1";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("ParkingSpot_ID");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting available spot ID: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return -1;
	}

	/**
	 * Checks if a specific parking spot is currently available (not occupied).
	 *
	 * @param spotID the ID of the spot to check
	 * @return true if the spot is available, false otherwise
	 */
	private boolean isParkingSpotAvailable(int spotID) {
		String qry = "SELECT isOccupied FROM ParkingSpot WHERE ParkingSpot_ID = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setInt(1, spotID);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return !rs.getBoolean("isOccupied");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error checking spot availability: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return false;
	}

	/**
	 * Updates the status of a parking spot to reflect whether it is occupied or
	 * not.
	 *
	 * @param spotID     the ID of the spot
	 * @param isOccupied true to mark as occupied, false for available
	 */
	private void updateParkingSpotStatus(int spotID, boolean isOccupied) {
		String qry = "UPDATE ParkingSpot SET isOccupied = ? WHERE ParkingSpot_ID = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setBoolean(1, isOccupied);
			stmt.setInt(2, spotID);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Error updating parking spot status: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
	}

	/**
	 * Updates the occupancy of a parking spot using the existing status update
	 * logic.
	 *
	 * @param spotId     the ID of the spot
	 * @param isOccupied true to mark as occupied, false otherwise
	 * @throws SQLException if a database error occurs
	 */
	private void updateSpotOccupancy(int spotId, boolean isOccupied) throws SQLException {
		updateParkingSpotStatus(spotId, isOccupied);
	}

	/**
	 * Updates the status of a reservation in the database.
	 *
	 * @param reservationCode the reservation code to update
	 * @param status          the new status to assign (e.g., "active", "cancelled")
	 */
	private void updateReservationStatus(int reservationCode, String status) {
		String qry = "UPDATE Reservations SET statusEnum = ? WHERE Reservation_code = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setString(1, status);
			stmt.setInt(2, reservationCode);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Error updating reservation status: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
	}

	/**
	 * Sends a simulated notification to the user about a late exit, including
	 * contact info from the database.
	 *
	 * @param userID the ID of the user to notify
	 */
	private void sendLateExitNotification(int userID) {
		String qry = "SELECT Email, Phone, Name FROM users WHERE User_ID = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setInt(1, userID);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					String email = rs.getString("Email");
					String phone = rs.getString("Phone");
					String name = rs.getString("Name");

					System.out.println("Sending late exit notification to " + name + " at email: " + email
							+ " and phone: " + phone);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error sending late notification: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
	}

	/**
	 * Checks if a given username is available (not already used in the system).
	 *
	 * @param userName the username to check
	 * @return true if the username is available, false otherwise
	 */
	private boolean isUsernameAvailable(String userName) {
		String checkQry = "SELECT COUNT(*) FROM users WHERE UserName = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(checkQry)) {
			stmt.setString(1, userName);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) == 0;
				}
			}
		} catch (SQLException e) {
			System.out.println("Error checking username availability: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return false;
	}
}