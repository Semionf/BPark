package entities;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a parking subscriber in the ParkB system.
 * 
 * Each subscriber has a unique subscriber code, contact information,
 * car number, user type (subscriber, employee, manager), and a list of their
 * past parking orders.
 */
public class ParkingSubscriber implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Unique identifier for the subscriber (internal ID). */
    private int subscriberID;

    /** Unique subscriber code used for identification and login. */
    private String subscriberCode;

    /** Subscriber's first name. */
    private String firstName;

    /** Subscriber's phone number. */
    private String phoneNumber;

    /** Subscriber's email address. */
    private String email;

    /** The license plate number of the subscriber's car. */
    private String carNumber;

    /**
     * Type of user: "sub" for subscriber, "emp" for employee, "mng" for manager.
     */
    private String userType;

    /** A list of parking orders made by the subscriber. */
    private ArrayList<ParkingOrder> parkingHistory;

    // ---------------- Constructors ----------------

    /**
     * Default constructor. Initializes an empty parking history list.
     */
    public ParkingSubscriber() {
        this.parkingHistory = new ArrayList<>();
    }

    /**
     * Constructs a new ParkingSubscriber with the specified information.
     *
     * @param subscriberID   internal ID of the subscriber
     * @param subscriberCode unique code of the subscriber
     * @param firstName      subscriber's first name
     * @param phoneNumber    subscriber's phone number
     * @param email          subscriber's email address
     * @param carNumber      subscriber's car license plate
     * @param userType       user type ("sub", "emp", or "mng")
     */
    public ParkingSubscriber(int subscriberID, String subscriberCode, String firstName,
                             String phoneNumber, String email, String carNumber, String userType) {
        this.subscriberID = subscriberID;
        this.subscriberCode = subscriberCode;
        this.firstName = firstName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.carNumber = carNumber;
        this.userType = userType;
        this.parkingHistory = new ArrayList<>();
    }

    // ---------------- Getters and Setters ----------------

    /**
     * Returns the subscriber's internal ID.
     *
     * @return the subscriber ID
     */
    public int getSubscriberID() {
        return subscriberID;
    }

    /**
     * Sets the subscriber's internal ID.
     *
     * @param subscriberID the new subscriber ID
     */
    public void setSubscriberID(int subscriberID) {
        this.subscriberID = subscriberID;
    }

    /**
     * Returns the subscriber's unique code.
     *
     * @return the subscriber code
     */
    public String getSubscriberCode() {
        return subscriberCode;
    }

    /**
     * Sets the subscriber's unique code.
     *
     * @param subscriberCode the new subscriber code
     */
    public void setSubscriberCode(String subscriberCode) {
        this.subscriberCode = subscriberCode;
    }

    /**
     * Returns the subscriber's first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the subscriber's first name.
     *
     * @param firstName the new first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the subscriber's phone number.
     *
     * @return the phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the subscriber's phone number.
     *
     * @param phoneNumber the new phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns the subscriber's email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the subscriber's email address.
     *
     * @param email the new email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the subscriber's car number (license plate).
     *
     * @return the car number
     */
    public String getCarNumber() {
        return carNumber;
    }

    /**
     * Sets the subscriber's car number.
     *
     * @param carNumber the new car number
     */
    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    /**
     * Returns the type of user.
     *
     * @return the user type ("sub", "emp", or "mng")
     */
    public String getUserType() {
        return userType;
    }

    /**
     * Sets the type of user.
     *
     * @param userType the new user type ("sub", "emp", or "mng")
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * Returns the list of parking orders associated with this subscriber.
     *
     * @return the parking history list
     */
    public ArrayList<ParkingOrder> getParkingHistory() {
        return parkingHistory;
    }

    /**
     * Sets the parking history list.
     *
     * @param parkingHistory the new parking history list
     */
    public void setParkingHistory(ArrayList<ParkingOrder> parkingHistory) {
        this.parkingHistory = parkingHistory;
    }

    /**
     * Adds a new parking order to the subscriber's history.
     *
     * @param order the parking order to add
     */
    public void addParkingOrder(ParkingOrder order) {
        this.parkingHistory.add(order);
    }

    /**
     * Returns a string representation of the subscriber, excluding the parking history.
     *
     * @return a string with subscriber details
     */
    @Override
    public String toString() {
        return "ParkingSubscriber{" +
                "subscriberID=" + subscriberID +
                ", subscriberCode='" + subscriberCode + '\'' +
                ", firstName='" + firstName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", carNumber='" + carNumber + '\'' +
                ", userType='" + userType + '\'' +
                '}';
    }
}