package ch.epfl.sweng.project;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Class representing a locationName
 */
public class Location implements Parcelable{

    /**
     * Used to regenerate a Task, all parcelables must have a creator
     */
    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
        /**
         * Create a new instance of the Task with the data previously
         * written by writeToParcel().
         *
         * @param in The Parcel to read the object's data from
         * @return New instance of Task
         */
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        /**
         * Create a new array of Task
         * @param size Size of the array
         * @return An array of Task
         */
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    private String name;
    private double latitude;
    private double longitude;

    /**
     * Constructor of the class
     *
     * @param name       Location name
     * @param latitude   Latitude of the locationName
     * @param longitude  Longitude of the locationName
     * @throws IllegalArgumentException if the parameter is null
     */
    public Location(String name, double latitude, double longitude) {
        if (name == null)
            throw new IllegalArgumentException("Name passed to the Location's constructor is null");

        if (latitude < -90 || latitude > 90)
            throw new IllegalArgumentException("Latitude is out of range");

        if (longitude < -180 || longitude > 180)
            throw new IllegalArgumentException("Longitude is out of range");

        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Private constructor used to recreate a Task when
     * it was put inside an Intent.
     *
     * @param in Container of a Task
     */
    private Location(@NonNull Parcel in) {
        this(in.readString(), in.readDouble(), in.readDouble());
    }

    /**
     * Default constructor initializing fields to default values
     */
    public Location() {
        this("Everywhere", 0, 0);
    }

    /**
     * Getter returning the name of the locationName
     */
    public String getName() {
        return name;
    }

    /**
     * Setter to modify the locationName name
     *
     * @param newName The new locationName name
     * @throws IllegalArgumentException if newName is null
     */
    public void setName(String newName) {
        if (newName == null) {
            throw new IllegalArgumentException("New name passed to the Location's setter is null");
        }
        this.name = newName;
    }

    /**
     * Getter returning the longitude of the locationName
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Setter to modify the longitude
     *
     * @param newLongitude The new longitude
     * @throws IllegalArgumentException if the argument is not between -180 and 180
     */
    public void setLongitude(double newLongitude) {
        if (newLongitude < -180 || newLongitude > 180)
            throw new IllegalArgumentException("New longitude passed to Location's setter invalid");
        longitude = newLongitude;
    }

    /**
     * Getter returning the latitude of the location
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Setter to modify the latitude
     *
     * @param newLatitude The new latitude
     * @throws IllegalArgumentException if the argument is not between -90 and 90
     */
    public void setLatitude(double newLatitude) {
        if (newLatitude < -90 || newLatitude > 90)
            throw new IllegalArgumentException("New latitude passed to Location's setter invalid");
        latitude = newLatitude;
    }

    /**
     * Override the describeContents method from
     * Parcelable interface.
     *
     * @return 0
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten the Task in to a Parcel
     *
     * @param dest  The parcel in which the Task should be written
     * @param flags Flags about how the object should be written
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}