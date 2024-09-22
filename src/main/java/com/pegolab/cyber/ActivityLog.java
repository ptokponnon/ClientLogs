package com.pegolab.cyber;

import java.time.LocalTime;

public class ActivityLog {
    private String startDate;
    private String endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String duration;
    private String ipAddress;
    private static String totalDuration; 

    public ActivityLog(String startDate, String endDate, LocalTime startTime, LocalTime endTime) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public String getStartDate() {
        return startDate;
    }
    public String getEndDate() {
        return endDate;
    }
    public LocalTime getStartTime() {
        return startTime;
    }
    public LocalTime getEndTime() {
        return endTime;
    }
    
    public String getDuration() {
        return duration;
    }

    public String getIpAddress() {
        return ipAddress;
    }
    
    public static String getTotalDuration() {
        return ActivityLog.totalDuration;
    }
    
    public void setStartDate(String string) {
        this.startDate = string;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setDuration(String string) {
        this.duration = string;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public static void setTotalDuration(String totalDuration) {
        ActivityLog.totalDuration = totalDuration;
    }    
}
