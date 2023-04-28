package com.example.myntrafind;

import java.util.Calendar;

public class DateTimeUtils {
    public static String getCurrentDateTime() {
        // Get current time and date
        Calendar calendar = Calendar.getInstance();
        String currentTime = calendar.getTime().toString();

        // Return current time and date
        return currentTime;
    }
}