package controllers;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import entities.ParkingOrder;
import entities.ParkingSubscriber;
import server.DBController;
import services.EmailService;

/**
 * Enhanced ParkingController with email notifications Updated to work with
 * unified parkinginfo table structure
 */

public class ParkingController {
	  private int subscriberID;
    private String firstName;
    private String phoneNumber;
    private String email;
    private String carNumber;
    private String subscriberCode;
    private String userType;
    protected Connection conn;
    public int successFlag;
    private static final int TOTAL_PARKING_SPOTS = 100;
    private static final double RESERVATION_THRESHOLD = 0.4;
    
    
    // Getters
    public int getSubscriberID() { return subscriberID; }
    public String getFirstName() { return firstName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getCarNumber() { return carNumber; }
    public String getSubscriberCode() { return subscriberCode; }
    public String getUserType() { return userType; }

    // Setters
    public void setSubscriberID(int subscriberID) { this.subscriberID = subscriberID; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEmail(String email) { this.email = email; }
    public void setCarNumber(String carNumber) { this.carNumber = carNumber; }
    public void setSubscriberCode(String subscriberCode) { this.subscriberCode = subscriberCode; }
    public void setUserType(String userType) { this.userType = userType; }

    
    /**
     * Role-based access control for all parking operations
     */
    public enum UserRole {
        SUBSCRIBER("sub"),
        ATTENDANT("emp"), 
        MANAGER("mng");
        
        private final String dbValue;
        
        UserRole(String dbValue) {
            this.dbValue = dbValue;
        }
        
        public String getDbValue() {
            return dbValue;
        }
        
        public static UserRole fromDbValue(String dbValue) {
            for (UserRole role : values()) {
                if (role.dbValue.equals(dbValue)) {
                    return role;
                }
            }
            return null;
        }
    }

    /**
     * Get user role from database
     */
    private UserRole getUserRole(String userName) {
        String qry = "SELECT UserTypeEnum FROM users WHERE UserName = ?";
        
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
        }
        return null;
    }

    /**
     * Check if user has required role for operation
     */
    private boolean hasRole(String userName, UserRole requiredRole) {
        UserRole userRole = getUserRole(userName);
        return userRole == requiredRole;
    }

    /**
     * Check if user has any of the required roles
     */
    private boolean hasAnyRole(String userName, UserRole... requiredRoles) {
        UserRole userRole = getUserRole(userName);
        if (userRole == null) return false;
        
        for (UserRole role : requiredRoles) {
            if (userRole == role) return true;
        }
        return false;
    }
    
    // Auto-cancellation service
    private SimpleAutoCancellationService autoCancellationService;



    public Connection getConnection() {
        return conn;
    }

//    public void connectToDB(String path, String pass) {
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            System.out.println("Driver definition succeed");
//        } catch (Exception ex) {
//            System.out.println("Driver definition failed");
//        }
//
//        try {
//            conn = DriverManager.getConnection(path, "root", pass);
//            System.out.println("SQL connection succeed");
//            successFlag = 1;
//        } catch (SQLException ex) {
//            System.out.println("SQLException: " + ex.getMessage());
//            System.out.println("SQLState: " + ex.getSQLState());
//            System.out.println("VendorError: " + ex.getErrorCode());
//            successFlag = 2;
//        }
//    }

    /**
     * Start the automatic monitoring service (cancellations + late pickups)
     */
    public void startAutoCancellationService() {
        if (autoCancellationService != null) {
            autoCancellationService.startService();
            System.out.println("✅ Auto-monitoring service started:");
            System.out.println("   - Monitoring preorder reservations (auto-cancel after 15 min)");
            System.out.println("   - Monitoring active parkings (notify late pickups after 15 min)");
        }
    }

    /**
     * Stop the automatic monitoring service
     */
    public void stopAutoCancellationService() {
        if (autoCancellationService != null) {
            autoCancellationService.stopService();
            System.out.println("⛔ Auto-monitoring service stopped");
        }
    }

    /**
     * Cleanup method - call when shutting down the controller
     */
    public void shutdown() {
        if (autoCancellationService != null) {
            autoCancellationService.shutdown();
        }
    }

    // ========== ALL YOUR EXISTING METHODS UPDATED ==========
    
    public String checkLogin(String userName, String userCode) {
        String qry = "SELECT UserTypeEnum FROM users WHERE UserName = ? AND User_ID = ?";
        
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
        }
        return "None";
    }

    /**
     * Gets user information by userName
     */
    public ParkingSubscriber getUserInfo(String userName) {
        String qry = "SELECT * FROM users WHERE UserName = ?";
        
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
        }
        return null;
    }


    /**
     * Checks if reservation is possible (40% of spots must be available)
     */
    public boolean canMakeReservation() {
        int availableSpots = getAvailableParkingSpots();
        return availableSpots >= (TOTAL_PARKING_SPOTS * RESERVATION_THRESHOLD);
    }

    /**
 * Makes a parking reservation with specific DATE and TIME
 * FIXED: Now properly checks for time conflicts to prevent double-booking
 */
