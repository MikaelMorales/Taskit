package ch.epfl.sweng.project.chat;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 *  Class representing the message in the char.
 */
public class Message implements Parcelable {

    /**
     * Needed variable to allow Message class to be parcelable.
     */
    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    private final String userName;
    private final String body;
    private final long time;

    /**
     * Constructor of the class
     *
     * @param userName Name of the user which sends the message
     * @param body Body of the message
     * @param time time when the message was sent
     */
    public Message(@NonNull String userName, @NonNull String body, long time) {
        this.userName = userName;
        this.body = body;
        this.time = time;
    }

    /**
     * Constructor of the class with default values
     */
    public Message() {
        this("","",0);
    }

    /**
     * Private constructor used to recreate a Message when
     * it was put inside an Intent.
     *
     * @param in Container of a Task
     */
    private Message(@NonNull Parcel in) {
        this(in.readString(), in.readString(), in.readLong());
    }

    /**
     * Getter that returns the name of the user which sends the message
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Getter that returns the body of the message
     */
    public String getBody() {
        return body;
    }

    /**
     * Getter that returns the time when the message was sent
     */
    public long getTime() {
        return time;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     * @see #CONTENTS_FILE_DESCRIPTOR
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userName);
        dest.writeString(body);
        dest.writeLong(time);
    }
}
