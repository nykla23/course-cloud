package com.zjgsu.obl.enrollment_service.respository;

import com.zjgsu.obl.enrollment_service.model.Enrollment;
import com.zjgsu.obl.enrollment_service.model.EnrollmentStatus;
import com.zjgsu.obl.enrollment_service.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

    // 按课程查询
    List<Enrollment> findByCourse(Course course);

    // 按学生查询
    List<Enrollment> findByStudent(Student student);

    // 按状态查询
    List<Enrollment> findByStatus(EnrollmentStatus status);

    // 添加：按课程和状态组合查询
    List<Enrollment> findByCourseAndStatus(Course course, EnrollmentStatus status);

    // 按课程和学生组合查询
    Optional<Enrollment> findByCourseAndStudent(Course course, Student student);

    // 统计课程的活跃选课人数
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course = :course AND e.status = 'ACTIVE'")
    long countActiveEnrollmentsByCourse(@Param("course") Course course);

    // 检查学生是否已选某课程
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Enrollment e WHERE e.student = :student AND e.course = :course AND e.status = 'ACTIVE'")
    boolean existsByStudentAndCourseAndActive(@Param("student") Student student, @Param("course") Course course);

    // 按学生ID和课程代码查询（用于API兼容）
    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId AND e.student.id = :studentId")
    Optional<Enrollment> findByCourseIdAndStudentId(@Param("courseId") String courseId, @Param("studentId") String studentId);

    // 按课程代码查询选课记录
    @Query("SELECT e FROM Enrollment e WHERE e.course.courseCode = :courseCode")
    List<Enrollment> findByCourseCode(@Param("courseCode") String courseCode);

    // 按学生学号查询选课记录
    @Query("SELECT e FROM Enrollment e WHERE e.student.studentId = :studentId")
    List<Enrollment> findByStudentId(@Param("studentId") String studentId);

    // 统计学生选课数量
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.studentId = :studentId")
    long countByStudentId(@Param("studentId") String studentId);

    // 添加：按多个条件组合查询
    @Query("SELECT e FROM Enrollment e WHERE " +
            "(:courseCode IS NULL OR e.course.courseCode = :courseCode) AND " +
            "(:studentId IS NULL OR e.student.studentId = :studentId) AND " +
            "(:status IS NULL OR e.status = :status)")
    List<Enrollment> findByMultipleCriteria(@Param("courseCode") String courseCode,
                                            @Param("studentId") String studentId,
                                            @Param("status") EnrollmentStatus status);

}
