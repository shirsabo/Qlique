package com.example.qlique.CreateEvent;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import kotlinx.android.parcel.Parcelize;

@Parcelize
/**
 * Class Event.
 * The class which represents the events in the DB.
 * All the events on the application are represented as class objects.
 */
public class Event implements Parcelable, Serializable {
    public String photoUrl; // the url of the photo in firebase
    public String uid; // the unique id of the author, matches the id in the Firebase
    public String eventUid; //the unique id of the event, matches the id in the Firebase
    public String description; //the description of the event
    public List<String> hobbiesRelated; ////the hobbies related to the event
    public List<String> members; //list of members' unique ids in Firebase.
    public Double longitude; //longitude of the event
    public Double latitude; //latitude of the event
    public String  header; //header of the event
    public String date;//the date which the event accrues
    public String hour;//the hour which the event accrues
    public int membersCapacity; //the capacity of members
    /**
     * Constructor.
     */
    public Event(){}
    /**
     * Constructor.
     * @params photoUrlIn, String uidIn,String descriptionIn, ArrayList<String> hobbiesRelatedIn
     */
    public Event(String photoUrlIn, String uidIn,String descriptionIn, ArrayList<String> hobbiesRelatedIn){
        uid = uidIn;
        photoUrl = photoUrlIn;
        description = descriptionIn;
        hobbiesRelated =  hobbiesRelatedIn;
        members = new ArrayList<String>();
        longitude = 0.0;
        latitude = 0.0;
    }
    /**
     * Sets the event's uid
     */
    public void setEventUid(String eventUidIn){
        this.eventUid = eventUidIn;
    }
    /**
     * Sets the event's hour
     */
    public void setHour(String hourIn){
        this.hour = hourIn;
    }
    /**
     * Sets the event's Date
     */
    public void setDate(String dateIn) {
        this.date = dateIn;
    }
    /**
     * Sets the event's members Capacity
     */
    public void setMembersCapacity(int capacityIn) {
        this.membersCapacity = capacityIn;
    }
    /**
     * Constructor for Parcelize Event
     */
    protected Event(Parcel in) {
        photoUrl = in.readString();
        uid = in.readString();
        description = in.readString();
        hobbiesRelated = in.createStringArrayList();
        members = in.createStringArrayList();
        longitude = in.readDouble();
        latitude = in.readDouble();
        header =in.readString();
        date = in.readString();
        membersCapacity = in.readInt();
        hour = in.readString();
        eventUid = in.readString();
    }
    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    /**
     * writes to parcel using dest
     * @param dest - used for writing the data
     */
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(photoUrl);
        dest.writeString(uid);
        dest.writeString(description);
        dest.writeStringList(hobbiesRelated);
        dest.writeStringList(members);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeString(header);
        dest.writeString(date);
        dest.writeInt(membersCapacity);
        dest.writeString(hour);
        dest.writeString(eventUid);
    }
}


