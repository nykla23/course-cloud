package com.zjgsu.obl.enrollment_service.service;

import com.zjgsu.obl.enrollment_service.model.Student;
import com.zjgsu.obl.enrollment_service.respository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Optional<Student> findById(String id) {
        return studentRepository.findById(id);
    }

    public Student createStudent(Student student) {
        if(!isValidEmail(student.getEmail())){
            throw new IllegalArgumentException("Invalid email format");
        }

        if (studentRepository.existsByStudentId(student.getStudentId())) {
            throw new IllegalArgumentException("学号已存在");
        }

        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在");
        }

        return studentRepository.save(student);
    }

    public Student updateStudent(String id, Student student) {
        Student existStudent = studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("学生不存在"));

        if(!isValidEmail(student.getEmail())){
            throw new IllegalArgumentException("邮箱格式不正确");
        }

        Optional<Student> studentByStudentId = studentRepository.findByStudentId(student.getStudentId());
        if (studentByStudentId.isPresent() &&!studentByStudentId.get().getId().equals(id)){
            throw new IllegalArgumentException("学号已存在");
        }

        Optional<Student> studentByEmail = studentRepository.findByEmail(student.getEmail());
        if (studentByEmail.isPresent() &&!studentByEmail.get().getId().equals(id)){
            throw new IllegalArgumentException("邮箱已被其他学生使用");
        }

        existStudent.setStudentId(student.getStudentId());
        existStudent.setName(student.getName());
        existStudent.setMajor(student.getMajor());
        existStudent.setGrade(student.getGrade());
        existStudent.setEmail(student.getEmail());

        return studentRepository.save(existStudent);
    }

    public boolean deleteStudent(String id) {
        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private boolean isValidEmail(String email){
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public List<Student> findByMajor(String major) {
        System.out.println("Service: 按专业查询 - " + major);
        List<Student> students = studentRepository.findByMajor(major);
        System.out.println("Repository 返回: " + students.size() + " 个学生");
        return students;
    }

    public List<Student> findByGrade(Integer grade) {
        return studentRepository.findByGrade(grade);
    }

    public List<Student> findByMajorAndGrade(String major, Integer grade) {
        return studentRepository.findByMajorAndGrade(major, grade);
    }

    public List<Student> findByMultipleCriteria(String studentId, String major, Integer grade) {
        System.out.println("多条件查询 - studentId: " + studentId + ", major: " + major + ", grade: " + grade);

        List<Student> students = studentRepository.findByMultipleCriteria(studentId, major, grade);
        System.out.println("查询结果数量: " + students.size());

        // 打印所有学生信息用于调试
        List<Student> allStudents = studentRepository.findAll();
        System.out.println("数据库中学生总数: " + allStudents.size());
        for (Student s : allStudents) {
            System.out.println("学生: " + s.getStudentId() + ", " + s.getName() + ", " + s.getMajor() + ", " + s.getGrade());
        }

        return students;
    }

}
