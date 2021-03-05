package com.example.qlique;

public class User {
    public String firstName, lastName, email, city, gender;

    public User(String firstName, String lastName, String city, String email, String gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.city = city;
        this.gender = gender;
    }

    public User() {
    }
}
