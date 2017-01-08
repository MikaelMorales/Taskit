package ch.epfl.sweng.project;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class representing a user
 */
public class User implements Parcelable {
    public static final String DEFAULT_EMAIL = "trixyfinger@gmail.com";
    public static final String DEFAULT_NAME = "Trixy";

    private String email;
    private List<Location> listLocations;

    /**
     * Constructor of the user class which
     * set listLocations to an empty list.
     * @param mail user email
     */
    public User(String mail) {
        if (mail == null) {
            this.email = DEFAULT_EMAIL;
        } else {
            this.email = mail;
        }
        // Default values:
        this.listLocations = Arrays.asList(new Location(), new Location());
    }

    /**
     * Private constructor used to recreate an User when
     * it was put inside an Intent or a Bundle.
     *
     * @param in Container of an User
     */
    private User(@NonNull Parcel in) {
        this.email = in.readString();
        ArrayList<Location> list = new ArrayList<>();
        in.readTypedList(list, Location.CREATOR);
        this.listLocations = new ArrayList<>(list);
    }

    /**
     * Constructor of the class. Implementation of the fields of the class with
     * default values.
     *
     * @throws NullPointerException if an argument is null
     */
    public User(String mail, List<Location> listLocations) {
        if (listLocations == null) {
            throw new NullPointerException();
        } else {
            if(mail == null) {
                this.email = DEFAULT_EMAIL;
            }
            else {
                this.email = mail;
            }
            this.listLocations = new ArrayList<>(listLocations);
        }
    }

    /**
     * Getter returning the email of the user
     *
     * @return email of the user.
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Getter
     *
     * @return list of Locations of the user.
     */
    public List<Location> getListLocations() {
        return listLocations;
    }

    /**
     * Getter returning the name of the locations
     *
     * @return ArrayList of the names of the locations
     */
    public ArrayList<String> getListNamesLocations() {
        ArrayList<String> names = new ArrayList<>();
        for (Location loc : getListLocations()) {
            names.add(loc.getName());
        }
        return names;
    }

    /**
     * Setter that allows to change the locations of the user
     *
     * @throws NullPointerException If the argument is null
     */
    public void setListLocations(List<Location> list) {
        if(list == null) {
            throw new IllegalArgumentException("Bad list of locationName given in the setter of user");
        }else{
            this.listLocations = new ArrayList<>(list);
        }
    }
    /**
     * Allow the user to update a specific locationName.
     * @param location The new Location
     */
    public void updateLocation(Location location) {
        if(location == null) {
            throw new IllegalArgumentException("Bad locationName update !");
        }

        for(int i=0; i < listLocations.size(); i++) {
            if(listLocations.get(i).getName().equals(location.getName())) {
                listLocations.set(i,location);
            }
        }
    }

    /**
     * Used to regenerate an User, all parcelables must have a creator
     */
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        /**
         * Create a new instance of an User with the data previously
         * written by writeToParcel().
         *
         * @param in The Parcel to read the object's data from
         * @return New instance of User
         */
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        /**
         * Create a new array of Users
         * @param size Size of the array
         * @return An array of Users
         */
        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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
     * Flatten the User in to a Parcel
     *
     * @param dest  The parcel in which the User should be written
     * @param flags Flags about how the object should be written
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeTypedList(listLocations);
    }
}
