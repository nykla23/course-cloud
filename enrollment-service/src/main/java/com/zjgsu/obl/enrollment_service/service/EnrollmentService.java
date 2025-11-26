package com.zjgsu.obl.enrollment_service.service;

import com.zjgsu.obl.enrollment_service.exception.BusinessException;
import com.zjgsu.obl.enrollment_service.exception.ResourceNotFoundException;
import com.zjgsu.obl.enrollment_service.model.Enrollment;
import com.zjgsu.obl.enrollment_service.model.EnrollmentStatus;
import com.zjgsu.obl.enrollment_service.model.Student;
import com.zjgsu.obl.enrollment_service.respository.EnrollmentRepository;
import com.zjgsu.obl.enrollment_service.respository.StudentRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class EnrollmentService {
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentService.class);

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${catalog-service.url:http://localhost:8081}")
    private String catalogServiceUrl;

    /**
     * 获取所有选课记录
     */
    public List<Enrollment> findAll() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        enrollments.forEach(this::enrichWithCourseInfo);
        return enrollments;
    }

    /**
     * 根据ID查询选课记录
     */
    public Optional<Enrollment> findById(String id) {
        Optional<Enrollment> enrollment = enrollmentRepository.findById(id);
        enrollment.ifPresent(this::enrichWithCourseInfo);
        return enrollment;
    }

    /**
     * 学生选课
     */
