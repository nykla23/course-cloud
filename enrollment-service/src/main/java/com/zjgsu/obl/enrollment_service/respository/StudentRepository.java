package com.zjgsu.obl.enrollment_service.respository;

import com.zjgsu.obl.enrollment_service.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    Optional<Student> findByStudentId(String studentId);

    Optional<Student> findByEmail(String email);

    boolean existsByStudentId(String studentId);

    boolean existsByEmail(String email);

    // 添加：按专业筛选学生
    List<Student> findByMajor(String major);

    // 添加：按年级筛选学生
    List<Student> findByGrade(Integer grade);
    // 添加：按专业和年级筛选学生
    List<Student> findByMajorAndGrade(String major, Integer grade);

    // 添加：按多个条件组合查询
    @Query("SELECT s FROM Student s WHERE " +
            "(:studentId IS NULL OR s.studentId = :studentId) AND " +
            "(:major IS NULL OR s.major = :major) AND " +
            "(:grade IS NULL OR s.grade = :grade)")
    List<Student> findByMultipleCriteria(@Param("studentId") String studentId,
                                         @Param("major") String major,
                                         @Param("grade") Integer grade);

}