public String makeReservation(String userName, String reservationDateTimeStr) {
    // Check if reservation is possible (40% rule)
    if (!canMakeReservation()) {
        return "Not enough available spots for reservation (need 40% available)";
    }

    try {
        // Parse the datetime string
        LocalDateTime reservationDateTime = parseDateTime(reservationDateTimeStr);
        
        // Validate reservation is within allowed time range (24 hours to 7 days)
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

        // NEW VALIDATION: Check if user already has an ACTIVE reservation for the same date
        LocalDate reservationDate = reservationDateTime.toLocalDate();
        if (!canMakeReservationForDate(userID, reservationDate)) {
            return "You already have an active reservation for " + reservationDate + 
                   ". Only one active reservation per day is allowed. " +
                   "Previous reservations that were finished or cancelled do not count.";
        }

        // Calculate end time (default 4 hours)
        LocalDateTime estimatedEndTime = reservationDateTime.plusHours(4);

        // CRITICAL FIX: Use findAvailableSpotForTimeSlot instead of getAvailableParkingSpotID
        int parkingSpotID = findAvailableSpotForTimeSlot(reservationDateTime, estimatedEndTime);
        if (parkingSpotID == -1) {
            return "No parking spots available for the requested time slot";
        }

        // Create reservation in parkinginfo table with statusEnum='preorder'
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
            
            // Get the generated ParkingInfo_ID (this is our reservation code)
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int reservationCode = generatedKeys.getInt(1);
                    System.out.println("New preorder reservation created: " + reservationCode + 
                                     " for " + reservationDateTime + " (15-min auto-cancel rule applies)");
                    
                    // Send email confirmation
                    ParkingSubscriber user = getUserInfo(userName);
                    if (user != null && user.getEmail() != null) {
                        String formattedDateTime = reservationDateTime.format(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        EmailService.sendReservationConfirmation(
                            user.getEmail(), user.getFirstName(), 
                            String.valueOf(reservationCode), formattedDateTime, "Spot " + parkingSpotID
                        );
                    }
                    
                    return "Reservation confirmed for " + reservationDateTime.format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + 
                        ". Confirmation code: " + reservationCode + ". Spot: " + parkingSpotID;
                }
            }
        }
    } catch (Exception e) {
        System.out.println("Error making reservation: " + e.getMessage());
        return "Reservation failed: " + e.getMessage();
    }
    return "Reservation failed";
}


	public ParkingController(String dbname, String pass) {
		DBController.initializeConnection(dbname, pass);
		successFlag = DBController.getInstance().getSuccessFlag();
		conn = DBController.getInstance().getConnection();
		
		 this.autoCancellationService = new SimpleAutoCancellationService(this);
	     this.autoCancellationService.startService();

	}






//	public void connectToDB(String path, String pass) {
//		try {
//			Class.forName("com.mysql.cj.jdbc.Driver");
//			System.out.println("Driver definition succeed");
//		} catch (Exception ex) {
//			System.out.println("Driver definition failed");
//		}
//
//		try {
//			conn = DriverManager.getConnection(path, "root", pass);
//			System.out.println("SQL connection succeed");
//			successFlag = 1;
//		} catch (SQLException ex) {
//			System.out.println("SQLException: " + ex.getMessage());
//			System.out.println("SQLState: " + ex.getSQLState());
//			System.out.println("VendorError: " + ex.getErrorCode());
//			successFlag = 2;
//		}
//	}




	/**
	 * Gets the number of available parking spots
	 */
	public int getAvailableParkingSpots() {
		String qry = "SELECT COUNT(*) as available FROM ParkingSpot WHERE isOccupied = false";

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("available");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting available spots: " + e.getMessage());
		}
		return 0;
	}





	/**
	 * ADD THIS NEW METHOD - Find an available spot for a specific time slot This
	 * prevents double-booking by checking for time conflicts
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

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			// Set parameters for overlap check
			stmt.setTimestamp(1, Timestamp.valueOf(endTime)); // existing end > new start
			stmt.setTimestamp(2, Timestamp.valueOf(startTime)); // existing start < new end
			stmt.setTimestamp(3, Timestamp.valueOf(startTime)); // existing start >= new start
			stmt.setTimestamp(4, Timestamp.valueOf(endTime)); // existing start < new end

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
		}

		System.out.println("No available spots for time slot " + startTime + " to " + endTime);
		return -1;
	}

// Also update the getAvailableParkingSpots method to consider time slots:

	/**
	 * Gets the number of available parking spots for immediate use (NOW) For
	 * reservations, use getAvailableSpotsForTimeSlot() instead
	 */
	// ******************************************************************************************************************************
