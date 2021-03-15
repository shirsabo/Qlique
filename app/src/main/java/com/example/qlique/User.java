package com.example.qlique;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import android.os.Parcelable;

import kotlinx.android.parcel.Parcelize;

@Parcelize
public class User implements Parcelable{
    public String firstName, lastName, email, city, gender, uid, url, instagramUserName;
    public List<String> friends;
    public List<String> hobbies;



    public User(String firstName, String lastName, String city, String email, String gender,String uid, List<String> hobbies,String url, String instagramUserName)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.city = city;
        this.gender = gender;
        this.friends = new ArrayList<>();
        friends.add("shirsabo");
        this.hobbies = hobbies;
        this.uid = uid;
        this.url = url;
        this.instagramUserName = instagramUserName;
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
    }
}
