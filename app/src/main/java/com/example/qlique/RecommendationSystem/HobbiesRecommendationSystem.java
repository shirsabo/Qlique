package com.example.qlique.RecommendationSystem;

import com.example.qlique.CreateEvent.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class HobbiesRecommendationSystem implements RecommendationModel {
    @Override
    public ArrayList<Event> getRecommendedEvents() {
        ArrayList<Event> recommendedEvents = new  ArrayList<Event>();
        // get the current user instance
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return recommendedEvents;
        }
        if (checkMinEventsForUser(user)){
            // The user signed up for enough events for us to learn about him and his preferences.
             return getRecommendationByHobbies(user);
        } else {
            // The user didn't sign up for enough events for us to learn about him.
            return getRecommendationByLocation(user);
        }
    }

    private ArrayList<Event> getRecommendationByLocation(FirebaseUser user) {
        ArrayList<Event> recommendedEvents = new  ArrayList<Event>();
        // return the nearest places to the user.
        return recommendedEvents;
    }

    private ArrayList<Event> getRecommendationByHobbies(FirebaseUser user) {
        ArrayList<Event> recommendedEvents = new  ArrayList<Event>();
        // Get the events that match the user by his hobbies.
        // return the nearest places to the user first.
        return recommendedEvents;
    }

    private boolean checkMinEventsForUser(FirebaseUser user) {
        // Check if the user signed up for mor than the minimum requirement.
        return true;
    }
}
