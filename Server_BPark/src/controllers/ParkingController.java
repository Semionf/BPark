
package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.ParkingOrder;
import common.ParkingSubscriber;
import server.DBController;
import services.EmailService;

/**
 * Enhanced ParkingController with proper 40% rule implementation Manages
 * parking operations including reservations, entries, and exits
 * 
 * IMPORTANT: 40% Rule Implementation (STRICT) - For reservations: Must have
 * MORE than 40% spots available (>4 spots out of 10) - For spontaneous parking:
 * Can use ANY available spot (no restriction) - This ensures walk-in customers
 * always have access to parking
 */
public class ParkingController {

	/** Unique ID of the subscriber */
	private int subscriberID;

	/** First name of the subscriber */
	private String firstName;
	/** Phone number of the subscriber */
	private String phoneNumber;
	/** Email address of the subscriber */
	private String email;
	/** Car number of the subscriber */
	private String carNumber;
	/** Unique subscriber parking code */
	private String subscriberCode;
	/** Type of user */
	private String userType;
	/** Active connection to the database */
	private Connection conn;
	/** Flag indicating if the last operation succeeded */
	public int successFlag;

	// Constants
	/** Total number of parking spots available */
	private static final int TOTAL_PARKING_SPOTS = 10;
	/**
	 * Minimum percentage of available spots required to allow new reservations
	 * (40%)
	 */
	private static final double RESERVATION_THRESHOLD = 0.4; // 40% rule
	/** Default number of parking hours for a session */
	private static final int DEFAULT_PARKING_HOURS = 4;
	/** Minimum allowed extension in hours for a parking session */
	private static final int MIN_EXTENSION_HOURS = 1;
	/** Maximum allowed extension in hours for a parking session */
	private static final int MAX_EXTENSION_HOURS = 4;

	/** Automatic service that monitors late reservations and parking sessions */
	private SimpleAutoCancellationService autoCancellationService;

	/**
	 * Constructor – initializes database connection and auto-monitoring service. If
	 * the connection is successful, it also starts the auto-cancellation service.
	 *
	 * @param dbname The name of the database to connect to.
	 * @param pass   The password for database authentication.
	 */
	public ParkingController(String dbname, String pass) {
		DBController.initializeConnection(dbname, pass);
		successFlag = DBController.getInstance().getSuccessFlag();
		autoCancellationService = new SimpleAutoCancellationService(this);

		if (successFlag == 1) {
			startAutoCancellationService();
		}
	}

	/**
	 * Enum representing user roles in the system. Each role is mapped to a database
	 * value (e.g., "sub", "emp", "mng").
	 */
	public enum UserRole {
		/** Subscriber role (regular user) */
		SUBSCRIBER("sub"),
		/** Attendant role (employee at the parking lot) */
		ATTENDANT("emp"),
		/** Manager role (admin-level access) */
		MANAGER("mng");

		/** Role value as stored in the database */
		private final String dbValue;

		/**
		 * Constructor for the enum.
		 * 
		 * @param dbValue The string value stored in the database.
		 */
		UserRole(String dbValue) {
			this.dbValue = dbValue;
		}

		/**
		 * Gets the database value for this role.
		 * 
		 * @return The DB value as string.
		 */
		public String getDbValue() {
			return dbValue;
		}

		/**
		 * Converts a database value into a matching {@link UserRole}.
		 * 
		 * @param dbValue The string value from the database.
		 * @return The matching UserRole, or null if none found.
		 */
		public static UserRole fromDbValue(String dbValue) {
			for (UserRole role : values()) {
				if (role.dbValue.equals(dbValue)) {
					return role;
				}
			}
			return null;
		}
	}

	// ========== GETTERS AND SETTERS ==========

	/**
	 * Gets the subscriber ID.
	 * 
	 * @return the subscriber ID
	 */
	public int getSubscriberID() {
		return subscriberID;
	}

	/**
	 * Gets the subscriber's first name.
	 * 
	 * @return the first name
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Gets the subscriber's phone number.
	 * 
	 * @return the phone number
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * Gets the subscriber's email address.
	 * 
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Gets the car number (license plate) associated with the subscriber.
	 * 
	 * @return the car number
	 */
	public String getCarNumber() {
		return carNumber;
	}

	/**
	 * Gets the subscriber's unique parking code.
	 * 
	 * @return the subscriber code
	 */
	public String getSubscriberCode() {
		return subscriberCode;
	}

	/**
	 * Gets the type of user (e.g., subscriber, attendant).
	 * 
	 * @return the user type
	 */
	public String getUserType() {
		return userType;
	}

	/**
	 * Gets the active database connection.
	 * 
	 * @return the connection
	 */
	public Connection getConnection() {
		return conn;
	}

	/**
	 * Sets the subscriber ID.
	 * 
	 * @param subscriberID the subscriber ID to set
	 */
	public void setSubscriberID(int subscriberID) {
		this.subscriberID = subscriberID;
	}

