package services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * EmailService handles sending HTML-formatted Hebrew email notifications for
 * the BPARK parking management system.
 * <p>
 * All emails are sent using Gmail SMTP with secure authentication, and support
 * the following notification types:
 * <ul>
 * <li>Registration Confirmation</li>
 * <li>Welcome Message</li>
 * <li>Reservation Confirmation</li>
 * <li>Reservation Cancellation</li>
 * <li>Parking Code Recovery</li>
 * <li>Extension Confirmation</li>
 * <li>Late Pickup Alert</li>
 * <li>Parking Expiration Alert</li>
 * </ul>
 * <p>
 * Each email includes a consistent RTL (Hebrew) styled HTML layout, company
 * branding, date/time, and custom content per user.
 * <p>
 * Usage: use the generic {@code sendNotification()} method, or one of the
 * specific helper methods such as {@code sendRegistrationConfirmation()},
 * {@code sendReservationConfirmation()}, etc.
 *
 * <p>
 * <b>Note:</b> This service sends only Hebrew-language emails and expects all
 * dynamic data to be provided accordingly.
 * </p>
 *
 * @author BPARK
 * @version 1.2
 * @since 2025-07
 */

public class EmailService {

	// Email configuration
	private static final String GMAIL_USERNAME = "idopo25@gmail.com";
	private static final String GMAIL_APP_PASSWORD = "kylk wqxz kquw vccf";
	private static final String SUPPORT_PHONE = "1-800-800-123";
	private static final String SUPPORT_EMAIL = "support@bpark.com";
	private static final String COMPANY_NAME = "BPARK";
	private static final String LOGO_URL = "https://i.postimg.cc/7LFkRhp3/Screenshot-2025-06-04-180239.jpg";

	// Email notification types
	public enum NotificationType {
		LATE_PICKUP, REGISTRATION_CONFIRMATION, RESERVATION_CONFIRMATION, RESERVATION_CANCELLED, PARKING_CODE_RECOVERY,
		EXTENSION_CONFIRMATION, PARKING_EXPIRED, WELCOME_MESSAGE
	}

	/**
	 * Sends a generic notification email based on the given type and parameters.
	 *
	 * @param type           The type of notification to send
	 * @param recipientEmail Recipient's email address
	 * @param customerName   Customer's full name
	 * @param additionalData Additional parameters required for specific email types
	 * @return true if the email was sent successfully, false otherwise
	 */

	public static boolean sendNotification(NotificationType type, String recipientEmail, String customerName,
			Object... additionalData) {
		try {
			Session session = createEmailSession();
			MimeMessage message = new MimeMessage(session);

			// Set sender
			message.setFrom(new InternetAddress(GMAIL_USERNAME, COMPANY_NAME + " System"));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));

			// Get email content based on type
			EmailContent content = generateEmailContent(type, customerName, additionalData);
			message.setSubject(content.subject);
			message.setContent(content.htmlBody, "text/html; charset=UTF-8");

