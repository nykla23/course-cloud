package com.zjgsu.obl.enrollment_service.controller;

import com.zjgsu.obl.enrollment_service.common.ApiResponse;
import com.zjgsu.obl.enrollment_service.model.Enrollment;
import com.zjgsu.obl.enrollment_service.model.EnrollmentStatus;
import com.zjgsu.obl.enrollment_service.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {
    @Autowired
    private EnrollmentService enrollmentService;

    // 学生选课
    @PostMapping
    public ResponseEntity<ApiResponse<Enrollment>> createEnrollment(@RequestBody Enrollment enrollment) {
        try {
            Enrollment createdEnrollment = enrollmentService.createEnrollment(enrollment);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Enrollment created successfully", createdEnrollment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    // 退选
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEnrollment(@PathVariable String id) {
        boolean deleted = enrollmentService.deleteEnrollment(id);
        if (deleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success("Enrollment deleted successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Enrollment not found"));
        }
    }

    // 查询所有选课记录
    @GetMapping
    public ResponseEntity<ApiResponse<List<Enrollment>>> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentService.findAll();
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    // 按课程查询选课记录
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<Enrollment>>> getEnrollmentsByCourse(@PathVariable String courseId) {
        List<Enrollment> enrollments = enrollmentService.findByCourseId(courseId);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    // 按学生查询选课记录
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<Enrollment>>> getEnrollmentsByStudent(@PathVariable String studentId) {
        List<Enrollment> enrollments = enrollmentService.findByStudentId(studentId);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    // 新增：按状态查询选课记录
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Enrollment>>> getEnrollmentsByStatus(@PathVariable EnrollmentStatus status) {
        List<Enrollment> enrollments = enrollmentService.findByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    // 新增：统计课程活跃选课人数
    @GetMapping("/course/{courseCode}/active-count")
    public ResponseEntity<ApiResponse<Long>> getActiveEnrollmentCountByCourse(@PathVariable String courseCode) {
        long count = enrollmentService.countActiveEnrollmentsByCourse(courseCode);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    // 新增：检查学生是否已选某课程
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> checkEnrollmentExists(
            @RequestParam String studentId,
            @RequestParam String courseCode) {
        boolean exists = enrollmentService.hasActiveEnrollment(studentId, courseCode);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    // 新增：多条件组合查询
    @GetMapping("/query")
    public ResponseEntity<ApiResponse<List<Enrollment>>> queryEnrollments(
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) EnrollmentStatus status) {
        List<Enrollment> enrollments = enrollmentService.findByMultipleCriteria(courseCode, studentId, status);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }
}