	/**
	 * Sets the subscriber's first name.
	 * 
	 * @param firstName the first name to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Sets the subscriber's phone number.
	 * 
	 * @param phoneNumber the phone number to set
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * Sets the subscriber's email address.
	 * 
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Sets the car number (license plate).
	 * 
	 * @param carNumber the car number to set
	 */
	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}

	/**
	 * Sets the subscriber's unique parking code.
	 * 
	 * @param subscriberCode the subscriber code to set
	 */
	public void setSubscriberCode(String subscriberCode) {
		this.subscriberCode = subscriberCode;
	}

	/**
	 * Sets the user type (e.g., subscriber, attendant).
	 * 
	 * @param userType the user type to set
	 */
	public void setUserType(String userType) {
		this.userType = userType;
	}

	// ========== SERVICE MANAGEMENT ==========

	/**
	 * Starts the automatic parking monitoring service. Monitors preorder
	 * reservations and active parkings every 30 seconds: Auto-cancels late preorder
	 * reservations (15+ minutes late). Notifies users of late active parking
	 * sessions.
	 */
	public void startAutoCancellationService() {
		if (autoCancellationService != null) {
			autoCancellationService.startService();
			System.out.println(" Auto-monitoring service started:");
			System.out.println("   - Monitoring preorder reservations (auto-cancel after 15 min)");
			System.out.println("   - Monitoring active parkings (notify late pickups after 15 min)");
		}
	}

	/**
	 * Stops the automatic parking monitoring service if it is running.
	 */
	public void stopAutoCancellationService() {
		if (autoCancellationService != null) {
			autoCancellationService.stopService();
			System.out.println("Auto-monitoring service stopped");
		}
	}

	/**
	 * Shuts down the auto-cancellation service completely. Should be called during
	 * system shutdown to release resources.
	 */
	public void shutdown() {
		if (autoCancellationService != null) {
			autoCancellationService.shutdown();
		}
	}

	// ========== AUTHENTICATION & USER MANAGEMENT ==========

	/**
	 * Checks user login credentials against the database.
	 *
	 * @param userName The username entered by the user.
	 * @param userCode The user ID entered by the user.
	 * @return The user's role type if valid, otherwise "None".
	 */
	public String checkLogin(String userName, String userCode) {
		String qry = "SELECT UserTypeEnum FROM users WHERE UserName = ? AND User_ID = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setString(1, userName);
			stmt.setString(2, userCode);
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
	 * Retrieves full user information by username.
	 *
	 * @param userName the username of the user
	 * @return a ParkingSubscriber object containing user details, or null if not
	 *         found
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
	 * Gets the UserRole of a user based on their username.
	 *
	 * @param userName the username to look up
	 * @return the user's role as a UserRole enum, or null if not found
	 */
	private UserRole getUserRole(String userName) {
		String qry = "SELECT UserTypeEnum FROM users WHERE UserName = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setString(1, userName);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					String userType = rs.getString("UserTypeEnum");
					return UserRole.fromDbValue(userType);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting user role: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return null;
	}

	/**
	 * Checks if a user has the required role.
	 *
	 * @param userName     the username to check
	 * @param requiredRole the role to compare against
	 * @return true if the user has the required role, false otherwise
	 */
	private boolean hasRole(String userName, UserRole requiredRole) {
		UserRole userRole = getUserRole(userName);
		return userRole == requiredRole;
	}

	// ========== 40% RULE IMPLEMENTATION ==========

	/**
	 * Checks if a reservation can be made for the current time based on the general
	 * 40% rule.
	 *
	 * @return true if enough spots are available to allow reservation, false
	 *         otherwise
	 */
	public boolean canMakeReservation() {
		int availableSpots = getAvailableParkingSpots();
		return availableSpots >= (TOTAL_PARKING_SPOTS * RESERVATION_THRESHOLD);
	}

	/**
	 * Checks if a reservation can be made for a specific time slot using a strict
	 * version of the 40% rule. Strict Rule: There must be more than 40% of spots
	 * available throughout the entire time slot.
	 *
	 * @param startTime the start of the requested reservation time slot
	 * @param endTime   the end of the requested reservation time slot
	 * @return true if reservation is allowed, false if blocked by the rule
	 */
	public boolean canMakeReservationForTimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
		int availableSpots = getAvailableSpotsForTimeSlot(startTime, endTime);
		int requiredSpots = (int) Math.ceil(TOTAL_PARKING_SPOTS * RESERVATION_THRESHOLD);

		// STRICT: Must have MORE than 40%, not just exactly 40%
		boolean allowed = availableSpots > requiredSpots;

		System.out.println("Time slot check (STRICT): " + startTime + " to " + endTime);
		System.out.println("Available spots: " + availableSpots + ", Required: >" + requiredSpots);
		System.out.println("Result: " + (allowed ? "ALLOWED" : "BLOCKED"));

		return allowed;
	}

	/**
	 * Calculates how many parking spots are available during a specific time slot.
	 * Checks every 15-minute interval in the range and returns the *minimum* number
	 * of available spots during that time.
	 *
	 * @param startTime the start of the time slot
	 * @param endTime   the end of the time slot
	 * @return the minimum number of available spots during the given period
	 */
	public int getAvailableSpotsForTimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
		// Get all reservations that overlap with our period
		String qry = """
				SELECT ParkingSpot_ID, Estimated_start_time, Estimated_end_time
				FROM parkinginfo
				WHERE statusEnum IN ('preorder', 'active')
				AND ParkingSpot_ID IS NOT NULL
				AND Estimated_start_time < ?
				AND Estimated_end_time > ?
				ORDER BY Estimated_start_time
				""";

		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setTimestamp(1, Timestamp.valueOf(endTime));
			stmt.setTimestamp(2, Timestamp.valueOf(startTime));

			List<ReservationSlot> overlappingReservations = new ArrayList<>();

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					overlappingReservations.add(new ReservationSlot(rs.getInt("ParkingSpot_ID"),
							rs.getTimestamp("Estimated_start_time").toLocalDateTime(),
							rs.getTimestamp("Estimated_end_time").toLocalDateTime()));
				}
			}

			// Now check every 15-minute interval in our period to find minimum availability
			int minAvailable = TOTAL_PARKING_SPOTS;
			LocalDateTime checkTime = startTime;

			while (checkTime.isBefore(endTime)) {
				// Count how many spots are occupied at this moment
				Set<Integer> occupiedSpots = new HashSet<>();
				for (ReservationSlot reservation : overlappingReservations) {
					if (!checkTime.isBefore(reservation.startTime) && checkTime.isBefore(reservation.endTime)) {
						occupiedSpots.add(reservation.spotId);
					}
				}

				int availableAtThisTime = TOTAL_PARKING_SPOTS - occupiedSpots.size();
				minAvailable = Math.min(minAvailable, availableAtThisTime);

				// Move to next 15-minute interval
				checkTime = checkTime.plusMinutes(15);
			}

			System.out.println(
					"Time slot " + startTime + " to " + endTime + " has minimum " + minAvailable + " spots available");
			return minAvailable;

		} catch (SQLException e) {
			System.out.println("Error getting available spots for time slot: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return 0;
	}

	/**
	 * Helper class that represents a single parking reservation's time slot and
	 * spot.
	 */
	private static class ReservationSlot {
		/** ID of the parking spot associated with this reservation slot */
		int spotId;

		/** Start time of the reservation period */
		LocalDateTime startTime;

		/** End time of the reservation period */
		LocalDateTime endTime;

		/**
		 * Constructs a ReservationSlot.
		 *
		 * @param spotId    the ID of the parking spot
		 * @param startTime reservation start time
		 * @param endTime   reservation end time
		 */
		ReservationSlot(int spotId, LocalDateTime startTime, LocalDateTime endTime) {
			this.spotId = spotId;
			this.startTime = startTime;
			this.endTime = endTime;
		}
	}

	// ========== RESERVATION MANAGEMENT ==========

	/**
	 * Makes a new parking reservation for a user, with full validation and strict
	 * 40% availability rule.
	 *
	 * @param userName               the username of the subscriber
	 * @param reservationDateTimeStr the requested reservation time (in string
	 *                               format, e.g. "2025-07-12 15:00")
	 * @return a message indicating success or failure, with reservation details if
	 *         successful
	 */
	public String makeReservation(String userName, String reservationDateTimeStr) {
		Connection conn = DBController.getInstance().getConnection();
		try {
			// Parse the datetime string
			LocalDateTime reservationDateTime = parseDateTime(reservationDateTimeStr);

			// Validate reservation timing
			LocalDateTime now = LocalDateTime.now();
			if (reservationDateTime.isBefore(now.plusHours(24))) {
				return "Reservation must be at least 24 hours in advance";
			}
			if (reservationDateTime.isAfter(now.plusDays(7))) {
				return "Reservation cannot be more than 7 days in advance";
			}

			// Get user ID
			int userID = getUserID(userName);
			if (userID == -1) {
				return "User not found";
			}

			// Calculate end time
			LocalDateTime estimatedEndTime = reservationDateTime.plusHours(DEFAULT_PARKING_HOURS);

			// Check 40% rule for the specific time slot (STRICT: need MORE than 40%)
			if (!canMakeReservationForTimeSlot(reservationDateTime, estimatedEndTime)) {
				return "Not enough available spots for reservation at "
						+ reservationDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
						+ ". Must have more than 40% spots available (need > 4 spots free)";
			}

			// Find available spot for the time slot
			int parkingSpotID = findAvailableSpotForTimeSlot(reservationDateTime, estimatedEndTime);
			if (parkingSpotID == -1) {
				return "No parking spots available for the requested time slot";
			}

			// Create reservation
			String qry = """
					INSERT INTO parkinginfo
					(ParkingSpot_ID, User_ID, Date_Of_Placing_Order, Estimated_start_time,
					 Estimated_end_time, IsOrderedEnum, IsLate, IsExtended, statusEnum)
					VALUES (?, ?, NOW(), ?, ?, 'yes', 'no', 'no', 'preorder')
					""";

			try (PreparedStatement stmt = conn.prepareStatement(qry, PreparedStatement.RETURN_GENERATED_KEYS)) {
				stmt.setInt(1, parkingSpotID);
				stmt.setInt(2, userID);
				stmt.setTimestamp(3, Timestamp.valueOf(reservationDateTime));
				stmt.setTimestamp(4, Timestamp.valueOf(estimatedEndTime));
				stmt.executeUpdate();

				try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						int reservationCode = generatedKeys.getInt(1);
						System.out.println("New preorder reservation created: " + reservationCode);

						// Send email confirmation
						ParkingSubscriber user = getUserInfo(userName);
						if (user != null && user.getEmail() != null) {
							String formattedDateTime = reservationDateTime
									.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
							EmailService.sendReservationConfirmation(user.getEmail(), user.getFirstName(),
									String.valueOf(reservationCode), formattedDateTime, "Spot " + parkingSpotID);
						}

						return "Reservation confirmed for "
								+ reservationDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
								+ ". Confirmation code: " + reservationCode + ". Spot: " + parkingSpotID;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error making reservation: " + e.getMessage());
			return "Reservation failed: " + e.getMessage();
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return "Reservation failed";
	}

	/**
	 * Cancels a reservation by its code (used when a subscriber cancels).
	 *
	 * @param reservationCode the unique ID of the reservation to cancel
	 * @return result message indicating success or failure
	 */
	public String cancelReservation(int reservationCode) {
		return cancelReservationInternal(reservationCode, "User requested cancellation");
	}
	

	/**
	 * Cancels a reservation by subscriber and code (used when an attendant cancels
	 * on behalf).
	 *
	 * @param subscriberUserName the subscriber’s username
	 * @param reservationCode    the reservation ID to cancel
	 * @return result message indicating success or failure
	 */
	public String cancelReservation(String subscriberUserName, int reservationCode) {
		return cancelReservationInternal(reservationCode, "User requested cancellation");
	}
	
	/**
	 * Cancels a parking reservation if the given user is the owner of the reservation.
	 * First checks whether the reservation belongs to the user, and if so, proceeds with the cancellation.
	 *
	 * @param reservationCode the unique code identifying the reservation
	 * @param userID the ID of the user requesting the cancellation
	 * @return a message indicating whether the cancellation was successful or access was denied
	 */
	public String cancelReservation(int reservationCode, int userID) {
		// Validate parking order ownership
		if (!validateParkingOrderOwnership(reservationCode, userID)) {
			return "Access denied: This reservation does not belong to your account.";
		}

		return cancelReservationInternal(reservationCode, "User requested cancellation");
	}

	/**
	 * Internal helper method that cancels a reservation and notifies the user.
	 *
	 * @param reservationCode the ID of the reservation to cancel
	 * @param reason          the reason for cancellation (for logging purposes)
	 * @return a status message about the cancellation
	 */
	private String cancelReservationInternal(int reservationCode, String reason) {
		// Get reservation info for notification
		String getUserQry = """
				SELECT u.Email, u.Name, pi.statusEnum, pi.ParkingSpot_ID
				FROM parkinginfo pi
				JOIN users u ON pi.User_ID = u.User_ID
				WHERE pi.ParkingInfo_ID = ?
				""";

		String userEmail = null;
		String userName = null;
		String currentStatus = null;
		Integer spotId = null;

		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(getUserQry)) {
			stmt.setInt(1, reservationCode);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					userEmail = rs.getString("Email");
					userName = rs.getString("Name");
					currentStatus = rs.getString("statusEnum");
					spotId = rs.getObject("ParkingSpot_ID", Integer.class);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting reservation info: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		// Update reservation status
		String qry = """
				UPDATE parkinginfo
				SET statusEnum = 'cancelled'
				WHERE ParkingInfo_ID = ? AND statusEnum IN ('preorder', 'active')
				""";

		conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setInt(1, reservationCode);
			int rowsUpdated = stmt.executeUpdate();

			if (rowsUpdated > 0) {
				// Free up the spot
				if (spotId != null) {
					updateParkingSpotStatus(spotId, false);
				}

				// Send email notification
				if (userEmail != null && userName != null) {
					EmailService.sendReservationCancelled(userEmail, userName, String.valueOf(reservationCode));
				}

				System.out.println("Reservation " + reservationCode + " cancelled - " + reason);
				return "Reservation cancelled successfully";
			}
		} catch (SQLException e) {
			System.out.println("Error cancelling reservation: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		return "Reservation not found or already cancelled/finished";
	}

	// ========== PARKING ENTRY/EXIT ==========

	/**
	 * Handles spontaneous parking entry. Does not enforce 40% rule.
	 *
	 * @param userID the ID of the user entering the parking
	 * @return message indicating success or reason for failure
	 */
	public String enterParking(int userID) {
		// Check if user already has active parking
		String checkActiveQry = "SELECT COUNT(*) FROM parkinginfo WHERE User_ID = ? AND statusEnum = 'active'";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement activeStmt = conn.prepareStatement(checkActiveQry)) {
			activeStmt.setInt(1, userID);
			try (ResultSet rs = activeStmt.executeQuery()) {
				if (rs.next() && rs.getInt(1) > 0) {
					return "You already have an active parking session.";
				}
			}
		} catch (SQLException e) {
			System.out.println("Error checking active parking: " + e.getMessage());
			return "Could not verify active parking.";
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		// Check if ANY spot is available (NO 40% restriction for spontaneous!)
		if (isParkingFull()) {
			return "Parking is full. Try later.";
		}

		// Find first available parking spot
		int spotID = getAvailableParkingSpotID();
		if (spotID == -1) {
			return "No parking spots available.";
		}

		// Insert new active parking record
		String insertQry = """
				INSERT INTO parkinginfo
				(ParkingSpot_ID, User_ID, Actual_start_time, Estimated_start_time, Estimated_end_time,
				 IsOrderedEnum, IsLate, IsExtended, statusEnum)
				VALUES (?, ?, NOW(), NOW(), NOW() + INTERVAL 4 HOUR, 'no', 'no', 'no', 'active')
				""";

		conn = DBController.getInstance().getConnection();
		try (PreparedStatement insertStmt = conn.prepareStatement(insertQry, PreparedStatement.RETURN_GENERATED_KEYS)) {
			insertStmt.setInt(1, spotID);
			insertStmt.setInt(2, userID);
			insertStmt.executeUpdate();

			try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					int parkingCode = generatedKeys.getInt(1);

					// Mark spot as occupied
					updateParkingSpotStatus(spotID, true);

					return "Entry successful. Parking code: " + parkingCode + ". Spot: " + spotID;
				} else {
					return "Entry failed: No parking code generated.";
				}
			}
		} catch (SQLException e) {
			System.out.println("Error handling entry: " + e.getMessage());
			return "Entry failed due to database error.";
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
	}

	/**
	 * Handles parking entry using a reservation code. Validates time and activates
	 * the reservation.
	 *
	 * @param reservationCode the reservation ID
	 * @return message indicating success or reason for rejection
	 */
	public String enterParkingWithReservation(int reservationCode) {
		String checkQry = """
				SELECT pi.*, u.User_ID,
				       TIMESTAMPDIFF(MINUTE, pi.Estimated_start_time, NOW()) as minutes_since_start
				FROM parkinginfo pi
				JOIN users u ON pi.User_ID = u.User_ID
				WHERE pi.ParkingInfo_ID = ? AND pi.statusEnum = 'preorder'
				""";

		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(checkQry)) {
			stmt.setInt(1, reservationCode);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					int minutesSinceStart = rs.getInt("minutes_since_start");
					int parkingSpotID = rs.getInt("ParkingSpot_ID");

					LocalDateTime estimatedStartTime = rs.getTimestamp("Estimated_start_time").toLocalDateTime();
					LocalDateTime now = LocalDateTime.now();

					// Check if it's today
					if (!estimatedStartTime.toLocalDate().equals(now.toLocalDate())) {
						if (estimatedStartTime.isBefore(now)) {
							cancelReservation(reservationCode);
							return "Reservation expired (wrong date).";
						} else {
							return "Reservation is for a future date.";
						}
					}

					// Check if within 15 min after reserved time
					if (minutesSinceStart > 15) {
						cancelReservation(reservationCode);
						return "Reservation expired: arrived more than 15 min late.";
					}

					// Update reservation to active
					String updateQry = """
							UPDATE parkinginfo
							SET statusEnum = 'active', Actual_start_time = NOW()
							WHERE ParkingInfo_ID = ?
							""";

					try (PreparedStatement updateStmt = conn.prepareStatement(updateQry)) {
						updateStmt.setInt(1, reservationCode);
						updateStmt.executeUpdate();

						// Mark spot as occupied
						updateParkingSpotStatus(parkingSpotID, true);

						System.out.println("Reservation " + reservationCode + " activated");
						return "Entry successful! Reservation activated. Parking code: " + reservationCode + ". Spot: "
								+ parkingSpotID;
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Error handling reservation entry: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return "Invalid reservation code or reservation not in preorder status.";
	}

	/**
	 * Handles parking exit using a parking code. Marks parking as finished,
	 * releases the spot, and sends notification if late.
	 *
	 * @param parkingCodeStr the parking session code
	 * @return message confirming exit or explaining failure
	 */
	public String exitParking(String parkingCodeStr) {
		Connection conn = DBController.getInstance().getConnection();

		try {
			int parkingCode = Integer.parseInt(parkingCodeStr);
			String qry = """
					SELECT pi.*, ps.ParkingSpot_ID
					FROM parkinginfo pi
					JOIN parkingspot ps ON pi.ParkingSpot_ID = ps.ParkingSpot_ID
					WHERE pi.ParkingInfo_ID = ? AND pi.statusEnum = 'active'
					""";

			try (PreparedStatement stmt = conn.prepareStatement(qry)) {
				stmt.setInt(1, parkingCode);
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						int parkingInfoID = rs.getInt("ParkingInfo_ID");
						int spotID = rs.getInt("ParkingSpot_ID");
						Timestamp estimatedEndTime = rs.getTimestamp("Estimated_end_time");
						int userID = rs.getInt("User_ID");

						LocalDateTime now = LocalDateTime.now();
						LocalDateTime estimatedEnd = estimatedEndTime.toLocalDateTime();

						// Check if late
						boolean isLate = now.isAfter(estimatedEnd);

						// Update parking info
						String updateQry = """
								UPDATE parkinginfo
								SET Actual_end_time = ?, IsLate = ?, statusEnum = 'finished'
								WHERE ParkingInfo_ID = ?
								""";

						try (PreparedStatement updateStmt = conn.prepareStatement(updateQry)) {
							updateStmt.setTimestamp(1, Timestamp.valueOf(now));
							updateStmt.setString(2, isLate ? "yes" : "no");
							updateStmt.setInt(3, parkingInfoID);
							updateStmt.executeUpdate();

							// Free the parking spot
							updateParkingSpotStatus(spotID, false);

							if (isLate) {
								sendLateExitNotification(userID);
								return "Exit successful. You were late - please arrive on time for future reservations";
							}

							return "Exit successful. Thank you for using ParkB!";
						}
					}
				}
			}
		} catch (NumberFormatException e) {
			return "Invalid parking code format";
		} catch (SQLException e) {
			System.out.println("Error handling exit: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return "Invalid parking code or already exited";
	}
	
	/**
	 * Handles the user's request to exit the parking lot by validating the parking code 
	 * and ownership. If the code is valid and belongs to the user, proceeds to call the 
	 * original exit method.
	 *
	 * @param parkingCodeStr the parking code as a string input from the user
	 * @param userID the ID of the user attempting to exit
	 * @return a message indicating the result of the exit attempt:
	 *         - success message from the original method,
	 *         - access denied if ownership check fails,
	 *         - or an error message if the parking code format is invalid
	 */
	public String exitParking(String parkingCodeStr, int userID) {
		try {
			int parkingCode = Integer.parseInt(parkingCodeStr);

			// Validate parking order ownership
			if (!validateParkingOrderOwnership(parkingCode, userID)) {
				return "Access denied: This parking session does not belong to your account.";
			}

			// Call the original method if validation passes
			return exitParking(parkingCodeStr);
		} catch (NumberFormatException e) {
			return "Invalid parking code format";
		}
	}

	/**
	 * Extends an active parking session by a specified number of hours. Only one
	 * extension is allowed, and it must not conflict with future reservations.
	 *
	 * @param parkingCodeStr  the active parking session code
	 * @param additionalHours number of hours to extend (must be between 1–4)
	 * @return result message indicating success or reason for failure
	 */
	public String extendParkingTime(String parkingCodeStr, int additionalHours) {
		if (additionalHours < MIN_EXTENSION_HOURS || additionalHours > MAX_EXTENSION_HOURS) {
			return "Can only extend parking by " + MIN_EXTENSION_HOURS + "-" + MAX_EXTENSION_HOURS + " hours.";
		}

		Connection conn = DBController.getInstance().getConnection();
		try {
			int parkingCode = Integer.parseInt(parkingCodeStr);

			// Get current parking info
			String getUserQry = """
					SELECT pi.*, u.Email, u.Name
					FROM parkinginfo pi
					JOIN users u ON pi.User_ID = u.User_ID
					WHERE pi.ParkingInfo_ID = ? AND pi.statusEnum = 'active'
					""";

			try (PreparedStatement stmt = conn.prepareStatement(getUserQry)) {
				stmt.setInt(1, parkingCode);

				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						String isExtended = rs.getString("IsExtended");

						// Block if already extended
						if ("yes".equalsIgnoreCase(isExtended)) {
							return "Cannot extend again: You already extended this active parking session.";
						}

						Timestamp currentEstimatedEnd = rs.getTimestamp("Estimated_end_time");
						String userEmail = rs.getString("Email");
						String userName = rs.getString("Name");
						int parkingSpotId = rs.getInt("ParkingSpot_ID");

						LocalDateTime newEstimatedEnd = currentEstimatedEnd.toLocalDateTime()
								.plusHours(additionalHours);

						// Check for conflicting reservations
						String conflictCheckQry = """
								SELECT 1 FROM parkinginfo
								WHERE ParkingSpot_ID = ?
								  AND statusEnum = 'preorder'
								  AND Estimated_start_time > ?
								  AND Estimated_start_time < ?
								""";

						try (PreparedStatement checkStmt = conn.prepareStatement(conflictCheckQry)) {
							checkStmt.setInt(1, parkingSpotId);
							checkStmt.setTimestamp(2, currentEstimatedEnd);
							checkStmt.setTimestamp(3, Timestamp.valueOf(newEstimatedEnd));

							try (ResultSet conflictRs = checkStmt.executeQuery()) {
								if (conflictRs.next()) {
									return "Cannot extend parking: A reservation is scheduled during the extension period.";
								}
							}
						}

						// Update parking time
						String updateQry = """
								UPDATE parkinginfo
								SET Estimated_end_time = ?, IsExtended = 'yes'
								WHERE ParkingInfo_ID = ?
								""";

						try (PreparedStatement updateStmt = conn.prepareStatement(updateQry)) {
							updateStmt.setTimestamp(1, Timestamp.valueOf(newEstimatedEnd));
							updateStmt.setInt(2, parkingCode);
							updateStmt.executeUpdate();

							// Send email confirmation
							if (userEmail != null && userName != null) {
								EmailService.sendExtensionConfirmation(userEmail, userName, parkingCodeStr,
										additionalHours, newEstimatedEnd.toString());
							}

							return "Parking time extended by " + additionalHours + " hours until " + newEstimatedEnd;
						}
					}
				}
			}
		} catch (NumberFormatException e) {
			return "Invalid parking code format.";
		} catch (SQLException e) {
			System.out.println("Error extending parking time: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		return "Invalid parking code or parking session not active.";
	}
	
	/**
	 * Extends a parking session if the user owns it and the number of hours is valid.
	 *
	 * @param parkingCodeStr the parking code as a string
	 * @param additionalHours number of hours to add
	 * @param userID the user's ID
	 * @return a message with the result (success, error, or access denied)
	 */
	public String extendParkingTime(String parkingCodeStr, int additionalHours, int userID) {
		if (additionalHours < MIN_EXTENSION_HOURS || additionalHours > MAX_EXTENSION_HOURS) {
			return "Can only extend parking by " + MIN_EXTENSION_HOURS + "-" + MAX_EXTENSION_HOURS + " hours.";
		}

		try {
			int parkingCode = Integer.parseInt(parkingCodeStr);

			// Validate parking order ownership
			if (!validateParkingOrderOwnership(parkingCode, userID)) {
				return "Access denied: This parking order does not belong to your account.";
			}

			// Call the original method if validation passes
			return extendParkingTime(parkingCodeStr, additionalHours);
		} catch (NumberFormatException e) {
			return "Invalid parking code format.";
		}
	}

	// ========== PARKING QUERIES ==========

	/**
	 * Retrieves the number of currently available parking spots.
	 *
	 * @return number of free (not occupied) parking spots
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
	 * Checks if the parking lot is currently full.
	 *
	 * @return true if there are no available spots, false otherwise
	 */
	public boolean isParkingFull() {
		return getAvailableParkingSpots() <= 0;
	}

	/**
	 * Retrieves the full parking history for a given user.
	 *
	 * @param userName the username to search history for
	 * @return a list of {@link ParkingOrder} objects representing past sessions
	 */
	public ArrayList<ParkingOrder> getParkingHistory(String userName) {
		ArrayList<ParkingOrder> history = new ArrayList<>();
		String qry = """
				SELECT pi.*, ps.ParkingSpot_ID
				FROM parkinginfo pi
				JOIN users u ON pi.User_ID = u.User_ID
				JOIN parkingspot ps ON pi.ParkingSpot_ID = ps.ParkingSpot_ID
				WHERE u.UserName = ?
				ORDER BY pi.Date_Of_Placing_Order DESC
				""";

		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setString(1, userName);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					ParkingOrder order = new ParkingOrder();
					order.setOrderID(rs.getInt("ParkingInfo_ID"));
					order.setParkingCode(String.valueOf(rs.getInt("ParkingInfo_ID")));
					order.setOrderType(rs.getString("IsOrderedEnum"));
					order.setSpotNumber("Spot " + rs.getInt("ParkingSpot_ID"));

					// Convert Timestamps to LocalDateTime
					Timestamp actualStart = rs.getTimestamp("Actual_start_time");
					Timestamp actualEnd = rs.getTimestamp("Actual_end_time");
					Timestamp estimatedEnd = rs.getTimestamp("Estimated_end_time");
					Timestamp estimatedStart = rs.getTimestamp("Estimated_start_time");

					if (actualStart != null) {
						order.setEntryTime(actualStart.toLocalDateTime());
					}
					if (actualEnd != null) {
						order.setExitTime(actualEnd.toLocalDateTime());
					}
					if (estimatedEnd != null) {
						order.setExpectedExitTime(estimatedEnd.toLocalDateTime());
					}
					if (estimatedStart != null) {
						order.setEstimatedStartTime(estimatedStart.toLocalDateTime());
					}

					order.setLate("yes".equals(rs.getString("IsLate")));
					order.setExtended("yes".equals(rs.getString("IsExtended")));
					order.setStatus(rs.getString("statusEnum"));

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
	 * Retrieves all active parking sessions (status = 'active').
	 *
	 * @return a list of currently active ParkingOrder entries
	 */
	public ArrayList<ParkingOrder> getActiveParkings() {
		ArrayList<ParkingOrder> activeParkings = new ArrayList<>();
		String qry = """
				SELECT pi.*, u.Name, ps.ParkingSpot_ID
				FROM parkinginfo pi
				JOIN users u ON pi.User_ID = u.User_ID
				JOIN parkingspot ps ON pi.ParkingSpot_ID = ps.ParkingSpot_ID
				WHERE pi.statusEnum = 'active'
				ORDER BY pi.Actual_start_time
				""";

		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					ParkingOrder order = new ParkingOrder();
					order.setOrderID(rs.getInt("ParkingInfo_ID"));
					order.setParkingCode(String.valueOf(rs.getInt("ParkingInfo_ID")));
					order.setOrderType(rs.getString("IsOrderedEnum"));
					order.setSubscriberName(rs.getString("Name"));
					order.setSpotNumber("Spot " + rs.getInt("ParkingSpot_ID"));

					Timestamp actualStart = rs.getTimestamp("Actual_start_time");
					Timestamp estimatedEnd = rs.getTimestamp("Estimated_end_time");

					if (actualStart != null) {
						order.setEntryTime(actualStart.toLocalDateTime());
					}
					if (estimatedEnd != null) {
						order.setExpectedExitTime(estimatedEnd.toLocalDateTime());
					}

					order.setStatus("active");
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

	// ========== LOST PARKING CODE ==========

	/**
	 * Sends the parking code to the user (by userName) if they have an active
	 * parking session. The code is sent via email.
	 *
	 * @param userName the username of the user
	 * @return the parking code if found, otherwise a message indicating no active
	 *         session
	 */
	public String sendLostParkingCode(String userName) {
		String qry = """
				SELECT pi.ParkingInfo_ID, u.Email, u.Phone, u.Name
				FROM parkinginfo pi
				JOIN users u ON pi.User_ID = u.User_ID
				WHERE u.UserName = ? AND pi.statusEnum = 'active'
				""";

		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setString(1, userName);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					int parkingCode = rs.getInt("ParkingInfo_ID");
					String email = rs.getString("Email");
					String phone = rs.getString("Phone");
					String name = rs.getString("Name");

					// Send email notification
					EmailService.sendParkingCodeRecovery(email, name, String.valueOf(parkingCode));

					return String.valueOf(parkingCode);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error sending lost code: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return "No active parking session found";
	}

	/**
	 * Sends the parking code to the user (by userID) if they have an active parking
	 * session. The code is sent via email.
	 *
	 * @param userID the unique ID of the user
	 * @return a message with the parking code or an error message if no active
	 *         session is found
	 */
	public String sendLostParkingCode(int userID) {
		String qry = """
				SELECT pi.ParkingInfo_ID, u.Email, u.Phone, u.Name
				FROM parkinginfo pi
				JOIN users u ON pi.User_ID = u.User_ID
				WHERE u.User_ID = ? AND pi.statusEnum = 'active'
				""";

		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setInt(1, userID);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					int parkingCode = rs.getInt("ParkingInfo_ID");
					String email = rs.getString("Email");
					String phone = rs.getString("Phone");
					String name = rs.getString("Name");

					// Send recovery email
					EmailService.sendParkingCodeRecovery(email, name, String.valueOf(parkingCode));

					return "Your active parking code is: " + parkingCode;
				}
			}
		} catch (SQLException e) {
			System.out.println("Error sending lost code: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		return "No active parking session found for this user.";
	}

	// ========== USER REGISTRATION ==========

	/**
	 * Registers a new subscriber. Only allowed for attendants.
	 *
	 * @param attendantUserName the username of the attendant performing the
	 *                          registration
	 * @param name              the subscriber's name
	 * @param phone             the subscriber's phone number
	 * @param email             the subscriber's email address
	 * @param carNumber         the subscriber's car number
	 * @param userName          the desired username for the subscriber
	 * @return success message or error message
	 */
	public String registerNewSubscriber(String attendantUserName, String name, String phone, String email,
			String carNumber, String userName) {
		// Verify caller is attendant
		if (!hasRole(attendantUserName, UserRole.ATTENDANT)) {
			return "ERROR: Only parking attendants can register new subscribers";
		}

		return registerNewSubscriberInternal(name, phone, email, carNumber, userName);
	}

	/**
	 * Internal method to register a new subscriber after validation.
	 *
	 * @param name      subscriber's name
	 * @param phone     subscriber's phone number
	 * @param email     subscriber's email
	 * @param carNumber subscriber's car number
	 * @param userName  desired username
	 * @return result message
	 */
	private String registerNewSubscriberInternal(String name, String phone, String email, String carNumber,
			String userName) {
		// Validate input
		if (name == null || name.trim().isEmpty()) {
			return "Name is required";
		}
		if (phone == null || phone.trim().isEmpty()) {
			return "Phone number is required";
		}
		if (email == null || email.trim().isEmpty()) {
			return "Email is required";
		}
		if (userName == null || userName.trim().isEmpty()) {
			return "Username is required";
		}

		// Check if username exists
		if (doesUsernameExist(userName)) {
			return "Username already exists. Please choose a different username.";
		}

		// Insert new subscriber
		String insertQry = "INSERT INTO users (UserName, Name, Phone, Email, CarNum, UserTypeEnum) VALUES (?, ?, ?, ?, ?, 'sub')";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(insertQry, PreparedStatement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, userName);
			stmt.setString(2, name);
			stmt.setString(3, phone);
			stmt.setString(4, email);
			stmt.setString(5, carNumber);

			int rowsInserted = stmt.executeUpdate();
			if (rowsInserted > 0) {
				// Get generated User_ID
				int userID = -1;
				try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						userID = generatedKeys.getInt(1);
					}
				}

				System.out.println("New subscriber registered: " + userName + " with User_ID: " + userID);

				// Send email notifications
				EmailService.sendRegistrationConfirmation(email, name, userName, userID);
				EmailService.sendWelcomeMessage(email, name, userName, userID);

				return "SUCCESS:Subscriber registered successfully. Username: " + userName + ", User ID: " + userID;
			}
		} catch (SQLException e) {
			System.out.println("Registration failed: " + e.getMessage());
			return "Registration failed: " + e.getMessage();
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		return "Registration failed: Unknown error";
	}

	/**
	 * Generates a unique username based on the base name provided.
	 *
	 * @param baseName the base name to generate a username from
	 * @return a unique username
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
	 * Updates subscriber information (phone, email, car number). Input format:
	 * "userName,phone,email,carNumber".
	 *
	 * @param updateData CSV string with updated data
	 * @return success or failure message
	 */
	public String updateSubscriberInfo(String updateData) {
		// Format: userName,phone,email,carNumber
		String[] data = updateData.split(",", -1);
		if (data.length != 4) {
			return "Invalid update data format";
		}

		String userName = data[0];
		String phone = data[1];
		String email = data[2];
		String carNumber = data[3];

		StringBuilder queryBuilder = new StringBuilder("UPDATE users SET ");
		List<String> fields = new ArrayList<>();
		List<String> values = new ArrayList<>();

		if (!phone.isEmpty()) {
			fields.add("Phone = ?");
			values.add(phone);
		}
		if (!email.isEmpty()) {
			fields.add("Email = ?");
			values.add(email);
		}
		if (!carNumber.isEmpty()) {
			fields.add("CarNum = ?");
			values.add(carNumber);
		}

		if (fields.isEmpty()) {
			return "No changes to update.";
		}

		queryBuilder.append(String.join(", ", fields));
		queryBuilder.append(" WHERE UserName = ?");

		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {
			for (int i = 0; i < values.size(); i++) {
				stmt.setString(i + 1, values.get(i));
			}
			stmt.setString(values.size() + 1, userName);

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

	// ========== HELPER METHODS ==========

	/**
	 * Searches the database for an available parking spot that is not reserved or
	 * active during the specified time slot. Returns the first available spot ID or
	 * -1 if none found.
	 *
	 * @param startTime the desired start time of the reservation
	 * @param endTime   the desired end time of the reservation
	 * @return the ID of the available parking spot, or -1 if none found
	 */
	private int findAvailableSpotForTimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
		String qry = """
				SELECT ps.ParkingSpot_ID
				FROM parkingspot ps
				WHERE ps.ParkingSpot_ID NOT IN (
				    SELECT DISTINCT pi.ParkingSpot_ID
				    FROM parkinginfo pi
				    WHERE pi.statusEnum IN ('preorder', 'active')
				    AND pi.ParkingSpot_ID IS NOT NULL
				    AND (
				        -- Check if times overlap
				        (pi.Estimated_start_time < ? AND pi.Estimated_end_time > ?)
				        OR
				        (pi.Estimated_start_time >= ? AND pi.Estimated_start_time < ?)
				    )
				)
				ORDER BY ps.ParkingSpot_ID
				LIMIT 1
				""";

		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setTimestamp(1, Timestamp.valueOf(endTime));
			stmt.setTimestamp(2, Timestamp.valueOf(startTime));
			stmt.setTimestamp(3, Timestamp.valueOf(startTime));
			stmt.setTimestamp(4, Timestamp.valueOf(endTime));

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					int spotId = rs.getInt("ParkingSpot_ID");
					System.out.println(
							"Found available spot " + spotId + " for time slot " + startTime + " to " + endTime);
					return spotId;
				}
			}
		} catch (SQLException e) {
			System.out.println("Error finding available spot for time slot: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		System.out.println("No available spots for time slot " + startTime + " to " + endTime);
		return -1;
	}

	/**
	 * Parses a datetime string in the format "yyyy-MM-dd HH:mm" to a LocalDateTime
	 * object.
	 *
	 * @param dateTimeStr the string to parse
	 * @return parsed LocalDateTime object, or null if parsing fails
	 */
	private LocalDateTime parseDateTime(String dateTimeStr) {
		try {
			// Try "YYYY-MM-DD HH:MM:SS" format first
			if (dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
				return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			}
			// Try "YYYY-MM-DD HH:MM" format
			else if (dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
				return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
			}
			// Try ISO format "YYYY-MM-DDTHH:MM"
			else if (dateTimeStr.contains("T")) {
				return LocalDateTime.parse(dateTimeStr);
			} else {
				throw new IllegalArgumentException("Unsupported datetime format: " + dateTimeStr);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Invalid datetime format: " + dateTimeStr + ". Use 'YYYY-MM-DD HH:MM' or 'YYYY-MM-DD HH:MM:SS'");
		}
	}

	/**
	 * Returns the user ID associated with a given username. If not found or in case
	 * of error, returns -1.
	 *
	 * @param userName the username to search
	 * @return user ID if found, -1 otherwise
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
	 * Returns the ID of the first available parking spot that is not currently
	 * reserved or occupied.
	 *
	 * @return available parking spot ID, or -1 if none found
	 */
	private int getAvailableParkingSpotID() {
		String qry = """
				SELECT ps.ParkingSpot_ID
				FROM ParkingSpot ps
				WHERE ps.isOccupied = false
				  AND ps.ParkingSpot_ID NOT IN (
				      SELECT pi.ParkingSpot_ID
				      FROM parkinginfo pi
				      WHERE pi.statusEnum IN ('preorder', 'active')
				        AND (
				            (pi.Estimated_start_time <= NOW() AND pi.Estimated_end_time >= NOW())
				            OR (pi.statusEnum = 'preorder' AND TIMESTAMPDIFF(MINUTE, pi.Estimated_start_time, NOW()) BETWEEN 0 AND 15)
				        )
				  )
				LIMIT 1
				""";

		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(qry); ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt("ParkingSpot_ID");
			}
		} catch (SQLException e) {
			System.out.println("Error getting available spot ID: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return -1;
	}

	/**
	 * Updates the 'isOccupied' status of a given parking spot.
	 *
	 * @param spotID     ID of the parking spot to update
	 * @param isOccupied new occupancy status
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
	 * Sends a late pickup notification to the user with the given ID, using email
	 * from the users table.
	 *
	 * @param userID the user's ID
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

					// Send email notification
					EmailService.sendLatePickupNotification(email, name);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error sending late notification: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
	}

	/**
	 * Checks whether a given username is available (i.e., not already used).
	 *
	 * @param userName the username to check
	 * @return true if available, false otherwise
	 */
	private boolean isUsernameAvailable(String userName) {
		return !doesUsernameExist(userName);
	}

	/**
	 * Checks if a username already exists in the database.
	 *
	 * @param username the username to check
	 * @return true if exists, false otherwise
	 */
	public boolean doesUsernameExist(String username) {
		String qry = "SELECT COUNT(*) FROM users WHERE UserName = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setString(1, username);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.out.println("Error checking username: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return false;
	}

	/**
	 * Checks if a user with the given ID exists in the database.
	 *
	 * @param userID the ID to check
	 * @return true if user exists, false otherwise
	 */
	public boolean doesUserIDExist(int userID) {
		String qry = "SELECT COUNT(*) FROM users WHERE User_ID = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setInt(1, userID);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			}
		} catch (SQLException e) {
			System.out.println("Error checking user ID: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return false;
	}
	
	/**
	 * Validate that a parking order belongs to the specified user
	 * @param parkingInfoID The parking order ID
	 * @param userID The user ID to validate ownership
	 * @return true if the parking order belongs to the user, false otherwise
	 */
	public boolean validateParkingOrderOwnership(int parkingInfoID, int userID) {
		String qry = "SELECT COUNT(*) FROM parkinginfo WHERE ParkingInfo_ID = ? AND User_ID = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setInt(1, parkingInfoID);
			stmt.setInt(2, userID);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					boolean isOwner = rs.getInt(1) > 0;
					if (!isOwner) {
						System.out.println("Security Alert: User " + userID + " attempted to access parking order " + parkingInfoID + " without authorization");
					}
					return isOwner;
				}
			}
		} catch (SQLException e) {
			System.out.println("Error validating parking order ownership: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return false;
	}


	/**
	 * Retrieves the user's full name by matching both username and user ID.
	 *
	 * @param username the username
	 * @param userID   the user's ID
	 * @return full name if found, null otherwise
	 */
	public String getNameByUsernameAndUserID(String username, int userID) {
		String qry = "SELECT Name FROM users WHERE UserName = ? AND User_ID = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setString(1, username);
			stmt.setInt(2, userID);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("Name");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting name: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return null;
	}

	/**
	 * Retrieves the full name of a user by their ID.
	 *
	 * @param userID the user's ID
	 * @return full name if found, null otherwise
	 */
	public String getNameByUserID(int userID) {
		String qry = "SELECT Name FROM users WHERE User_ID = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setInt(1, userID);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("Name");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting name by user ID: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return null;
	}

	/**
	 * Fetches a subscriber's details based on their name.
	 *
	 * @param name subscriber's name
	 * @return ParkingSubscriber object if found, null otherwise
	 */
	public ParkingSubscriber getSubscriberByName(String name) {
		ParkingSubscriber subscriber = null;
		String query = "SELECT * FROM users WHERE Name = ?";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				subscriber = new ParkingSubscriber(rs.getInt("User_ID"), rs.getString("UserName"), rs.getString("Name"),
						rs.getString("Phone"), rs.getString("Email"), rs.getString("CarNum"),
						rs.getString("UserTypeEnum"));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
		return subscriber;
	}

	/**
	 * Retrieves a list of all parking subscribers from the database.
	 *
	 * @return list of ParkingSubscriber objects
	 */
	public List<ParkingSubscriber> getAllSubscribers() {
		List<ParkingSubscriber> list = new ArrayList<>();
		String query = "SELECT * FROM users WHERE UserTypeEnum = 'sub'";
		Connection conn = DBController.getInstance().getConnection();

		try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				ParkingSubscriber subscriber = new ParkingSubscriber(rs.getInt("User_ID"), rs.getString("UserName"),
						rs.getString("Name"), rs.getString("Phone"), rs.getString("Email"), rs.getString("CarNum"),
						rs.getString("UserTypeEnum"));
				list.add(subscriber);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}

		return list;
	}

	/**
	 * Ends the parking session and marks the spot as free, based on a parking info
	 * ID (used by attendants).
	 *
	 * @param parkingInfoID ID of the parking session
	 * @return status message to show to the user
	 */
	public String retrieveCarByCode(int parkingInfoID) {
		System.out.println("[DEBUG] retrieveCarByCode called with ParkingInfo_ID: " + parkingInfoID);

		String selectQry = """
				SELECT ParkingSpot_ID
				FROM parkinginfo
				WHERE ParkingInfo_ID = ? AND statusEnum = 'active'
				""";

		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement selectStmt = conn.prepareStatement(selectQry)) {
			selectStmt.setInt(1, parkingInfoID);

			try (ResultSet rs = selectStmt.executeQuery()) {
				if (rs.next()) {
					int parkingSpotID = rs.getInt("ParkingSpot_ID");
					System.out.println("[DEBUG] Found active parking: Spot ID = " + parkingSpotID);

					// Update parkinginfo
					String updateParkingInfo = """
							UPDATE parkinginfo
							SET Actual_end_time = NOW(), statusEnum = 'finished'
							WHERE ParkingInfo_ID = ?
							""";

					try (PreparedStatement updateInfoStmt = conn.prepareStatement(updateParkingInfo)) {
						updateInfoStmt.setInt(1, parkingInfoID);
						int rowsUpdated = updateInfoStmt.executeUpdate();
						System.out.println("[DEBUG] Updated parkinginfo rows: " + rowsUpdated);
					}

					// Update parking spot
					updateParkingSpotStatus(parkingSpotID, false);

					return "Car retrieved successfully from spot " + parkingSpotID;
				} else {
					System.out.println("[DEBUG] No active parking found for ParkingInfo_ID: " + parkingInfoID);
					return "No active parking session found for this code.";
				}
			}

		} catch (SQLException e) {
			System.out.println("Error retrieving car: " + e.getMessage());
			e.printStackTrace();
			return "Error retrieving car.";
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
	}

	/**
	 * Initializes the parking spots in the database if none exist. Starts the
	 * auto-cancellation service if applicable.
	 */
	public void initializeParkingSpots() {
		Connection conn = DBController.getInstance().getConnection();

		try {
			// Check if spots already exist
			String checkQry = "SELECT COUNT(*) FROM ParkingSpot";
			try (PreparedStatement stmt = conn.prepareStatement(checkQry)) {
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next() && rs.getInt(1) == 0) {
						// Initialize parking spots
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

		// Start auto-cancellation service if needed
		if (autoCancellationService != null && !autoCancellationService.isRunning()) {
			startAutoCancellationService();
		}
	}

	/**
	 * Logs out the user with the given username (only console message).
	 *
	 * @param userName the username to log out
	 */
	public void logoutUser(String userName) {
		System.out.println("User logged out: " + userName);
	}

	/**
	 * Debug utility to print a visual timeline of spot availability. Helps verify
	 * if availability threshold is met for a time range.
	 *
	 * @param startTime start of time period
	 * @param endTime   end of time period
	 */
	public void debugAvailabilityTimeline(LocalDateTime startTime, LocalDateTime endTime) {
		System.out.println("\n=== Availability Timeline Debug ===");
		System.out.println("Checking period: " + startTime + " to " + endTime);
		System.out.println("Total spots: " + TOTAL_PARKING_SPOTS);
		System.out
				.println("40% threshold: > " + (int) (TOTAL_PARKING_SPOTS * RESERVATION_THRESHOLD) + " spots required");
		System.out.println("\nTimeline:");

		// Get all overlapping reservations
		String qry = """
				SELECT ParkingSpot_ID, Estimated_start_time, Estimated_end_time
				FROM parkinginfo
				WHERE statusEnum IN ('preorder', 'active')
				AND ParkingSpot_ID IS NOT NULL
				AND Estimated_start_time < ?
				AND Estimated_end_time > ?
				ORDER BY ParkingSpot_ID, Estimated_start_time
				""";

		Connection conn = DBController.getInstance().getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setTimestamp(1, Timestamp.valueOf(endTime));
			stmt.setTimestamp(2, Timestamp.valueOf(startTime));

			List<ReservationSlot> reservations = new ArrayList<>();
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					ReservationSlot slot = new ReservationSlot(rs.getInt("ParkingSpot_ID"),
							rs.getTimestamp("Estimated_start_time").toLocalDateTime(),
							rs.getTimestamp("Estimated_end_time").toLocalDateTime());
					reservations.add(slot);
					System.out.println(
							"  Spot " + slot.spotId + ": " + slot.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))
									+ " - " + slot.endTime.format(DateTimeFormatter.ofPattern("HH:mm")));
				}
			}

			System.out.println("\nAvailability by time:");
			LocalDateTime checkTime = startTime;
			int minAvailable = TOTAL_PARKING_SPOTS;

			while (checkTime.isBefore(endTime)) {
				Set<Integer> occupiedSpots = new HashSet<>();
				for (ReservationSlot res : reservations) {
					if (!checkTime.isBefore(res.startTime) && checkTime.isBefore(res.endTime)) {
						occupiedSpots.add(res.spotId);
					}
				}

				int available = TOTAL_PARKING_SPOTS - occupiedSpots.size();
				minAvailable = Math.min(minAvailable, available);

				String status = "";
				if (available <= (int) (TOTAL_PARKING_SPOTS * RESERVATION_THRESHOLD)) {
					status = " ⚠️ AT OR BELOW THRESHOLD";
				}

				System.out.println("  " + checkTime.format(DateTimeFormatter.ofPattern("HH:mm")) + ": " + available
						+ " spots available" + status);

				checkTime = checkTime.plusMinutes(30); // Show every 30 minutes for clarity
			}

			System.out.println("\nMinimum availability: " + minAvailable + " spots");
			boolean allowed = minAvailable > (int) (TOTAL_PARKING_SPOTS * RESERVATION_THRESHOLD);
			System.out.println("Reservation allowed: " + (allowed ? "YES ✓" : "NO ✗"));
			System.out.println("================================\n");

		} catch (SQLException e) {
			System.out.println("Error in debug: " + e.getMessage());
		} finally {
			DBController.getInstance().releaseConnection(conn);
		}
	}
}