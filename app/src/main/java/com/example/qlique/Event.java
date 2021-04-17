package com.example.qlique;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.qlique.Profile.User;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import kotlinx.android.parcel.Parcelize;

@Parcelize
 public class Event implements Parcelable {
     public String photoUrl;
     public String uid;
     public String description;
     public List<String> hobbiesRelated;
    public List<String> members;
     public Event(String photoUrlIn, String uidIn,String descriptionIn, ArrayList<String> hobbiesRelatedIn){
         uid = uidIn;
         photoUrl = photoUrlIn;
         description = descriptionIn;
         hobbiesRelated =  hobbiesRelatedIn;
         members = new ArrayList<String>();
     }
     public void addMember(String newMember){
         if (newMember!=null){
             members.add(newMember);
         }
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

    }
}


