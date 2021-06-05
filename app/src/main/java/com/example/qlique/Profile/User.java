package com.example.qlique.Profile;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kotlinx.android.parcel.Parcelize;

@Parcelize
public class User implements Parcelable{
    public String firstName, lastName, email, city, gender, uid, url, instagramUserName, tokenFCM;
    public List<String> friends;
    public List<String> hobbies;
    public List<String> events;

    public void setInstagramUserName(String instagramUserName) {
        this.instagramUserName = instagramUserName;
    }

    public User(String firstName, String lastName, String city, String email, String gender, String uid, String url, String instagramUserName)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.city = city;
        this.gender = gender;
        this.friends = new ArrayList<>();
        friends.add("shirsabo");
        this.uid = uid;
        this.url = url;
        this.instagramUserName = instagramUserName;
        this.events =  new ArrayList<>();
    }

    public User() {
    }

    protected User(Parcel in) {
        firstName = in.readString();
        lastName = in.readString();
        email = in.readString();
        city = in.readString();
        gender = in.readString();
        uid =in.readString();
        url = in.readString();
        friends = in.createStringArrayList();
        hobbies = in.createStringArrayList();
        events = in.createStringArrayList();
        tokenFCM = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public List<String> getFriends() {
        return friends;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(email);
        dest.writeString(city);
        dest.writeString(gender);
        dest.writeString(uid);
        dest.writeString(url);
        dest.writeStringList(friends);
        dest.writeStringList(hobbies);
        dest.writeStringList(events);
        dest.writeString(tokenFCM);
    }
    public static User createUserFromHashMap(HashMap map){
        User user = new User(map.get("firstName").toString(), map.get("lastName").toString(), map.get("city").toString(),
                map.get("email").toString(), map.get("gender").toString(),
                map.get("uid").toString(), map.get("url").toString(), map.get("instagramUserName").toString());
        user.events = (List<String>) map.get("events");
        user.friends = (List<String>) map.get("friends");
        user.hobbies = (List<String>) map.get("hobbies");
        return user;
    }
}
