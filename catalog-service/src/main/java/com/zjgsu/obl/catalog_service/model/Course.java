package com.zjgsu.obl.catalog_service.model;


import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id; // 系统生成的课程ID

    @Column(name = "course_code", unique = true,nullable = false,length = 50)
    private String courseCode; // 课程代码，如 "CS101"

    @Column(nullable = false)
    private String title; // 课程名称

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "instructor_id")),
            @AttributeOverride(name = "name", column = @Column(name = "instructor_name")),
            @AttributeOverride(name = "email", column = @Column(name = "instructor_email"))
    })
    private Instructor instructor; // 授课教师

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "dayOfWeek", column = @Column(name = "schedule_day")),
            @AttributeOverride(name = "startTime", column = @Column(name = "schedule_start_time")),
            @AttributeOverride(name = "endTime", column = @Column(name = "schedule_end_time")),
            @AttributeOverride(name = "expectedAttendance", column = @Column(name = "expected_attendance"))
    })
    private ScheduleSlot schedule; // 课程安排

    @Column(nullable = false)
    private Integer capacity = 0; // 课程容量

    @Column(name = "enrolled_count",nullable = false)
    private Integer enrolledCount = 0; // 已选课人数，初始为0

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Course() {
        this.enrolledCount = 0;
    }

    public Course(String id, String code, String title, Instructor instructor, ScheduleSlot schedule, Integer capacity) {
        this.id = id;
        this.courseCode = code;
        this.title = title;
        this.instructor = instructor;
        this.schedule = schedule;
        this.capacity = capacity;
        this.enrolledCount = 0;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getCode() {
        return courseCode;
    }
    public void setCode(String code) {
        this.courseCode = code;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Instructor getInstructor() {
        return instructor;
    }
    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }
    public ScheduleSlot getSchedule() {
        return schedule;
    }
    public void setSchedule(ScheduleSlot schedule) {
        this.schedule = schedule;
    }
    public Integer getCapacity() {
        return capacity;
    }
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    public Integer getEnrolled() {
        return enrolledCount;
    }
    public void setEnrolled(Integer enrolled) {
        this.enrolledCount = enrolled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setEnrolledCount(Integer enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public Integer getEnrolledCount() {
        return enrolledCount;
    }
}
