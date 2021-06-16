package com.example.qlique.CreateEvent;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class CalendarEvent {
    private static String[] splitTimeString(String timeIn) {
        String[] parts = timeIn.split("[:]+");
        return parts;
    }
    public static boolean isTimePassed(String timeIn) {
        if (timeIn==null){
            return true;
        }
        String[] parts = timeIn.split("[:]+");
        if(parts.length!=2){
            return true;
        }
        int hour =  Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int curHour  = Integer.parseInt(splitTimeString(getCurrentTime())[0]);
        int curmin  = Integer.parseInt(splitTimeString(getCurrentTime())[1]);
        if (curHour>= hour && curmin>=minutes ){
            return true;
        }else{
            return false;
        }

    }

    public static boolean isDatePassed(String dateIn){
        if (dateIn==null){
            return true;
        }
        String[] parts = dateIn.split("[.]+");
        if(parts.length!=3){
           return true;
        }
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = Integer.parseInt(parts[2]);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
        if (c.get(Calendar.YEAR)> year){
            return true;
        }if(c.get(Calendar.MONTH) == month) {
            if(Calendar.DAY_OF_MONTH<day){
                return true;
            }else{return false;}
        }else{
            return true;
        }
    }
    public static Date getCurrentDate(){
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
        return c.getTime();
    }
    public static String getCurrentTime(){
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        String curTime = timeFormat.format(new Date());
        return curTime;
    }

}
