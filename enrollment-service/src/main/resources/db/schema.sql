-- 设置客户端字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS enrollment_db
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE enrollment_db;

-- 更新的 schema.sql
CREATE TABLE IF NOT EXISTS enrollments (
    id VARCHAR(36) PRIMARY KEY,
    student_id VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    status ENUM('ACTIVE', 'DROPPED', 'COMPLETED') DEFAULT 'ACTIVE',
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_course_student (course_id, student_id),
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    INDEX idx_course_status (course_id, status),
    INDEX idx_student_status (student_id, status)
);

CREATE TABLE IF NOT EXISTS students (
    id VARCHAR(36) PRIMARY KEY,
    student_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    major VARCHAR(100),
    grade INT,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 初始化学生数据
INSERT  INTO students (id, student_id, name, major, grade, email, created_at) VALUES
(UUID(), 'S2024001', '张三', '计算机科学与技术', 2024, 'zhangsan@example.com', CURRENT_TIMESTAMP),
(UUID(), 'S2024002', '李四', '软件工程', 2024, 'lisi@example.com', CURRENT_TIMESTAMP),
(UUID(), 'S2024003', '王五', '数学', 2024, 'wangwu@example.com', CURRENT_TIMESTAMP);

