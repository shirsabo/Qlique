package com.example.qlique;

import com.example.qlique.Profile.User;

import java.sql.Timestamp;
import java.util.List;

 public class Event {
    private String uid;
    private String eventName, description;
    private Integer minAge, numOfParticipants;
    private Timestamp startTime, endTime;
    private List<String> hobbiesRelated;
    private User author;

}
