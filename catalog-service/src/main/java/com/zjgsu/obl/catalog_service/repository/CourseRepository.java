package com.zjgsu.obl.catalog_service.repository;

import com.zjgsu.obl.catalog_service.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, String> {

    // 按课程代码查询课程
    Optional<Course> findByCourseCode(String courseCode);

    // 按讲师ID查询
    @Query("SELECT c FROM Course c WHERE c.instructor.id = :instructorId")
    List<Course> findByInstructorId(@Param("instructorId") String instructorId);

    // 按标题关键字模糊查询
    List<Course> findByTitleContainingIgnoreCase(String keyword);

    // 查询有剩余容量的课程
    @Query("SELECT c FROM Course c WHERE c.capacity > COALESCE(c.enrolledCount, 0)")
    List<Course> findAvailableCourses();

    // 检查课程代码是否存在
    boolean existsByCourseCode(String courseCode);

    // 添加：按多个条件组合查询
    @Query("SELECT c FROM Course c WHERE " +
            "(:courseCode IS NULL OR c.courseCode = :courseCode) AND " +
            "(:instructorId IS NULL OR c.instructor.id = :instructorId) AND " +
            "(:title IS NULL OR c.title LIKE %:title%)")
    List<Course> findByMultipleCriteria(@Param("courseCode") String courseCode,
                                        @Param("instructorId") String instructorId,
                                        @Param("title") String title);

}
