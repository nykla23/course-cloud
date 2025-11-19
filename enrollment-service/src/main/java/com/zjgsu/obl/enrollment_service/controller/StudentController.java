package com.zjgsu.obl.enrollment_service.controller;

import com.zjgsu.obl.enrollment_service.common.ApiResponse;
import com.zjgsu.obl.enrollment_service.model.Student;
import com.zjgsu.obl.enrollment_service.service.EnrollmentService;
import com.zjgsu.obl.enrollment_service.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/students")
public class StudentController {
    @Autowired
    private StudentService studentService;

    @Autowired
    private EnrollmentService enrollmentService;

    // 查询所有学生
    @GetMapping
    public ResponseEntity<ApiResponse<List<Student>>> getAllStudents() {
        List<Student> students = studentService.findAll();
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    // 根据ID查询学生
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Student>> getStudentById(@PathVariable String id) {
        return studentService.findById(id)
                .map(student -> ResponseEntity.ok(ApiResponse.success(student)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "Student not found")));
    }

    // 创建学生
    @PostMapping
    public ResponseEntity<ApiResponse<Student>> createStudent(@RequestBody Student student) {
        try {
            Student createdStudent = studentService.createStudent(student);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Student created successfully", createdStudent));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    // 更新学生信息
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Student>> updateStudent(@PathVariable String id, @RequestBody Student student) {
        try {
            Student updatedStudent = studentService.updateStudent(id, student);
            if (updatedStudent != null) {
                return ResponseEntity.ok(ApiResponse.success("Student updated successfully", updatedStudent));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "Student not found"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        }
    }

    // 删除学生
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable String id) {
        // 检查学生是否存在
        if (studentService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Student not found"));
        }

        // 检查学生是否有选课记录
        if (enrollmentService.hasEnrollments(id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, "无法删除：该学生存在选课记录"));
        }

        boolean deleted = studentService.deleteStudent(id);
        if (deleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success("Student deleted successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Student not found"));
        }
    }

    // 新增：按专业查询学生
    @GetMapping("/major")
    public ResponseEntity<ApiResponse<List<Student>>> getStudentsByMajor(@RequestParam String major) {
        System.out.println("按专业查询: " + major);

        try {
            List<Student> students = studentService.findByMajor(major);
            System.out.println("找到 " + students.size() + " 个学生");
            return ResponseEntity.ok(ApiResponse.success(students));
        } catch (Exception e) {
            System.out.println("查询出错: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "查询失败: " + e.getMessage()));
        }
    }

    // 新增：按年级查询学生
    @GetMapping("/grade/{grade}")
    public ResponseEntity<ApiResponse<List<Student>>> getStudentsByGrade(@PathVariable Integer grade) {
        List<Student> students = studentService.findByGrade(grade);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    // 新增：按专业和年级查询学生
    @GetMapping("/major/{major}/grade/{grade}")
    public ResponseEntity<ApiResponse<List<Student>>> getStudentsByMajorAndGrade(
            @PathVariable String major, @PathVariable Integer grade) {
        List<Student> students = studentService.findByMajorAndGrade(major, grade);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    // 新增：多条件组合查询
    @GetMapping("/query")
    public ResponseEntity<ApiResponse<List<Student>>> queryStudents(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) Integer grade) {
        List<Student> students = studentService.findByMultipleCriteria(studentId, major, grade);
        return ResponseEntity.ok(ApiResponse.success(students));
    }
}