//    @Transactional
//    public Enrollment enrollStudent(String courseId, String studentId) {
//        logger.info("开始选课: courseId={}, studentId={}", courseId, studentId);
//
//        // 1. 验证学生是否存在
//        Student student = studentRepository.findById(studentId)
//                .orElseThrow(() -> new ResourceNotFoundException("学生不存在: " + studentId));
//
//        // 2. 调用课程目录服务验证课程
//        Map<String, Object> courseInfo = getCourseInfoFromCatalogService(courseId);
//        Integer capacity = (Integer) courseInfo.get("capacity");
//        Integer enrolledCount = (Integer) courseInfo.get("enrolledCount");
//        if (enrolledCount == null) {
//            enrolledCount = 0;
//            logger.warn("课程 {} 的 enrolledCount 为 null，已设置为 0", courseId);
//        }
//
//        // 3. 检查课程容量
//        if (enrolledCount >= capacity) {
//            throw new BusinessException("课程容量已满，无法选课");
//        }
//
//        // 4. 检查重复选课
//        if (enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(courseId, studentId)) {
//            throw new BusinessException("该学生已经选过这门课程");
//        }
//
//        // 5. 创建选课记录
//        Enrollment enrollment = new Enrollment();
//        enrollment.setCourseId(courseId);
//        enrollment.setStudent(student);
//        enrollment.setStatus(EnrollmentStatus.ACTIVE);
//
//        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
//
//        // 6. 更新课程的已选人数
//        updateCourseEnrolledCount(courseId, enrolledCount + 1);
//
//        // 7. 丰富课程信息用于响应
//        enrichWithCourseInfo(savedEnrollment);
//
//        logger.info("选课成功: enrollmentId={}", savedEnrollment.getId());
//        return savedEnrollment;
//    }

    /**
     * 学生选课 - 完整修复版本
     */
    @Transactional(rollbackOn = Exception.class)
    public Enrollment enrollStudent(String courseId, String studentId) {
        logger.info("开始选课: courseId={}, studentId={}", courseId, studentId);

        try {
            // 1. 验证学生是否存在
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("学生不存在: " + studentId));

            // 2. 调用课程目录服务验证课程
            Map<String, Object> courseInfo = getCourseInfoFromCatalogService(courseId);
            Integer capacity = (Integer) courseInfo.get("capacity");
            Integer enrolledCount = (Integer) courseInfo.get("enrolledCount");
            if (enrolledCount == null) {
                enrolledCount = 0;
                logger.warn("课程 {} 的 enrolledCount 为 null，已设置为 0", courseId);
            }

            // 3. 检查课程容量
            if (enrolledCount >= capacity) {
                throw new BusinessException("课程容量已满，无法选课");
            }

            // 4. 检查重复选课
            if (enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(courseId, studentId)) {
                throw new BusinessException("该学生已经选过这门课程");
            }

            // 5. 创建选课记录
            Enrollment enrollment = new Enrollment();
            enrollment.setCourseId(courseId);
            enrollment.setStudent(student);
            enrollment.setStatus(EnrollmentStatus.ACTIVE);

            Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

            // 6. 更新课程的已选人数 - 关键修复！
            updateCourseEnrolledCount(courseId, enrolledCount + 1);

            // 7. 丰富课程信息用于响应
            enrichWithCourseInfo(savedEnrollment);

            logger.info("选课成功: enrollmentId={}", savedEnrollment.getId());
            return savedEnrollment;

        } catch (Exception e) {
            logger.error("选课失败: courseId={}, studentId={}, error={}",
                    courseId, studentId, e.getMessage());
            throw new BusinessException("选课失败: " + e.getMessage());
        }
    }

    /**
     * 学生退课
     */
    @Transactional
    public boolean dropEnrollment(String enrollmentId) {
        logger.info("开始退课: enrollmentId={}", enrollmentId);

        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(enrollmentId);
        if (enrollmentOpt.isEmpty()) {
            throw new ResourceNotFoundException("选课记录不存在: " + enrollmentId);
        }

        Enrollment enrollment = enrollmentOpt.get();

        // 1. 获取当前课程信息
        Map<String, Object> courseInfo = getCourseInfoFromCatalogService(enrollment.getCourseId());
        Integer currentEnrolled = (Integer) courseInfo.get("enrolledCount");

        // 2. 更新选课状态为退课
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);

        // 3. 更新课程的已选人数
        updateCourseEnrolledCount(enrollment.getCourseId(), currentEnrolled - 1);

        logger.info("退课成功: enrollmentId={}", enrollmentId);
        return true;
    }

    /**
     * 从catalog-service获取课程信息
     */
    private Map<String, Object> getCourseInfoFromCatalogService(String courseId) {
        String url = catalogServiceUrl + "/api/courses/" + courseId;
        logger.debug("调用课程服务: {}", url);

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("data")) {
                return (Map<String, Object>) response.get("data");
            } else {
                throw new ResourceNotFoundException("课程不存在: " + courseId);
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("课程不存在: " + courseId);
        } catch (Exception e) {
            logger.error("调用课程服务失败: {}", e.getMessage());
            throw new BusinessException("课程服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 更新课程的已选人数
     */
//    private void updateCourseEnrolledCount(String courseId, int newCount) {
//        String url = catalogServiceUrl + "/api/courses/" + courseId;
//        Map<String, Object> updateData = Map.of("enrolledCount", newCount);
//
//        logger.info("准备更新课程人数: courseId={}, newCount={}", courseId, newCount);
//        logger.info("调用URL: {}", url);
//        logger.info("请求数据: {}", updateData);
//
//        try {
//            restTemplate.put(url, updateData);
//            logger.info("更新课程人数成功: courseId={}, newCount={}", courseId, newCount);
//        } catch (Exception e) {
//            logger.error("更新课程人数失败: courseId={}, error={}", courseId, e.getMessage());
//            logger.error("异常详情:", e);
//        }
//    }

    /**
     * 更新课程的已选人数 - 修复版本
     */
    private void updateCourseEnrolledCount(String courseId, int newCount) {
        String url = catalogServiceUrl + "/api/courses/" + courseId;
        Map<String, Object> updateData = Map.of("enrolledCount", newCount);

        logger.info("准备更新课程人数: courseId={}, newCount={}", courseId, newCount);
        logger.info("调用URL: {}", url);
        logger.info("请求数据: {}", updateData);

        try {
            // 使用 PATCH 方法
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(updateData, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.PATCH, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("更新课程人数成功: courseId={}, newCount={}", courseId, newCount);
            } else {
                logger.error("更新课程人数失败: 状态码={}, 响应={}",
                        response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            logger.error("更新课程人数失败: courseId={}, error={}", courseId, e.getMessage());
            logger.error("异常详情:", e);
            // 重要：如果更新失败，抛出异常让事务回滚
            throw new BusinessException("更新课程人数失败: " + e.getMessage());
        }
    }

    /**
     * 为选课记录丰富课程信息
     */
    private void enrichWithCourseInfo(Enrollment enrollment) {
        try {
            Map<String, Object> courseInfo = getCourseInfoFromCatalogService(enrollment.getCourseId());
            enrollment.setCourseInfo(courseInfo);
        } catch (Exception e) {
            logger.warn("获取课程信息失败: courseId={}", enrollment.getCourseId());
        }
    }

    /**
     * 根据课程ID查询选课记录
     */
    public List<Enrollment> findByCourseId(String courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        enrollments.forEach(this::enrichWithCourseInfo);
        return enrollments;
    }

    /**
     * 根据学生ID查询选课记录
     */
    public List<Enrollment> findByStudentId(String studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        enrollments.forEach(this::enrichWithCourseInfo);
        return enrollments;
    }

    /**
     * 根据状态查询选课记录
     */
    public List<Enrollment> findByStatus(EnrollmentStatus status) {
        List<Enrollment> enrollments = enrollmentRepository.findByStatus(status);
        enrollments.forEach(this::enrichWithCourseInfo);
        return enrollments;
    }

    /**
     * 检查学生是否有选课记录
     */
    public boolean hasEnrollments(String studentId) {
        return enrollmentRepository.countByStudentId(studentId) > 0;
    }

    /**
     * 统计课程活跃选课人数
     */
    public long countActiveEnrollmentsByCourse(String courseId) {
        return enrollmentRepository.countActiveEnrollmentsByCourse(courseId);
    }

    /**
     * 检查学生是否已选某课程
     */
    public boolean hasActiveEnrollment(String studentId, String courseId) {
        return enrollmentRepository.existsByCourseIdAndStudentIdAndStatus(courseId, studentId);
    }

    /**
     * 多条件组合查询
     */
    public List<Enrollment> findByMultipleCriteria(String courseId, String studentId, EnrollmentStatus status) {
        List<Enrollment> enrollments = enrollmentRepository.findByMultipleCriteria(courseId, studentId, status);
        enrollments.forEach(this::enrichWithCourseInfo);
        return enrollments;
    }
}