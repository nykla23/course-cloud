package com.zjgsu.obl.enrollment_service.respository;

import com.zjgsu.obl.enrollment_service.model.Enrollment;
import com.zjgsu.obl.enrollment_service.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

    // 按课程ID查询选课记录
    List<Enrollment> findByCourseId(String courseId);

    // 按学生ID查询选课记录
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId")
    List<Enrollment> findByStudentId(@Param("studentId") String studentId);

    // 按状态查询选课记录
    List<Enrollment> findByStatus(EnrollmentStatus status);

    // 按课程ID和状态查询选课记录
    List<Enrollment> findByCourseIdAndStatus(String courseId, EnrollmentStatus status);

    // 检查是否存在活跃的选课记录
    @Query("SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.courseId = :courseId AND e.student.id = :studentId AND e.status = 'ACTIVE'")
    boolean existsByCourseIdAndStudentIdAndStatus(@Param("courseId") String courseId,
                                                  @Param("studentId") String studentId);

    // 统计课程的活跃选课人数
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.courseId = :courseId AND e.status = 'ACTIVE'")
    long countActiveEnrollmentsByCourse(@Param("courseId") String courseId);

    // 统计学生的选课数量
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :studentId")
    long countByStudentId(@Param("studentId") String studentId);

    // 按学生学号查询选课记录
    @Query("SELECT e FROM Enrollment e WHERE e.student.studentId = :studentId")
    List<Enrollment> findByStudentStudentId(@Param("studentId") String studentId);

    // 多条件组合查询
    @Query("SELECT e FROM Enrollment e WHERE " +
            "(:courseId IS NULL OR e.courseId = :courseId) AND " +
            "(:studentId IS NULL OR e.student.studentId = :studentId) AND " +
            "(:status IS NULL OR e.status = :status)")
    List<Enrollment> findByMultipleCriteria(@Param("courseId") String courseId,
                                            @Param("studentId") String studentId,
                                            @Param("status") EnrollmentStatus status);
}