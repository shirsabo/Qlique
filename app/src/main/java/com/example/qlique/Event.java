package com.example.qlique;

import com.example.qlique.Profile.User;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


 public class Event {
     public String photoUrl;
     public String uid;
     public String description;
     public List<String> hobbiesRelated;
     public Event(String photoUrlIn, String uidIn,String descriptionIn, ArrayList<String> hobbiesRelatedIn){
         uid = uidIn;
         photoUrl = photoUrlIn;
         description = descriptionIn;
         hobbiesRelated =  hobbiesRelatedIn;
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
}


