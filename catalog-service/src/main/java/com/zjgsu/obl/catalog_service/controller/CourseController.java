package com.zjgsu.obl.catalog_service.controller;



import com.zjgsu.obl.catalog_service.common.ApiResponse;
import com.zjgsu.obl.catalog_service.model.Course;
import com.zjgsu.obl.catalog_service.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    @Autowired
    private CourseService courseService;

    // 查询所有课程
    @GetMapping
    public ResponseEntity<ApiResponse<List<Course>>> getAllCourses() {
        List<Course> courses = courseService.findAll();
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    // 查询单个课程
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> getCourseById(@PathVariable String id) {
        return courseService.getById(id)
                .map(course -> ResponseEntity.ok(ApiResponse.success(course)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "Course not found")));
    }

    // 创建课程
    @PostMapping
    public ResponseEntity<ApiResponse<Course>> createCourse(@RequestBody Course course) {
        try {
            Course createdCourse = courseService.createCourse(course);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Course created successfully", createdCourse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    // 更新课程
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> updateCourse(@PathVariable String id, @RequestBody Course course) {
        Course updatedCourse = courseService.updateCourse(id, course);
        if (updatedCourse != null) {
            return ResponseEntity.ok(ApiResponse.success("Course updated successfully", updatedCourse));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Course not found"));
        }
    }

    // 删除课程
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable String id) {
        boolean deleted = courseService.deleteCourse(id);
        if (deleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success("Course deleted successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Course not found"));
        }
    }

    // 新增：按讲师ID查询课程
    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<ApiResponse<List<Course>>> getCoursesByInstructor(@PathVariable String instructorId) {
        List<Course> courses = courseService.findByInstructorId(instructorId);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    // 新增：查询有剩余容量的课程
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Course>>> getAvailableCourses() {
        List<Course> courses = courseService.findAvailableCourses();
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    // 新增：按标题关键字搜索课程
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Course>>> searchCourses(@RequestParam String keyword) {
        List<Course> courses = courseService.findByTitleKeyword(keyword);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    // 新增：多条件组合查询
    @GetMapping("/query")
    public ResponseEntity<ApiResponse<List<Course>>> queryCourses(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String instructorId,
            @RequestParam(required = false) String title) {
        List<Course> courses = courseService.findByMultipleCriteria(code, instructorId, title);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }
}