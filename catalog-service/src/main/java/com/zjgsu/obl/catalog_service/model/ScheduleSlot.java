package com.zjgsu.obl.catalog_service.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;


public class ScheduleSlot {
    private String dayOfWeek; // 例如： "MONDAY"
    private String startTime; // 例如： "08:00"
    private String endTime;   // 例如： "10:00"
    private Integer expectedAttendance;

    // 构造方法
    public ScheduleSlot() {}
    public ScheduleSlot(String dayOfWeek, String startTime, String endTime, Integer expectedAttendance) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.expectedAttendance = expectedAttendance;
    }

    // Getter 和 Setter 方法
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Integer getExpectedAttendance() { return expectedAttendance; }
    public void setExpectedAttendance(Integer expectedAttendance) { this.expectedAttendance = expectedAttendance; }
}
