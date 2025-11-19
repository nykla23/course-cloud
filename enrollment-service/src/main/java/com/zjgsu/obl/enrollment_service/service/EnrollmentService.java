package com.zjgsu.obl.enrollment_service.service;

import com.zjgsu.obl.enrollment_service.exception.ResourceNotFoundException;
import com.zjgsu.obl.enrollment_service.model.Enrollment;
import com.zjgsu.obl.enrollment_service.model.EnrollmentStatus;
import com.zjgsu.obl.enrollment_service.model.Student;
import com.zjgsu.obl.enrollment_service.respository.EnrollmentRepository;
import com.zjgsu.obl.enrollment_service.respository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EnrollmentService {
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Value("${catalog-service.url}")
    private String catalogServiceUrl;

    @Autowired
    private StudentRepository studentRepository;

    public List<Enrollment> findAll() {
        return enrollmentRepository.findAll();
    }

    public Optional<Enrollment> findById(String id) {
        return enrollmentRepository.findById(id);
    }

    /**
     * 创建选课记录 - 完整的事务管理
     * 确保选课操作和课程人数更新的原子性
     */
    @Transactional(rollbackOn = {Exception.class})
    public Enrollment createEnrollment(Enrollment enrollment) {
        // 参数校验
        if (enrollment == null) {
            throw new IllegalArgumentException("选课信息不能为空");
        }
        if (enrollment.getStudentId() == null || enrollment.getCourseId() == null) {
            throw new IllegalArgumentException("学生ID和课程ID不能为空");
        }

        // 根据 studentId 查找 Student 实体
        Optional<Student> student = studentRepository.findByStudentId(enrollment.getStudentId());
        if (student.isEmpty()) {
            throw new ResourceNotFoundException("学生不存在，学号: " + enrollment.getStudentId());
        }

        // 根据 courseId 查找 Course 实体
        Optional<Course> course = courseRepository.findById(enrollment.getCourseId());
        if (course.isEmpty()) {
            throw new ResourceNotFoundException("课程不存在，课程ID: " + enrollment.getCourseId());
        }

        Course courseObj = course.get();
        Student studentObj = student.get();

        // 检查是否已经选过该课程
        Optional<Enrollment> existingEnrollment = enrollmentRepository.findByCourseAndStudent(courseObj, studentObj);
        if (existingEnrollment.isPresent()) {
            Enrollment existing = existingEnrollment.get();
            if (existing.getStatus() == EnrollmentStatus.ACTIVE) {
                throw new IllegalArgumentException("该学生已经选过这门课程");
            } else {
                // 如果是退课状态，可以重新激活
                existing.setStatus(EnrollmentStatus.ACTIVE);
                Enrollment updatedEnrollment = enrollmentRepository.save(existing);

                // 更新课程已选人数
                updateCourseEnrollmentCount(courseObj);
                return updatedEnrollment;
            }
        }

        // 检查课程容量
        long currentEnrollments = enrollmentRepository.countActiveEnrollmentsByCourse(courseObj);
        if (currentEnrollments >= courseObj.getCapacity()) {
            throw new IllegalArgumentException("课程容量已满，无法选课");
        }

        // 设置对象关联和默认状态
        enrollment.setStudent(studentObj);
        enrollment.setCourse(courseObj);
        if (enrollment.getStatus() == null) {
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
        }

        // 保存选课记录
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // 更新课程的已选人数
        updateCourseEnrollmentCount(courseObj);

        return savedEnrollment;
    }

    /**
     * 删除选课记录（退课） - 完整的事务管理
     */
    @Transactional(rollbackOn = {Exception.class})
    public boolean deleteEnrollment(String id) {
        Optional<Enrollment> enrollment = enrollmentRepository.findById(id);
        if (enrollment.isPresent()) {
            Enrollment enrollmentObj = enrollment.get();
            Course course = enrollmentObj.getCourse();

            // 软删除：将状态改为 DROPPED
            enrollmentObj.setStatus(EnrollmentStatus.DROPPED);
            enrollmentRepository.save(enrollmentObj);

            // 更新课程的已选人数
            updateCourseEnrollmentCount(course);

            return true;
        } else {
            return false;
        }
    }

    /**
     * 硬删除选课记录 - 仅用于管理目的
     */
    @Transactional(rollbackOn = {Exception.class})
    public boolean hardDeleteEnrollment(String id) {
        Optional<Enrollment> enrollment = enrollmentRepository.findById(id);
        if (enrollment.isPresent()) {
            Enrollment enrollmentObj = enrollment.get();
            Course course = enrollmentObj.getCourse();

            // 硬删除：直接从数据库删除记录
            enrollmentRepository.deleteById(id);

            // 更新课程的已选人数
            updateCourseEnrollmentCount(course);

            return true;
        } else {
            return false;
        }
    }

    /**
     * 更新课程已选人数 - 私有方法，确保在事务内执行
     */
    private void updateCourseEnrollmentCount(Course course) {
        long currentEnrollments = enrollmentRepository.countActiveEnrollmentsByCourse(course);
        course.setEnrolled((int) currentEnrollments);
        courseRepository.save(course);
    }

    /**
     * 完成课程 - 将选课状态改为 COMPLETED
     */
    @Transactional(rollbackOn = {Exception.class})
    public boolean completeEnrollment(String id) {
        Optional<Enrollment> enrollment = enrollmentRepository.findById(id);
        if (enrollment.isPresent()) {
            Enrollment enrollmentObj = enrollment.get();
            enrollmentObj.setStatus(EnrollmentStatus.COMPLETED);
            enrollmentRepository.save(enrollmentObj);
            return true;
        }
        return false;
    }

    // 根据课程ID查询选课记录
    public List<Enrollment> findByCourseId(String courseCode) {
        return enrollmentRepository.findByCourseCode(courseCode);
    }

    // 根据学生ID查询选课记录
    public List<Enrollment> findByStudentId(String studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    // 检查学生是否有选课记录
    public boolean hasEnrollments(String studentId) {
        return enrollmentRepository.countByStudentId(studentId) > 0;
    }

    // 查询指定状态的选课记录
    public List<Enrollment> findByStatus(EnrollmentStatus status) {
        return enrollmentRepository.findByStatus(status);
    }

    // 统计课程活跃选课人数
    public long countActiveEnrollmentsByCourse(String courseCode) {
        Optional<Course> course = courseRepository.findByCourseCode(courseCode);
        return course.map(c -> enrollmentRepository.countActiveEnrollmentsByCourse(c)).orElse(0L);
    }

    // 检查学生是否已选某课程（活跃状态）
    public boolean hasActiveEnrollment(String studentId, String courseCode) {
        Optional<Student> student = studentRepository.findByStudentId(studentId);
        Optional<Course> course = courseRepository.findByCourseCode(courseCode);

        if (student.isPresent() && course.isPresent()) {
            return enrollmentRepository.existsByStudentAndCourseAndActive(student.get(), course.get());
        }
        return false;
    }

    // 多条件组合查询
    public List<Enrollment> findByMultipleCriteria(String courseCode, String studentId, EnrollmentStatus status) {
        return enrollmentRepository.findByMultipleCriteria(courseCode, studentId, status);
    }

    /**
     * 批量选课 - 为多个学生同时选课
     */
    @Transactional(rollbackOn = {Exception.class})
    public List<Enrollment> batchCreateEnrollments(List<Enrollment> enrollments) {
        if (enrollments == null || enrollments.isEmpty()) {
            throw new IllegalArgumentException("选课列表不能为空");
        }

        List<Enrollment> createdEnrollments = new java.util.ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            try {
                Enrollment created = createEnrollment(enrollment);
                createdEnrollments.add(created);
            } catch (Exception e) {
                // 如果单个选课失败，记录日志但继续处理其他选课
                // 由于在事务中，所有操作都会在最终异常时回滚
                System.err.println("选课失败: " + e.getMessage());
                throw new RuntimeException("批量选课过程中发生错误: " + e.getMessage(), e);
            }
        }
        return createdEnrollments;
    }

    /**
     * 获取课程的活跃选课列表
     */
    public List<Enrollment> findActiveEnrollmentsByCourse(String courseCode) {
        Optional<Course> course = courseRepository.findByCourseCode(courseCode);
        if (course.isPresent()) {
            return enrollmentRepository.findByCourseAndStatus(course.get(), EnrollmentStatus.ACTIVE);
        }
        return List.of();
    }
}