			Transport.send(message);
			System.out.println("✅ Email sent successfully: " + type + " to " + recipientEmail);
			return true;

		} catch (Exception e) {
			System.err.println("❌ Failed to send email: " + type + " to " + recipientEmail);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Sends a late pickup warning email.
	 *
	 * @param recipientEmail Recipient's email address
	 * @param customerName   Customer's full name
	 * @return true if the email was sent successfully, false otherwise
	 */

	public static boolean sendLatePickupNotification(String recipientEmail, String customerName) {
		return sendNotification(NotificationType.LATE_PICKUP, recipientEmail, customerName);
	}

	/**
	 * Sends a registration confirmation email including user ID and username.
	 *
	 * @param recipientEmail Recipient's email address
	 * @param customerName   Customer's full name
	 * @param username       The user's username
	 * @param userID         The user's system ID
	 * @return true if the email was sent successfully, false otherwise
	 */

	public static boolean sendRegistrationConfirmation(String recipientEmail, String customerName, String username,
			int userID) {
		return sendNotification(NotificationType.REGISTRATION_CONFIRMATION, recipientEmail, customerName, username,
				userID);
	}

	/**
	 * Sends a reservation confirmation email.
	 *
	 * @param recipientEmail  Recipient's email address
	 * @param customerName    Customer's full name
	 * @param reservationCode Unique reservation code
	 * @param date            Reservation date
	 * @param spotNumber      Parking spot number
	 * @return true if the email was sent successfully, false otherwise
	 */

	public static boolean sendReservationConfirmation(String recipientEmail, String customerName,
			String reservationCode, String date, String spotNumber) {
		return sendNotification(NotificationType.RESERVATION_CONFIRMATION, recipientEmail, customerName,
				reservationCode, date, spotNumber);
	}

	/**
	 * Sends a reservation cancellation email.
	 *
	 * @param recipientEmail  Recipient's email address
	 * @param customerName    Customer's full name
	 * @param reservationCode The cancelled reservation code
	 * @return true if the email was sent successfully, false otherwise
	 */

	public static boolean sendReservationCancelled(String recipientEmail, String customerName, String reservationCode) {
		return sendNotification(NotificationType.RESERVATION_CANCELLED, recipientEmail, customerName, reservationCode);
	}

	/**
	 * Sends a parking code recovery email.
	 *
	 * @param recipientEmail Recipient's email address
	 * @param customerName   Customer's full name
	 * @param parkingCode    The current active parking code
	 * @return true if the email was sent successfully, false otherwise
	 */

	public static boolean sendParkingCodeRecovery(String recipientEmail, String customerName, String parkingCode) {
		return sendNotification(NotificationType.PARKING_CODE_RECOVERY, recipientEmail, customerName, parkingCode);
	}

	/**
	 * Sends an extension confirmation email including new end time.
	 *
	 * @param recipientEmail Recipient's email address
	 * @param customerName   Customer's full name
	 * @param parkingCode    Current parking code
	 * @param hours          Number of extended hours
	 * @param newEndTime     The new end time after extension
	 * @return true if the email was sent successfully, false otherwise
	 */

	public static boolean sendExtensionConfirmation(String recipientEmail, String customerName, String parkingCode,
			int hours, String newEndTime) {

		return sendNotification(NotificationType.EXTENSION_CONFIRMATION, recipientEmail, customerName, parkingCode,
				hours, newEndTime);
	}

	/**
	 * Sends a parking expired alert email.
	 *
	 * @param recipientEmail Recipient's email address
	 * @param customerName   Customer's full name
	 * @param spotNumber     The spot where parking expired
	 * @return true if the email was sent successfully, false otherwise
	 */

	public static boolean sendParkingExpiredNotification(String recipientEmail, String customerName,
			String spotNumber) {
		return sendNotification(NotificationType.PARKING_EXPIRED, recipientEmail, customerName, spotNumber);
	}

	/**
	 * Sends a welcome message email after successful registration.
	 *
	 * @param recipientEmail Recipient's email address
	 * @param customerName   Customer's full name
	 * @param username       The user's username
	 * @param userID         The user's system ID
	 * @return true if the email was sent successfully, false otherwise
	 */

	public static boolean sendWelcomeMessage(String recipientEmail, String customerName, String username, int userID) {
		return sendNotification(NotificationType.WELCOME_MESSAGE, recipientEmail, customerName, username, userID);
	}

	/**
	 * Creates and configures a Gmail SMTP session for sending emails.
	 *
	 * @return Configured email session with Gmail SMTP and authentication
	 */
	private static Session createEmailSession() {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.starttls.required", "true");
		properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
		properties.put("mail.smtp.connectiontimeout", "10000");
		properties.put("mail.smtp.timeout", "10000");

		return Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(GMAIL_USERNAME, GMAIL_APP_PASSWORD);
			}
		});
	}

	/**
	 * Generates the appropriate email content (subject and HTML body) based on the
	 * notification type and given parameters.
	 *
	 * @param type           The type of notification to generate
	 * @param customerName   The full name of the customer
	 * @param additionalData Optional parameters depending on the email type
	 * @return EmailContent object containing subject and HTML body
	 */
	private static EmailContent generateEmailContent(NotificationType type, String customerName,
			Object... additionalData) {
		String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

		switch (type) {
		case LATE_PICKUP:
			return createLatePickupContent(customerName, currentDate, currentTime);

		case REGISTRATION_CONFIRMATION:
			String username = (String) additionalData[0];
			Integer userID = (Integer) additionalData[1];
			return createRegistrationContent(customerName, username, userID, currentDate, currentTime);

		case RESERVATION_CONFIRMATION:
			String reservationCode = (String) additionalData[0];
			String reservationDate = (String) additionalData[1];
			String spotNumber = (String) additionalData[2];
			return createReservationContent(customerName, reservationCode, reservationDate, spotNumber);

		case RESERVATION_CANCELLED:
			String cancelledCode = (String) additionalData[0];
			return createCancellationContent(customerName, cancelledCode, currentDate, currentTime);

		case PARKING_CODE_RECOVERY:
			String parkingCode = (String) additionalData[0];
			return createCodeRecoveryContent(customerName, parkingCode, currentDate, currentTime);

		case EXTENSION_CONFIRMATION:
			String extendCode = (String) additionalData[0];
			Integer hours = (Integer) additionalData[1];
			String newEndTime = (String) additionalData[2];
			return createExtensionContent(customerName, extendCode, hours, newEndTime);

		case PARKING_EXPIRED:
			String expiredSpot = (String) additionalData[0];
			return createExpiredContent(customerName, expiredSpot, currentDate, currentTime);

		case WELCOME_MESSAGE:
			String welcomeUsername = (String) additionalData[0];
			Integer welcomeUserID = (Integer) additionalData[1];
			return createWelcomeContent(customerName, welcomeUsername, welcomeUserID);

		default:
			return createDefaultContent(customerName);
		}
	}

	/**
	 * Simple structure to hold email subject and HTML body.
	 */
	private static class EmailContent {
		String subject;
		String htmlBody;

		EmailContent(String subject, String htmlBody) {
			this.subject = subject;
			this.htmlBody = htmlBody;
		}
	}

	/**
	 * Creates email content for a late pickup warning notification.
	 *
	 * @param customerName Full name of the customer
	 * @param date         Current date (formatted)
	 * @param time         Current time (formatted)
	 * @return EmailContent object with subject and HTML message
	 */
	private static EmailContent createLatePickupContent(String customerName, String date, String time) {
		String subject = "הודעה על איחור באיסוף הרכב - " + date;
		String content = createEmailTemplate(customerName, date, time, "הודעה על איחור באיסוף הרכב",
				(customerName != null && !customerName.trim().isEmpty() ? "לקוח/ה יקר/ה " + customerName + ","
						: "לקוח/ה יקר/ה,"),
				"ברצוננו להודיעך כי חלה חריגה בזמן איסוף הרכב מהחניון, מעבר לזמן שהוזמן מראש.<br>"
						+ "נודה לך אם תוכל/י להגיע לאסוף את רכבך בהקדם.",
				"<strong>לתשומת לבך:</strong> ייתכן שיחולו חיובים נוספים בגין שהות מעבר לזמן שהוזמן.", "#fff3cd",
				"#ffc107");
		return new EmailContent(subject, content);
	}

	/**
	 * Creates email content for a successful registration confirmation.
	 *
	 * Includes the user's full name, username, and system-assigned user ID. Styled
	 * with a welcoming tone and guidance for future use of the system.
	 *
	 * @param customerName Full name of the customer
	 * @param username     Username assigned to the customer
	 * @param userID       System-generated user ID
	 * @param date         Current date (dd/MM/yyyy)
	 * @param time         Current time (HH:mm)
	 * @return EmailContent containing subject and HTML body
	 */
	private static EmailContent createRegistrationContent(String customerName, String username, int userID, String date,
			String time) {
		String subject = "ברוכים הבאים ל-BPARK - רישום מוצלח!";
		String content = createEmailTemplate(customerName, date, time, "ברוכים הבאים ל-BPARK!",
				"שלום " + customerName + " וברוכים הבאים!",
				"ברוכים הבאים למערכת החניון החכם BPARK!<br>" + "רישומך הושלם בהצלחה.<br><br>"
						+ "<strong>מספר מזהה הלקוח שלך הוא:</strong> " + userID + "<br>"
						+ "<strong>שם המשתמש שלך:</strong> " + username + "<br><br>"
						+ "כעת תוכל להזמין מקומות חניה, לנהל הזמנות ולקבל עדכונים בזמן אמת.",
				"<strong>טיפ:</strong> שמור את מספר המזהה ושם המשתמש שלך במקום בטוח לכניסה מהירה למערכת.", "#d4edda",
				"#28a745");
		return new EmailContent(subject, content);
	}

	/**
	 * Creates email content for a reservation confirmation.
	 *
	 * Includes reservation code, date, and the allocated parking spot number. The
	 * message highlights the importance of arriving on time.
	 *
	 * @param customerName    Full name of the customer
	 * @param reservationCode Unique reservation identifier
	 * @param reservationDate Date of the reservation
	 * @param spotNumber      Allocated parking spot number
	 * @return EmailContent containing subject and HTML body
	 */
	private static EmailContent createReservationContent(String customerName, String reservationCode,
			String reservationDate, String spotNumber) {
		String subject = "אישור הזמנת חניה - קוד " + reservationCode;
		String content = createEmailTemplate(customerName,
				LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
				LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), "אישור הזמנת חניה",
				"שלום " + customerName + ",",
				"הזמנת החניה שלך אושרה בהצלחה!<br><br>" + "<strong>קוד הזמנה:</strong> " + reservationCode + "<br>"
						+ "<strong>תאריך:</strong> " + reservationDate + "<br>" + "<strong>מקום חניה:</strong> "
						+ spotNumber + "<br><br>" + "אנא הגע עם קוד ההזמנה למכונת הכניסה.",
				"<strong>חשוב:</strong> הגעה מאוחרת מעל 15 דקות עלולה לגרום לביטול אוטומטי של ההזמנה.", "#d1ecf1",
				"#17a2b8");
		return new EmailContent(subject, content);
	}

	/**
	 * Creates email content for a reservation cancellation notification.
	 *
	 * Includes the cancelled reservation code and possible reasons for
	 * cancellation. Also offers guidance for rebooking if applicable.
	 *
	 * @param customerName    Full name of the customer
	 * @param reservationCode Code of the cancelled reservation
	 * @param date            Current date (dd/MM/yyyy)
	 * @param time            Current time (HH:mm)
	 * @return EmailContent containing subject and HTML body
	 */
	private static EmailContent createCancellationContent(String customerName, String reservationCode, String date,
			String time) {
		String subject = "ביטול הזמנת חניה - קוד " + reservationCode;
		String content = createEmailTemplate(customerName, date, time, "ביטול הזמנת חניה", "שלום " + customerName + ",",
				"הזמנת החניה שלך בוטלה.<br><br>" + "<strong>קוד הזמנה מבוטל:</strong> " + reservationCode + "<br><br>"
						+ "הביטול יכול להיות מסיבות הבאות:<br>" + "• איחור של מעל 15 דקות (ביטול אוטומטי)<br>"
						+ "• ביטול ידני על ידך<br>" + "• בעיה טכנית במערכת",
				"<strong>הערה:</strong> אם לא ביטלת בעצמך, ניתן ליצור הזמנה חדשה דרך המערכת.", "#f8d7da", "#dc3545");
		return new EmailContent(subject, content);
	}

	/**
	 * Creates email content for parking code recovery.
	 *
	 * Includes the current active parking code and a security note. Presented in a
	 * prominent design to ease user recognition.
	 *
	 * @param customerName Full name of the customer
	 * @param parkingCode  Active parking code to be restored
	 * @param date         Current date (dd/MM/yyyy)
	 * @param time         Current time (HH:mm)
	 * @return EmailContent containing subject and HTML body
	 */
	private static EmailContent createCodeRecoveryContent(String customerName, String parkingCode, String date,
			String time) {
		String subject = "שחזור קוד חניה - BPARK";
		String content = createEmailTemplate(customerName, date, time, "שחזור קוד חניה", "שלום " + customerName + ",",
				"לפי בקשתך, להלן קוד החניה הפעיל שלך:<br><br>"
						+ "<div style='background:#e2f3ff;padding:15px;border-radius:8px;text-align:center;font-size:24px;font-weight:bold;color:#1a237e;'>"
						+ parkingCode + "</div><br>" + "השתמש בקוד זה כדי לצאת מהחניון או לבצע פעולות נוספות.",
				"<strong>אבטחה:</strong> אל תשתף קוד זה עם אחרים. הוא תקף רק עבור ההזמנה הנוכחית שלך.", "#d1ecf1",
				"#17a2b8");
		return new EmailContent(subject, content);
	}

	/**
	 * Creates email content for confirming a parking extension.
	 *
	 * Includes the parking code, number of additional hours, and the new end time.
	 * The message reminds the user to leave on time to avoid extra charges.
	 *
	 * @param customerName Full name of the customer
	 * @param parkingCode  Current parking code
	 * @param hours        Number of extended hours
	 * @param newEndTime   Updated parking expiration time
	 * @return EmailContent containing subject and HTML body
	 */
	private static EmailContent createExtensionContent(String customerName, String parkingCode, int hours,
			String newEndTime) {
		String subject = "אישור הארכת חניה - קוד " + parkingCode;
		String content = createEmailTemplate(customerName,
				LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
				LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), "אישור הארכת חניה",
				"שלום " + customerName + ",",
				"הארכת החניה שלך אושרה בהצלחה!<br><br>" + "<strong>קוד חניה:</strong> " + parkingCode + "<br>"
						+ "<strong>זמן הארכה:</strong> " + hours + " שעות<br>" + "<strong>זמן סיום חדש:</strong> "
						+ newEndTime + "<br><br>" + "תוכל כעת להישאר בחניון עד לזמן החדש.",
				"<strong>תזכורת:</strong> אנא הקפד לצאת עד לזמן החדש כדי למנוע חיובים נוספים.", "#d4edda", "#28a745");
		return new EmailContent(subject, content);
	}

	/**
	 * Creates email content for an expired parking session.
	 *
	 * Informs the user that their time has ended and urges them to remove the car.
	 * Mentions that extra fees may apply beyond the expiration.
	 *
	 * @param customerName Full name of the customer
	 * @param spotNumber   The spot number where the car was parked
	 * @param date         Current date (dd/MM/yyyy)
	 * @param time         Current time (HH:mm)
	 * @return EmailContent containing subject and HTML body
	 */
	private static EmailContent createExpiredContent(String customerName, String spotNumber, String date, String time) {
		String subject = "הודעה על פקיעת זמן חניה - " + date;
		String content = createEmailTemplate(customerName, date, time, "הודעה על פקיעת זמן חניה",
				"שלום " + customerName + ",",
				"זמן החניה שלך פג במקום " + spotNumber + ".<br><br>" + "אנא הגע לאסוף את רכבך בהקדם האפשרי.<br>"
						+ "החל מרגע זה עלולים לחול חיובים נוספים.",
				"<strong>חשוב:</strong> יש לפנות את מקום החניה כדי לא לחסום אותו עבור לקוחות אחרים.", "#fff3cd",
				"#ffc107");
		return new EmailContent(subject, content);
	}

	/**
	 * Creates a warm welcome email for newly registered users.
	 *
	 * Includes user ID, username, and highlights the platform’s key features.
	 * Serves as a motivational introduction to the BPARK system.
	 *
	 * @param customerName Full name of the customer
	 * @param username     Chosen username
	 * @param userID       System-generated user ID
	 * @return EmailContent containing subject and HTML body
	 */
	private static EmailContent createWelcomeContent(String customerName, String username, int userID) {
		String subject = "ברוכים הבאים ל-BPARK - מערכת חניון חכמה!";
		String content = createEmailTemplate(customerName,
				LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
				LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), "ברוכים הבאים ל-BPARK!",
				"שלום " + customerName + " וברוכים הבאים!",
				"אנחנו שמחים שהצטרפת למערכת החניון החכם שלנו.<br><br>" + "<strong>מספר מזהה הלקוח שלך הוא:</strong> "
						+ userID + "<br>" + "<strong>שם המשתמש שלך:</strong> " + username + "<br><br>"
						+ "במערכת שלנו תוכל:<br>" + "• להזמין מקומות חניה מראש<br>" + "• לנהל הזמנות קיימות<br>"
						+ "• לקבל התראות בזמן אמת<br>" + "• לשחזר קודי חניה<br>" + "• להאריך זמן חניה",
				"<strong>התחל עכשיו:</strong> היכנס למערכת עם שם המשתמש שלך ותתחיל ליהנות מחניה חכמה!", "#d4edda",
				"#28a745");
		return new EmailContent(subject, content);
	}

	/**
	 * Creates a fallback email content for unknown or unspecified notification
	 * types.
	 *
	 * Displays a generic greeting and timestamped message from BPARK.
	 *
	 * @param customerName Full name of the customer
	 * @return EmailContent containing subject and HTML body
	 */
	private static EmailContent createDefaultContent(String customerName) {
		return new EmailContent("הודעה מ-BPARK",
				createEmailTemplate(customerName, LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
						LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")), "הודעה מ-BPARK",
						"שלום " + customerName + ",", "קיבלת הודעה מצוות BPARK.", "", "#d1ecf1", "#17a2b8"));
	}

	/**
	 * Generates the full HTML layout for an email with consistent RTL styling and
	 * company branding.
	 *
	 * @param customerName     Customer's full name
	 * @param date             Current date (dd/MM/yyyy)
	 * @param time             Current time (HH:mm)
	 * @param title            Title to show in the email header
	 * @param greeting         Personalized greeting line
	 * @param mainMessage      The main body message
	 * @param alertMessage     Highlighted alert or additional info (can be empty)
	 * @param alertBgColor     Background color for alert box
	 * @param alertBorderColor Border color for alert box
	 * @return Full HTML email string
	 */
	private static String createEmailTemplate(String customerName, String date, String time, String title,
			String greeting, String mainMessage, String alertMessage, String alertBgColor, String alertBorderColor) {
		return "<!DOCTYPE html>" + "<html dir='rtl'>" + "<head>" + "<meta charset='UTF-8'>"
				+ "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" + "</head>"
				+ "<body style='margin:0;padding:20px;background:#f0f0f0;font-family:Arial,sans-serif;'>" +

				"<table style='max-width:600px;margin:auto;border:1px solid #eee;font-family:Arial,sans-serif;background:#fff;'>"
				+

				"<tr>" + "<td style='padding:0;text-align:center;'>" + "<img src='" + LOGO_URL + "' alt='"
				+ COMPANY_NAME + "' style='width:100%;max-width:600px;height:auto;display:block;'>" + "</td>" + "</tr>"
				+

				"<tr>" + "<td style='padding:30px 20px 10px 20px;'>" + "<h2 style='color:#1a237e;margin:0 0 16px 0;'>"
				+ title + "</h2>" +

				"<p style='font-size:16px;color:#333;margin-bottom:18px;'>" + greeting + "</p>" +

				"<div style='background:#f9f9f9;padding:15px;border-right:4px solid #1a237e;margin-bottom:20px;'>"
				+ "<p style='margin:0;font-size:15px;color:#444;'>" + "<strong>תאריך:</strong> " + date + "<br>"
				+ "<strong>שעה:</strong> " + time + "</p>" + "</div>" +

				"<p style='font-size:15px;color:#444;margin-bottom:16px;line-height:1.6;'>" + mainMessage + "</p>" +

				(alertMessage.isEmpty() ? ""
						: "<p style='font-size:15px;color:#444;margin-bottom:20px;padding:10px;background:"
								+ alertBgColor + ";border-right:4px solid " + alertBorderColor + ";'>" + alertMessage
								+ "</p>")
				+

				"<p style='font-size:15px;color:#444;margin-bottom:20px;'>"
				+ "לפרטים נוספים ולסיוע ניתן לפנות אלינו במוקד BPARK בטלפון: " + "<strong>" + SUPPORT_PHONE
				+ "</strong><br>" + "או במייל: " + "<a href='mailto:" + SUPPORT_EMAIL
				+ "' style='color:#1a237e;text-decoration:none;'>" + SUPPORT_EMAIL + "</a>" + "</p>" + "</td>" + "</tr>"
				+

				"<tr>" + "<td style='padding:15px 20px 30px 20px;'>"
				+ "<p style='font-size:16px;color:#1a237e;margin:0;font-weight:bold;'>בברכה,<br>צוות BPARK</p>"
				+ "</td>" + "</tr>" +

				"<tr>" + "<td style='background:#f5f5f5;text-align:center;padding:15px;color:#999;font-size:12px;'>"
				+ "הודעה זו נשלחה באופן אוטומטי ב-" + date + " בשעה " + time + "<br>" + "אין להשיב להודעה זו" + "</td>"
				+ "</tr>" +

				"</table>" + "</body>" + "</html>";
	}
}