package com.zjgsu.obl.enrollment_service.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "course_id"})
})
public class Enrollment {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "course_id", nullable = false, length = 36)
    private String courseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;

    // Transient 字段 - 不会保存到数据库，但会在JSON中返回
    @Transient
    private String studentId;

    @Transient
    private Object courseInfo;

    @PrePersist
    protected void onCreate() {
        enrolledAt = LocalDateTime.now();
    }

    // 构造方法
    public Enrollment() {
    }

    public Enrollment(Student student, String courseId) {
        this.student = student;
        this.courseId = courseId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Object getCourseInfo() {
        return courseInfo;
    }

    public void setCourseInfo(Object courseInfo) {
        this.courseInfo = courseInfo;
    }
}
