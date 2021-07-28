package com.example.qlique.CreateEvent;

import android.annotation.SuppressLint;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
/**
 * Class CalendarEvent.
 * This class is responsible for the time of the event, and current time, checks if event has passed.
 */
public class CalendarEvent {
    /**
     * Splits the time to hours,minutes
     * @return parts- the data
     */
    private static String[] splitTimeString(String timeIn) {
        String[] parts = timeIn.split("[:]+");
        return parts;
    }
    /**
     * checks if the time of event has passed (the event is on current date)
     * @param timeIn - the time to check.
     * @return True if has passed, else false
     */
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
        // if the curHour is greater or the cur min is greater then the time passed
        return curHour >= hour && curMin >= minutes;
    }
    /**
     * Checks if the event is in the current day, if its true it checks if the event happened today and return false.
     * If the event is in date that accrued returns True.
     * If the date is in the future returns False.
     * @param dateIn - the date
     *  @param hour - the hour
     * @return True if has passed, else false
     */
    public static boolean isEventPassed(String dateIn, String hour){
        if (dateIn==null){
            return true;
        }
        String[] parts = dateIn.split("[.]+");
        if(parts.length!=3){ //checks the format
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
            if(cur_month > month) {// for example: April>march ->passed
                return true;
            }else if (cur_month==month){
                if(cur_day_of_month>day){ // for example: 3>2 ->passed
                    return true;
                }else if(cur_day_of_month==day){ //same day
                    return isTimePassed(hour);
                }
            }
        }
        return false;
    }
    /**
     * @return the current time
     */
    public static String getCurrentTime(){
        @SuppressLint("SimpleDateFormat") DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        return timeFormat.format(new Date());
    }

}
