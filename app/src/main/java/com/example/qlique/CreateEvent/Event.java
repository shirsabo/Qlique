package com.example.qlique.CreateEvent;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kotlinx.android.parcel.Parcelize;

@Parcelize
public class Event implements Parcelable {
    public String photoUrl;
    public String uid;
    public String eventUid;
    public String description;
    public List<String> hobbiesRelated;
    public List<String> members;
    public Double longitude;
    public Double latitude;
    public String  header;
    public String date;
    public String hour;
    public int members_capacity;
    public Event(String photoUrlIn, String uidIn,String descriptionIn, ArrayList<String> hobbiesRelatedIn){
        uid = uidIn;
        photoUrl = photoUrlIn;
        description = descriptionIn;
        hobbiesRelated =  hobbiesRelatedIn;
        members = new ArrayList<String>();
        longitude = 0.0;
        latitude = 0.0;
    }
    public void setEventUid(String eventUidIn){
        this.eventUid = eventUidIn;
    }
    public void setHour(String hourIn){
        this.hour = hourIn;
    }
    public void setDate(String dateIn) {
        this.date = dateIn;
    }
    public void setMembers_capacity(int capacityIn) {
        this.members_capacity = capacityIn;
    }
    public void setHeader(String header) {
        this.header = header;
    }

    public void addMember(String newMember){
        if (newMember!=null&& members_capacity>members.size()){
            members.add(newMember);
        }
    }
    public void setCoordinate(Double latitude,Double longitude){
        this.longitude =longitude;
        this.latitude = latitude;
    }
    public Event() {
    }
    public void setuid(String uidIn){
        uid = uid;
    }
    public void setdescription(String descriptionIn){
        description = uid;
    }
    public void sethobbiesRelated(List<String>hobbiesRelatedIn){
        hobbiesRelated=hobbiesRelatedIn;
    }
    public void setPhotoUrl(String url){
        photoUrl = url;
    }
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
        members_capacity = in.readInt();
        hour = in.readString();
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
        dest.writeInt(members_capacity);
        dest.writeString(hour);

    }
}


