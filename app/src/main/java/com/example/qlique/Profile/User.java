package com.example.qlique.Profile;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kotlinx.android.parcel.Parcelize;

@Parcelize
/**
 * User
 * represents a user and the information we save for each one in firebase.
 */
public class User implements Parcelable{
    public String firstName, lastName, email, city, gender, uid, url, instagramUserName, tokenFCM;
    public List<String> friends;
    public List<String> hobbies;
    public List<String> events;

    /**
     * constructor.
     * @param firstName
     * @param lastName
     * @param city
     * @param email
     * @param gender
     * @param uid
     * @param url
     * @param instagramUserName
     */
    public User(String firstName, String lastName, String city, String email, String gender,
                String uid, String url, String instagramUserName) {
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

    /**
     * constructor.
     */
    public User() {
    }

    /**
     * constructor.
     * @param in
     */
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

    /**
     * getter.
     * @return
     */
    public List<String> getFriends() {
        return friends;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    /**
      write the user info into parcel.
     */
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
}
