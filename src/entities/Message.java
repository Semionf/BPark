package entities;

import java.io.Serializable;

/**
 * Represents a message used for communication between the client and server in
 * the ParkB parking system. Each message consists of a type and content.
 * 
 * The message type defines the kind of operation requested or responded to, and
 * the content holds the actual data, such as objects, strings, or other
 * serializable data used in the operation.
 * 
 * @author ParkB Team
 * @version 1.0
 */

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;

	// ***************************Class variables ***************************

	/**
	 * The type of the message indicating the operation to perform or that was
	 * performed.
	 */
	private MessageType type;

	/**
	 * The content of the message. This can include data like subscriber info,
	 * parking orders, response messages, etc.
	 */
	private Serializable content;

	/**
	 * Enum representing all supported message types in the ParkB parking system.
	 */
	public enum MessageType {
		/** Register new subscriber request */
		REGISTER_SUBSCRIBER,
		/** Registration response */
		REGISTRATION_RESPONSE,
		/**
		 * Generate unique username request
		 */
		GENERATE_USERNAME,
		/**
		 * Generated username response
		 */
		USERNAME_RESPONSE,

		/**
		 * Subscriber login request
		 */
		SUBSCRIBER_LOGIN,
		/**
		 * Subscriber login response
		 */
		SUBSCRIBER_LOGIN_RESPONSE,
		/**
		 * Check parking availability
		 */
		CHECK_PARKING_AVAILABILITY,
		/**
		 * Parking availability response
		 */
		PARKING_AVAILABILITY_RESPONSE,
		/**
		 * Reserve parking spot
		 */
		RESERVE_PARKING,
		/**
		 * Reservation response
		 */
		RESERVATION_RESPONSE,
		/**
		 * Enter parking request
		 */
		ENTER_PARKING,
		/**
		 * Enter parking response
		 */
		ENTER_PARKING_RESPONSE,
		/**
		 * Exit parking request
		 */
		EXIT_PARKING,
		/**
		 * Exit parking response
		 */
		EXIT_PARKING_RESPONSE,
		/**
		 * Extend parking time
		 */
		EXTEND_PARKING,
		/**
		 * Extend parking response
		 */
		EXTEND_PARKING_RESPONSE,
		/**
		 * Request lost parking code
		 */
		REQUEST_LOST_CODE,
		/**
		 * Lost code response
		 */
		LOST_CODE_RESPONSE,
		/**
		 * Get parking history
		 */
		GET_PARKING_HISTORY,
		/**
		 * Parking history response
		 */
		PARKING_HISTORY_RESPONSE,
		/**
		 * Manager login request
		 */
		MANAGER_LOGIN,
		/**
		 * Manager login response
		 */
		MANAGER_LOGIN_RESPONSE,
		/**
		 * Get active parkings (for attendant)
		 */
		GET_ACTIVE_PARKINGS,
		/**
		 * Active parkings response
		 */
		ACTIVE_PARKINGS_RESPONSE,
		/**
		 * Manager get reports request
		 */
		MANAGER_GET_REPORTS,
		/**
		 * Manager send reports response
		 */
		MANAGER_SEND_REPORTS,
		/**
		 * Update subscriber information
		 */
		UPDATE_SUBSCRIBER_INFO,
		/**
		 * Update subscriber response
		 */
		UPDATE_SUBSCRIBER_RESPONSE,
		/**
		 * Generate monthly reports
		 */
		GENERATE_MONTHLY_REPORTS,
		/**
		 * Monthly reports response
		 */
		MONTHLY_REPORTS_RESPONSE,

		/**
		 * Get available time slots for a date/time (15-minute precision)
		 */
		GET_TIME_SLOTS,
		/**
		 * Time slots response
		 */
		TIME_SLOTS_RESPONSE,
		/**
		 * Make pre-booking reservation (DATETIME format)
		 */
		MAKE_PREBOOKING,
		/**
		 * Pre-booking response
		 */
		PREBOOKING_RESPONSE,
		/**
		 * Spontaneous parking entry (immediate spot assignment)
		 */
		SPONTANEOUS_PARKING,
		/**
		 * Spontaneous parking response
		 */
		SPONTANEOUS_RESPONSE,
		/**
		 * Request parking extension (during last hour)
		 */
		REQUEST_EXTENSION,
		/**
		 * Extension response
		 */
		EXTENSION_RESPONSE,
		/**
		 * Get system status
		 */
		GET_SYSTEM_STATUS,
		/**
		 * System status response
		 */
		SYSTEM_STATUS_RESPONSE,

		/**
		 * Activate reservation when arriving
		 */
		ACTIVATE_RESERVATION,
		/**
		 * Activation response
		 */
		ACTIVATION_RESPONSE,
		/**
		 * Cancel reservation
		 */
		CANCEL_RESERVATION,
		/**
		 * Cancellation response
		 */
		CANCELLATION_RESPONSE,

		/** Request subscriber by name */
		GET_SUBSCRIBER_BY_NAME,
		/** Request all subscribers */
		GET_ALL_SUBSCRIBERS,
		/** Show all subscribers in the system */
		SHOW_ALL_SUBSCRIBERS,
		/** Show details of a specific subscriber */
		SHOW_SUBSCRIBER_DETAILS,
		/** Request subscriber data */
		REQUEST_SUBSCRIBER_DATA,
		/** Response with subscriber data */
		SUBSCRIBER_DATA_RESPONSE,

		/**
		 * Sent by the client to request the latest dashboard statistics. Expected to be
		 * handled by the server, which queries the database and returns a summarized
		 * view of parking data.
		 */
		DASHBOARD_DATA_REQUEST,

		/**
		 * Sent by the server in response to DASHBOARD_DATA_REQUEST. Contains a
		 * DashboardData object with aggregated statistics such as total spots,
		 * occupied, available, reservations, etc.
		 */
		DASHBOARD_DATA_RESPONSE,

	}

	// ****************************Constructors **************************
	
	/**
	 * Constructs a new Message instance with a specific type and content.
	 *
	 * @param type    the type of the message (operation identifier)
	 * @param content the content of the message, must implement {@link Serializable}
	 */
	public Message(MessageType type, Serializable content) {
		this.setType(type);
		this.setContent(content);
	}

	// Methods ***********************************************************

	/**
	 * Returns the type of the message.
	 * 
	 * @return the type of the message
	 */
	public MessageType getType() {
		return type;
	}

	/**
	 * Sets the type of the message.
	 * 
	 * @param type the new type of the message
	 */
	public void setType(MessageType type) {
		this.type = type;
	}

	/**
	 * Returns the content of the message.
	 * 
	 * @return the content of the message
	 */
	public Serializable getContent() {
		return content;
	}

	/**
	 * Sets the content of the message.
	 * 
	 * @param content the new content of the message
	 */
	public void setContent(Serializable content) {
		this.content = content;
	}
}