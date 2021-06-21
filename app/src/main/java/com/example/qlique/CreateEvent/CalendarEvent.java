package com.example.qlique.CreateEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
        int curMin  = Integer.parseInt(splitTimeString(getCurrentTime())[1]);


        if (curHour>= hour && curMin>=minutes ){
            return true;
        }else{
            return false;
        }

    }

    public static boolean isEventPassed(String dateIn, String hour){
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
        int cur_year = c.get(Calendar.YEAR);//counting starts at zero
        int cur_month = c.get(Calendar.MONTH)+1;//counting starts at zero
        int cur_day_of_month = c.get(Calendar.DAY_OF_MONTH);
        if (cur_year> year){ // 2022>2021 ->passed
            return true;
        }else if(cur_year== year){
            if(cur_month > month) {// April>march ->passed
                return true;
            }else if (cur_month==month){
                if(cur_day_of_month>day){ //// 3>2 ->passed
                    return true;
                }else if(cur_day_of_month==day){ //same day
                    return isTimePassed(hour);
                }
            }
        }
        return false;
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