//	public int getAvailableParkingSpots() {
//		LocalDateTime now = LocalDateTime.now();
//		LocalDateTime fourHoursLater = now.plusHours(4);
//		return getAvailableSpotsForTimeSlot(now, fourHoursLater);
//	}

	// ******************************************************************************************************************************

	/**
	 * ADD THIS METHOD - Get count of available spots for a specific time slot
	 */
	public int getAvailableSpotsForTimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
		String qry = """
				SELECT COUNT(*) as available
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
				""";

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setTimestamp(1, Timestamp.valueOf(endTime));
			stmt.setTimestamp(2, Timestamp.valueOf(startTime));
			stmt.setTimestamp(3, Timestamp.valueOf(startTime));
			stmt.setTimestamp(4, Timestamp.valueOf(endTime));

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("available");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting available spots for time slot: " + e.getMessage());
		}
		return 0;
	}

	/**
	 * Parse datetime string in various formats
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
	 * Handles parking entry with subscriber code (immediate parking)
	 */
	public String enterParking(String userName) {
    // Get user ID
    int userID = getUserID(userName);
    if (userID == -1) {
        return "Invalid user code";
    }
    
    // VALIDATION 1: Check if user already has an active parking session
    if (hasActiveParkingSession(userID)) {
        return "You already have an active parking session. " +
               "Please exit your current parking before starting a new one.";
    }

    // VALIDATION 2: Check if user has preorder reservation for today
    if (hasPreorderReservationForToday(userID)) {
        return "You have a preorder reservation for today. " +
               "Please use your reservation code to activate it, or cancel the reservation first.";
    }
    
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime estimatedEnd = now.plusHours(4); // Default 4 hours
    
    // Check if ANY spots are available for the next 4 hours (no 40% rule)
    int availableSpots = getAvailableSpotsForTimeSlot(now, estimatedEnd);
    if (availableSpots <= 0) {
        return "No parking spots available for immediate parking (all spots occupied or reserved)";
    }
    
    // Find a spot that's free NOW and has no reservations for the next 4 hours
    int spotID = findAvailableSpotForTimeSlot(now, estimatedEnd);
    if (spotID == -1) {
        return "No available parking spot found (all spots have upcoming reservations)";
    }
    
    // Create parking info record for immediate parking
    String qry = """
        INSERT INTO parkinginfo
        (ParkingSpot_ID, User_ID, Date_Of_Placing_Order, Actual_start_time,
         Estimated_start_time, Estimated_end_time, IsOrderedEnum, IsLate, IsExtended, statusEnum)
        VALUES (?, ?, NOW(), ?, ?, ?, 'no', 'no', 'no', 'active')
        """;
    
    try (PreparedStatement stmt = conn.prepareStatement(qry, PreparedStatement.RETURN_GENERATED_KEYS)) {
        stmt.setInt(1, spotID);
        stmt.setInt(2, userID);
        stmt.setTimestamp(3, Timestamp.valueOf(now));
        stmt.setTimestamp(4, Timestamp.valueOf(now));
        stmt.setTimestamp(5, Timestamp.valueOf(estimatedEnd));
        stmt.executeUpdate();
        
        // Get the generated ParkingInfo_ID (parking code)
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                int parkingCode = generatedKeys.getInt(1);
                
                // Mark parking spot as occupied
                updateParkingSpotStatus(spotID, true);
                
                return "Entry successful. Parking code: " + parkingCode + ". Spot: " + spotID;
            }
        }
    } catch (SQLException e) {
        System.out.println("Error handling entry: " + e.getMessage());
        return "Entry failed";
    }
    return "Entry failed";
}

	/**
	 * Handles parking entry with reservation code - NOW SUPPORTS PREORDER->ACTIVE
	 */
	public String enterParkingWithReservation(int reservationCode) {
		// Check if reservation exists and is in preorder status
		String checkQry = """
				SELECT pi.*, u.User_ID
				FROM parkinginfo pi
				JOIN users u ON pi.User_ID = u.User_ID
				WHERE pi.ParkingInfo_ID = ? AND pi.statusEnum = 'preorder'
				""";

		try (PreparedStatement stmt = conn.prepareStatement(checkQry)) {
			stmt.setInt(1, reservationCode);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					LocalDateTime estimatedStartTime = rs.getTimestamp("Estimated_start_time").toLocalDateTime();
					int userID = rs.getInt("User_ID");
					int parkingSpotID = rs.getInt("ParkingSpot_ID");

					// Check if reservation is for today
					LocalDateTime now = LocalDateTime.now();
					if (!estimatedStartTime.toLocalDate().equals(now.toLocalDate())) {
						if (estimatedStartTime.isBefore(now)) {
							// Cancel expired reservation
							cancelReservation(reservationCode);
							return "Reservation expired";
						} else {
							return "Reservation is for future date";
						}
					}

					// Update reservation to active status and set actual start time
					String updateQry = """
							UPDATE parkinginfo
							SET statusEnum = 'active', Actual_start_time = ?
							WHERE ParkingInfo_ID = ?
							""";

					try (PreparedStatement updateStmt = conn.prepareStatement(updateQry)) {
						updateStmt.setTimestamp(1, Timestamp.valueOf(now));
						updateStmt.setInt(2, reservationCode);
						updateStmt.executeUpdate();

						// Mark parking spot as occupied
						updateParkingSpotStatus(parkingSpotID, true);

						System.out.println("Reservation " + reservationCode + " activated (preorder → active)");
						return "Entry successful! Reservation activated. Parking code: " + reservationCode + ". Spot: "
								+ parkingSpotID;
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Error handling reservation entry: " + e.getMessage());
		}
		return "Invalid reservation code or reservation not in preorder status";
	}

	/**
	 * ATTENDANT-ONLY: Register new subscriber (PDF requirement) Only attendants can
	 * register new users
	 */
	public String registerNewSubscriber(String attendantUserName, String name, String phone, String email,
			String carNumber, String userName) {
		// Verify caller is attendant
		if (!hasRole(attendantUserName, UserRole.ATTENDANT)) {
			return "ERROR: Only parking attendants can register new subscribers";
		}

		// Continue with existing registration logic
		return registerNewSubscriberInternal(name, phone, email, carNumber, userName);
	}

	/**
	 * For backwards compatibility - allow registration without attendant check This
	 * can be used by system initialization or admin functions
	 */
	public String registerNewSubscriber(String name, String phone, String email, String carNumber, String userName) {
		return registerNewSubscriberInternal(name, phone, email, carNumber, userName);
	}

	/**
	 * Registers a new subscriber in the system - WITH EMAIL NOTIFICATIONS
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

		// Check if username already exists
		String checkQry = "SELECT COUNT(*) FROM users WHERE UserName = ?";

		try (PreparedStatement checkStmt = conn.prepareStatement(checkQry)) {
			checkStmt.setString(1, userName);
			try (ResultSet rs = checkStmt.executeQuery()) {
				if (rs.next() && rs.getInt(1) > 0) {
					return "Username already exists. Please choose a different username.";
				}
			}
		} catch (SQLException e) {
			System.out.println("Error checking username: " + e.getMessage());
			return "Error checking username availability";
		}

		// Insert new subscriber
		String insertQry = "INSERT INTO users (UserName, Name, Phone, Email, CarNum, UserTypeEnum) VALUES (?, ?, ?, ?, ?, 'sub')";

		try (PreparedStatement stmt = conn.prepareStatement(insertQry, PreparedStatement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, userName);
			stmt.setString(2, name);
			stmt.setString(3, phone);
			stmt.setString(4, email);
			stmt.setString(5, carNumber);

			int rowsInserted = stmt.executeUpdate();
			if (rowsInserted > 0) {
				// Get the generated User_ID
				int userID = -1;
				try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						userID = generatedKeys.getInt(1);
					}
				}

				System.out.println("New subscriber registered: " + userName + " with User_ID: " + userID);

				// SEND EMAIL NOTIFICATIONS with User_ID
				EmailService.sendRegistrationConfirmation(email, name, userName, userID);
				EmailService.sendWelcomeMessage(email, name, userName, userID);

				return "SUCCESS:Subscriber registered successfully. Username: " + userName + ", User ID: " + userID;
			}
		} catch (SQLException e) {
			System.out.println("Registration failed: " + e.getMessage());
			return "Registration failed: " + e.getMessage();
		}

		return "Registration failed: Unknown error";
	}

	/**
	 * Generates a unique subscriber code/username
	 */
	public String generateUniqueUsername(String baseName) {
		// Remove spaces and special characters
		String cleanName = baseName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

		// Try the clean name first
		if (isUsernameAvailable(cleanName)) {
			return cleanName;
		}

		// If taken, try with numbers
		for (int i = 1; i <= 999; i++) {
			String candidate = cleanName + i;
			if (isUsernameAvailable(candidate)) {
				return candidate;
			}
		}

		// Fallback to random number
		return cleanName + System.currentTimeMillis() % 10000;
	}

	/**
	 * Handles parking exit - NOW SUPPORTS FINISHING RESERVATIONS
	 */
	public String exitParking(String parkingCodeStr) {
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
						String orderType = rs.getString("IsOrderedEnum");

						LocalDateTime now = LocalDateTime.now();
						LocalDateTime estimatedEnd = estimatedEndTime.toLocalDateTime();

						// Check if parking exceeded estimated time
						boolean isLate = now.isAfter(estimatedEnd);

						// Update parking info with exit time and finish status
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
		}
		return "Invalid parking code or already exited";
	}

