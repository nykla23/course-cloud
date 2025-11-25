package com.zjgsu.obl.catalog_service.service;

import com.zjgsu.obl.catalog_service.model.Course;
import com.zjgsu.obl.catalog_service.model.Instructor;
import com.zjgsu.obl.catalog_service.model.ScheduleSlot;
import com.zjgsu.obl.catalog_service.repository.CourseRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Optional<Course> getById(String id) {
        return courseRepository.findById(id);
    }

    public Optional<Course> getByCode(String code) {
        return courseRepository.findByCourseCode(code);
    }

    public Course createCourse(Course course) {
        // 添加必填字段验证
        if (course.getCode() == null || course.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("课程代码不能为空");
        }
        if (course.getTitle() == null || course.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("课程名称不能为空");
        }
        if (course.getInstructor() == null) {
            throw new IllegalArgumentException("教师信息不能为空");
        }
        if (course.getSchedule() == null) {
            throw new IllegalArgumentException("课程安排不能为空");
        }
        if (course.getCapacity() == null || course.getCapacity() <= 0) {
            throw new IllegalArgumentException("课程容量必须大于0");
        }

        if (courseRepository.existsByCourseCode(course.getCode())){
            throw new IllegalArgumentException("课程代码已存在");
        }

        return courseRepository.save(course);
    }

    public Course updateCourse(String id, Course courseUpdate) {

        // 检查课程是否存在

        Course existingCourse = courseRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("课程不存在"));

        if (courseUpdate.getCode() != null && !courseUpdate.getCode().equals(existingCourse.getCode())) {
            if (courseRepository.existsByCourseCode(courseUpdate.getCode())) {
                throw new IllegalArgumentException("课程代码已存在");
            }
            existingCourse.setCode(courseUpdate.getCode());
        }

        if (courseUpdate.getTitle() != null) {
            existingCourse.setTitle(courseUpdate.getTitle());
        }

        if (courseUpdate.getInstructor() != null) {
            Instructor existingInstructor = existingCourse.getInstructor();
            Instructor updatedInstructor = courseUpdate.getInstructor();

            if (updatedInstructor.getId() != null){
                existingInstructor.setId(updatedInstructor.getId());
            }

            if (updatedInstructor.getName() != null){
                existingInstructor.setName(updatedInstructor.getName());
            }

            if (updatedInstructor.getEmail() != null){
                existingInstructor.setEmail(updatedInstructor.getEmail());
            }
        }

        if (courseUpdate.getSchedule() != null) {
            ScheduleSlot existingSchedule = existingCourse.getSchedule();

            ScheduleSlot updatedSchedule = courseUpdate.getSchedule();

            if (updatedSchedule.getDayOfWeek() != null){
                existingSchedule.setDayOfWeek(updatedSchedule.getDayOfWeek());
            }

            if (updatedSchedule.getStartTime() != null){
                existingSchedule.setStartTime(updatedSchedule.getStartTime());
            }

            if (updatedSchedule.getEndTime() != null){
                existingSchedule.setEndTime(updatedSchedule.getEndTime());
            }

            if (updatedSchedule.getExpectedAttendance() != null){
                existingSchedule.setExpectedAttendance(updatedSchedule.getExpectedAttendance());
            }
        }

        if (courseUpdate.getCapacity() != null && courseUpdate.getCapacity() > 0) {
            existingCourse.setCapacity(courseUpdate.getCapacity());
        }

        return courseRepository.save(existingCourse);
    }

    public boolean deleteCourse(String id){
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public List<Course> findAvailableCourses() {
        return courseRepository.findAvailableCourses();
    }

    public List<Course> findByTitleKeyword(String keyword) {
        return courseRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public List<Course> findByInstructorId(String instructorId) {
        return courseRepository.findByInstructorId(instructorId);
    }

    public List<Course> findByMultipleCriteria(String code, String instructorId,String title) {
        return courseRepository.findByMultipleCriteria(code, instructorId, title);
    }
}