//	/**
//	 * Extends parking time
//	 */
//	public String extendParkingTime(String parkingCodeStr, int additionalHours) {
//		if (additionalHours < 1 || additionalHours > 4) {
//			return "Can only extend parking by 1-4 hours";
//		}
//
//		try {
//			int parkingCode = Integer.parseInt(parkingCodeStr);
//
//			// Get user info for email notification
//			String getUserQry = """
//					SELECT pi.*, u.Email, u.Name
//					FROM parkinginfo pi
//					JOIN users u ON pi.User_ID = u.User_ID
//					WHERE pi.ParkingInfo_ID = ? AND pi.statusEnum = 'active'
//					""";
//
//			try (PreparedStatement stmt = conn.prepareStatement(getUserQry)) {
//				stmt.setInt(1, parkingCode);
//				try (ResultSet rs = stmt.executeQuery()) {
//					if (rs.next()) {
//						Timestamp currentEstimatedEnd = rs.getTimestamp("Estimated_end_time");
//						String userEmail = rs.getString("Email");
//						String userName = rs.getString("Name");
//
//						LocalDateTime newEstimatedEnd = currentEstimatedEnd.toLocalDateTime()
//								.plusHours(additionalHours);
//
//						String updateQry = """
//								UPDATE parkinginfo
//								SET Estimated_end_time = ?, IsExtended = 'yes'
//								WHERE ParkingInfo_ID = ?
//								""";
//
//						try (PreparedStatement updateStmt = conn.prepareStatement(updateQry)) {
//							updateStmt.setTimestamp(1, Timestamp.valueOf(newEstimatedEnd));
//							updateStmt.setInt(2, parkingCode);
//							updateStmt.executeUpdate();
//
//							// SEND EMAIL NOTIFICATION
//							if (userEmail != null && userName != null) {
//								EmailService.sendExtensionConfirmation(userEmail, userName, parkingCodeStr,
//										additionalHours, newEstimatedEnd.toString());
//							}
//
//							return "Parking time extended by " + additionalHours + " hours until " + newEstimatedEnd;
//						}
//					}
//				}
//			}
//		} catch (NumberFormatException e) {
//			return "Invalid parking code format";
//		} catch (SQLException e) {
//			System.out.println("Error extending parking time: " + e.getMessage());
//		}
//		return "Invalid parking code or parking session not active";
//	}

	/**
	 * Sends lost parking code to user
	 */
	public String sendLostParkingCode(String userName) {
		String qry = """
				SELECT pi.ParkingInfo_ID, u.Email, u.Phone, u.Name
				FROM parkinginfo pi
				JOIN users u ON pi.User_ID = u.User_ID
				WHERE u.UserName = ? AND pi.statusEnum = 'active'
				""";

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setString(1, userName);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					int parkingCode = rs.getInt("ParkingInfo_ID");
					String email = rs.getString("Email");
					String phone = rs.getString("Phone");
					String name = rs.getString("Name");

					// SEND EMAIL NOTIFICATION
					EmailService.sendParkingCodeRecovery(email, name, String.valueOf(parkingCode));

					return String.valueOf(parkingCode);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error sending lost code: " + e.getMessage());
		}
		return "No active parking session found";
	}

	/**
	 * Gets parking history for a user
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

					if (actualStart != null) {
						order.setEntryTime(actualStart.toLocalDateTime());
					}
					if (actualEnd != null) {
						order.setExitTime(actualEnd.toLocalDateTime());
					}
					if (estimatedEnd != null) {
						order.setExpectedExitTime(estimatedEnd.toLocalDateTime());
					}

					order.setLate("yes".equals(rs.getString("IsLate")));
					order.setExtended("yes".equals(rs.getString("IsExtended")));
					order.setStatus(rs.getString("statusEnum"));

					history.add(order);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting parking history: " + e.getMessage());
		}
		return history;
	}

	/**
	 * Gets all active parking sessions (for attendant view)
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

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					ParkingOrder order = new ParkingOrder();
					order.setOrderID(rs.getInt("ParkingInfo_ID"));
					order.setParkingCode(String.valueOf(rs.getInt("ParkingInfo_ID")));
					order.setOrderType(rs.getString("IsOrderedEnum"));
					order.setSubscriberName(rs.getString("Name"));
					order.setSpotNumber("Spot " + rs.getInt("ParkingSpot_ID"));

					// Convert Timestamps to LocalDateTime
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
		}
		return activeParkings;
	}



	/**
	 * Cancels a reservation
	 */
	public String cancelReservation(int reservationCode) {
		// Get user info before cancelling for email notification
		String getUserQry = """
				SELECT u.Email, u.Name
				FROM parkinginfo pi
				JOIN users u ON pi.User_ID = u.User_ID
				WHERE pi.ParkingInfo_ID = ?
				""";
		String userEmail = null;
		String userName = null;

		try (PreparedStatement stmt = conn.prepareStatement(getUserQry)) {
			stmt.setInt(1, reservationCode);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					userEmail = rs.getString("Email");
					userName = rs.getString("Name");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting user info for cancellation: " + e.getMessage());
		}

		String qry = """
				UPDATE parkinginfo
				SET statusEnum = 'cancelled'
				WHERE ParkingInfo_ID = ? AND statusEnum IN ('preorder', 'active')
				""";

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setInt(1, reservationCode);
			int rowsUpdated = stmt.executeUpdate();

			if (rowsUpdated > 0) {
				// Also free up the spot if it was assigned
				freeSpotForReservation(reservationCode);

				// SEND EMAIL NOTIFICATION
				if (userEmail != null && userName != null) {
					EmailService.sendReservationCancelled(userEmail, userName, String.valueOf(reservationCode));
				}

				return "Reservation cancelled successfully";
			}
		} catch (SQLException e) {
			System.out.println("Error cancelling reservation: " + e.getMessage());
		}
		return "Reservation not found or already cancelled/finished";
	}
	
	/**
	 * Check if user has ACTIVE reservation for the same date (preorder or active status)
	 * This excludes finished and cancelled reservations, allowing new reservations
	 * if previous ones are completed or cancelled
	 */
	private boolean hasActiveReservationForDate(int userID, LocalDate reservationDate) {
	    String qry = """
	        SELECT COUNT(*) as active_reservation_count 
	        FROM parkinginfo 
	        WHERE User_ID = ? 
	        AND DATE(Estimated_start_time) = ? 
	        AND statusEnum IN ('preorder', 'active')
	        """;
	    
	    try (PreparedStatement stmt = conn.prepareStatement(qry)) {
	        stmt.setInt(1, userID);
	        stmt.setDate(2, java.sql.Date.valueOf(reservationDate));
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt("active_reservation_count") > 0;
	            }
	        }
	    } catch (SQLException e) {
	        System.out.println("Error checking active reservation for date: " + e.getMessage());
	    }
	    return false;
	}

	/**
	 * Check if user can make reservation for specific date
	 * Only blocks if there's an active (preorder/active) reservation
	 * Allows new reservations if previous ones are finished/cancelled
	 */
	private boolean canMakeReservationForDate(int userID, LocalDate reservationDate) {
	    return !hasActiveReservationForDate(userID, reservationDate);
	}

	/**
	 * Check if user has any active parking sessions
	 */
	private boolean hasActiveParkingSession(int userID) {
	    String qry = """
	        SELECT COUNT(*) as active_count 
	        FROM parkinginfo 
	        WHERE User_ID = ? 
	        AND statusEnum = 'active'
	        AND Actual_end_time IS NULL
	        """;
	    
	    try (PreparedStatement stmt = conn.prepareStatement(qry)) {
	        stmt.setInt(1, userID);
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt("active_count") > 0;
	            }
	        }
	    } catch (SQLException e) {
	        System.out.println("Error checking active parking: " + e.getMessage());
	    }
	    return false;
	}

	/**
	 * Check if user has a preorder reservation for today's date
	 * This prevents immediate parking when user has a reservation for the same day
	 */
	private boolean hasPreorderReservationForToday(int userID) {
	    LocalDate today = LocalDate.now();
	    String qry = """
	        SELECT COUNT(*) as preorder_count 
	        FROM parkinginfo 
	        WHERE User_ID = ? 
	        AND DATE(Estimated_start_time) = ? 
	        AND statusEnum = 'preorder'
	        """;
	    
	    try (PreparedStatement stmt = conn.prepareStatement(qry)) {
	        stmt.setInt(1, userID);
	        stmt.setDate(2, java.sql.Date.valueOf(today));
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt("preorder_count") > 0;
	            }
	        }
	    } catch (SQLException e) {
	        System.out.println("Error checking preorder reservation for today: " + e.getMessage());
	    }
	    return false;
	}

	/**
	 * Check if user can make a new parking entry (no active sessions)
	 */
	private boolean canMakeNewParking(int userID) {
	    return !hasActiveParkingSession(userID);
	}

	/**
	 * Logs out a user (for future use if needed)
	 */
	public void logoutUser(String userName) {
		System.out.println("User logged out: " + userName);
	}

	/**
	 * Initializes parking spots if they don't exist
	 */
	public void initializeParkingSpots() {
		try {
			// Check if spots already exist
			String checkQry = "SELECT COUNT(*) FROM ParkingSpot";
			try (PreparedStatement stmt = conn.prepareStatement(checkQry)) {
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next() && rs.getInt(1) == 0) {
						// Initialize parking spots - AUTO_INCREMENT will handle ParkingSpot_ID
						String insertQry = "INSERT INTO ParkingSpot (isOccupied) VALUES (false)";
						try (PreparedStatement insertStmt = conn.prepareStatement(insertQry)) {
							for (int i = 1; i <= TOTAL_PARKING_SPOTS; i++) {
								insertStmt.executeUpdate();
							}
						}
						System.out.println("Successfully initialized " + TOTAL_PARKING_SPOTS +
								" parking spots with AUTO_INCREMENT");
					} else {
						System.out.println("Parking spots already exist: " + rs.getInt(1) + " spots found");
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Error initializing parking spots: " + e.getMessage());
		}
	}

	// ========== HELPER METHODS ==========

	private int getUserID(String userName) {
		String qry = "SELECT User_ID FROM users WHERE UserName = ?";

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setString(1, userName);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("User_ID");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error getting user ID: " + e.getMessage());
		}
		return -1;
	}

	

	private boolean isParkingSpotAvailable(int spotID) {
		String qry = "SELECT isOccupied FROM ParkingSpot WHERE ParkingSpot_ID = ?";

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setInt(1, spotID);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return !rs.getBoolean("isOccupied");
				}
			}
		} catch (SQLException e) {
			System.out.println("Error checking spot availability: " + e.getMessage());
		}
		return false;
	}

	private void updateParkingSpotStatus(int spotID, boolean isOccupied) {
		String qry = "UPDATE ParkingSpot SET isOccupied = ? WHERE ParkingSpot_ID = ?";

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setBoolean(1, isOccupied);
			stmt.setInt(2, spotID);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Error updating parking spot status: " + e.getMessage());
		}
	}

	/**
	 * Send late exit notification
	 */
	private void sendLateExitNotification(int userID) {
		String qry = "SELECT Email, Phone, Name FROM users WHERE User_ID = ?";

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setInt(1, userID);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					String email = rs.getString("Email");
					String phone = rs.getString("Phone");
					String name = rs.getString("Name");

					// SEND EMAIL NOTIFICATION
					EmailService.sendLatePickupNotification(email, name);
				}
			}
		} catch (SQLException e) {
			System.out.println("Error sending late notification: " + e.getMessage());
		}
	}

	private boolean isUsernameAvailable(String userName) {
		String checkQry = "SELECT COUNT(*) FROM users WHERE UserName = ?";

		try (PreparedStatement stmt = conn.prepareStatement(checkQry)) {
			stmt.setString(1, userName);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) == 0;
				}
			}
		} catch (SQLException e) {
			System.out.println("Error checking username availability: " + e.getMessage());
		}

		return false;
	}

	private void freeSpotForReservation(int reservationCode) {
		String query = """
				UPDATE parkingspot ps
				JOIN parkinginfo pi ON ps.ParkingSpot_ID = pi.ParkingSpot_ID
				SET ps.isOccupied = FALSE
				WHERE pi.ParkingInfo_ID = ?
				""";

		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, reservationCode);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Error freeing spot for reservation: " + e.getMessage());
		}
	}

	/**
	 * Activate reservation when customer arrives (PREORDER → ACTIVE)
	 */
	public String activateReservation(String subscriberUserName, int reservationCode) {
		// Check if reservation exists and is in preorder status
		String checkQry = """
				SELECT pi.*, u.UserName,
				       TIMESTAMPDIFF(MINUTE, pi.Estimated_start_time, NOW()) as minutes_since_start
				FROM parkinginfo pi
				JOIN users u ON pi.User_ID = u.User_ID
				WHERE pi.ParkingInfo_ID = ? AND pi.statusEnum = 'preorder'
				""";

		try (PreparedStatement stmt = conn.prepareStatement(checkQry)) {
			stmt.setInt(1, reservationCode);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					int minutesSinceStart = rs.getInt("minutes_since_start");
					int spotId = rs.getInt("ParkingSpot_ID");

					// Check if within 15-minute grace period
					if (minutesSinceStart > 15) {
						// Too late - auto-cancel
						cancelReservation(subscriberUserName, reservationCode);
						return "Reservation cancelled due to late arrival (over 15 minutes). Please make a new reservation.";
					}

					// Update reservation status to ACTIVE and set actual start time
					LocalDateTime now = LocalDateTime.now();
					String updateQry = """
							UPDATE parkinginfo
							SET statusEnum = 'active',
							    Actual_start_time = ?,
							    IsLate = ?
							WHERE ParkingInfo_ID = ?
							""";

					try (PreparedStatement updateStmt = conn.prepareStatement(updateQry)) {
						updateStmt.setTimestamp(1, Timestamp.valueOf(now));
						updateStmt.setString(2, minutesSinceStart > 0 ? "yes" : "no");
						updateStmt.setInt(3, reservationCode);
						updateStmt.executeUpdate();

						// Mark parking spot as occupied
						updateParkingSpotStatus(spotId, true);

						String lateMessage = minutesSinceStart > 0 ? " (Note: " + minutesSinceStart + " minutes late)"
								: "";

						System.out.println(
								"Reservation " + reservationCode + " activated (preorder → active)" + lateMessage);

						return "Reservation activated! Parking code: " + reservationCode + ". Spot: " + spotId
								+ lateMessage;
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Error activating reservation: " + e.getMessage());
			return "Failed to activate reservation";
		}

		return "Reservation not found or already activated";
	}

	/**
	 * Cancel reservation
	 */
	public String cancelReservation(String subscriberUserName, int reservationCode) {
		return cancelReservationInternal(reservationCode, "User requested cancellation");
	}

	/**
	 * Internal cancellation method (used by auto-cancel and manual cancel)
	 */
	private String cancelReservationInternal(int reservationCode, String reason) {
		// Get reservation info first for email notification
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
			System.out.println("Error getting reservation info for cancellation: " + e.getMessage());
		}

		// Update reservation status to cancelled
		String qry = """
				UPDATE parkinginfo
				SET statusEnum = 'cancelled'
				WHERE ParkingInfo_ID = ? AND statusEnum IN ('preorder', 'active')
				""";

		try (PreparedStatement stmt = conn.prepareStatement(qry)) {
			stmt.setInt(1, reservationCode);
			int rowsUpdated = stmt.executeUpdate();

			if (rowsUpdated > 0) {
				// Free up the spot if it was assigned
				if (spotId != null) {
					updateParkingSpotStatus(spotId, false);
				}

				// Send email notification
				if (userEmail != null && userName != null) {
					EmailService.sendReservationCancelled(userEmail, userName, String.valueOf(reservationCode));
				}

				System.out.println("Reservation " + reservationCode + " cancelled (" + currentStatus
						+ " → cancelled) - " + reason);
				return "Reservation cancelled successfully";
			}
		} catch (SQLException e) {
			System.out.println("Error cancelling reservation: " + e.getMessage());
		}

		return "Reservation not found or already cancelled/finished";
	}

	public ParkingSubscriber getSubscriberByName(String name) {
		ParkingSubscriber subscriber = null;
		String query = "SELECT * FROM users WHERE Name = ?";
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
		}
		return subscriber;
	}

	public List<ParkingSubscriber> getAllSubscribers() {
		List<ParkingSubscriber> list = new ArrayList<>();
		String query = "SELECT * FROM users WHERE UserTypeEnum = 'sub'";

		try (PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				ParkingSubscriber subscriber = new ParkingSubscriber(rs.getInt("User_ID"), rs.getString("UserName"), // ←
																														// subscriberCode
						rs.getString("Name"), // ← firstName
						rs.getString("Phone"), rs.getString("Email"), rs.getString("CarNum"),
						rs.getString("UserTypeEnum"));
				list.add(subscriber);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return list;
	}








    


 
    /**
     * Extends parking time
     */
    public String extendParkingTime(String parkingCodeStr, int additionalHours) {
        if (additionalHours < 1 || additionalHours > 4) {
            return "Can only extend parking by 1–4 hours.";
        }

        try {
            int parkingCode = Integer.parseInt(parkingCodeStr);

            // Get current parking info and user data
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

                        // ❌ Block if already extended once
                        if ("yes".equalsIgnoreCase(isExtended)) {
                            return "Cannot extend again: You already extended this active parking session.";
                        }

                        Timestamp currentEstimatedEnd = rs.getTimestamp("Estimated_end_time");
                        String userEmail = rs.getString("Email");
                        String userName = rs.getString("Name");
                        int parkingSpotId = rs.getInt("ParkingSpot_ID");

                        LocalDateTime newEstimatedEnd = currentEstimatedEnd.toLocalDateTime().plusHours(additionalHours);

                        // Check for conflicting reservation
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

                        // ✅ Update estimated end time and mark as extended
                        String updateQry = """
                            UPDATE parkinginfo 
                            SET Estimated_end_time = ?, IsExtended = 'yes' 
                            WHERE ParkingInfo_ID = ?
                        """;

                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQry)) {
                            updateStmt.setTimestamp(1, Timestamp.valueOf(newEstimatedEnd));
                            updateStmt.setInt(2, parkingCode);
                            updateStmt.executeUpdate();

                            try {
                                if (userEmail != null && userName != null) {
                                    EmailService.sendExtensionConfirmation(
                                        userEmail, userName, parkingCodeStr,
                                        additionalHours, newEstimatedEnd.toString()
                                    );
                                }
                            } catch (Exception e) {
                                System.out.println("Email sending failed: " + e.getMessage());
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
        }

        return "Invalid parking code or parking session not active.";
    }

 

   
    
    /**
     * Updates subscriber information
     */
    public String updateSubscriberInfo(String updateData) {
        // Format: userName,phone,email
        String[] data = updateData.split(",", -1); // -1 keeps empty values
        if (data.length != 4) {
            return "Invalid update data format";
        }

        String userName = data[0];
        String phone = data[1];
        String email = data[2];
        String carNumber = data[3];

        // Build the update query dynamically
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

        try (PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {
            // Set the values for updated fields
            for (int i = 0; i < values.size(); i++) {
                stmt.setString(i + 1, values.get(i));
            }
            stmt.setString(values.size() + 1, userName); // Set username as the last parameter

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                return "Subscriber information updated successfully";
            }
        } catch (SQLException e) {
            System.out.println("Error updating subscriber info: " + e.getMessage());
        }

        return "Failed to update subscriber information";
    }
    /**
     * Gets detailed parking status including occupied, available (considering time conflicts), and active reservations
     * @return String with format "occupied,available,activeReservations"
     */
    public String getDetailedParkingStatus() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDate today = now.toLocalDate();
            
            // 1. Count occupied spots (active parking sessions)
            String occupiedQuery = """
                SELECT COUNT(DISTINCT ParkingSpot_ID) as occupied
                FROM parkinginfo
                WHERE statusEnum = 'active'
                AND ParkingSpot_ID IS NOT NULL
                """;
            
            int occupiedSpots = 0;
            try (PreparedStatement stmt = conn.prepareStatement(occupiedQuery)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        occupiedSpots = rs.getInt("occupied");
                    }
                }
            }
            
            // 2. Count spots that are truly available (not occupied AND no conflicting preorders)
            String conflictingQuery = """
                SELECT DISTINCT ParkingSpot_ID
                FROM parkinginfo
                WHERE ParkingSpot_ID IS NOT NULL
                AND (
                    -- Active sessions
                    statusEnum = 'active'
                    OR
                    -- Preorders that conflict with current time (±15 min window)
                    (statusEnum = 'preorder' 
                     AND Estimated_start_time <= ? 
                     AND Estimated_end_time >= ?)
                )
                """;
            
            LocalDateTime windowStart = now.minusMinutes(15);
            LocalDateTime windowEnd = now.plusMinutes(15);
            
            int conflictingSpots = 0;
            try (PreparedStatement stmt = conn.prepareStatement(conflictingQuery)) {
                stmt.setTimestamp(1, Timestamp.valueOf(windowEnd));
                stmt.setTimestamp(2, Timestamp.valueOf(windowStart));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        conflictingSpots++;
                    }
                }
            }
            
            int availableSpots = TOTAL_PARKING_SPOTS - conflictingSpots;
            
            // 3. Count active reservations (preorders scheduled for today)
            String reservationsQuery = """
                SELECT COUNT(*) as reservations
                FROM parkinginfo
                WHERE statusEnum = 'preorder'
                AND DATE(Estimated_start_time) = ?
                AND Estimated_start_time >= ?
                """;
            
            int activeReservations = 0;
            try (PreparedStatement stmt = conn.prepareStatement(reservationsQuery)) {
                stmt.setDate(1, Date.valueOf(today));
                stmt.setTimestamp(2, Timestamp.valueOf(now));
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        activeReservations = rs.getInt("reservations");
                    }
                }
            }
            
            // Return as comma-separated string
            return String.format("%d,%d,%d", occupiedSpots, availableSpots, activeReservations);
            
        } catch (SQLException e) {
            System.out.println("Error getting detailed parking status: " + e.getMessage());
            return "0,0,0";
        }
    }


}