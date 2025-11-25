-- 设置客户端字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS catalog_db
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE catalog_db;

-- 更新的 schema.sql
CREATE TABLE IF NOT EXISTS courses (
    id VARCHAR(36) PRIMARY KEY,
    course_code VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    capacity INT NOT NULL DEFAULT 0,
    enrolled_count INT NOT NULL DEFAULT 0,
    instructor_id VARCHAR(100),
    instructor_name VARCHAR(100),
    instructor_email VARCHAR(100),
    schedule_day VARCHAR(20),
    schedule_start_time VARCHAR(10),
    schedule_end_time VARCHAR(10),
    expected_attendance INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT  INTO courses (id, course_code, title, capacity, enrolled_count, instructor_id, instructor_name, instructor_email, schedule_day, schedule_start_time, schedule_end_time, expected_attendance) VALUES
(UUID(), 'CS101', '计算机科学导论', 60, 0, 'T001', '张教授', 'zhang@example.edu.cn', 'MONDAY', '08:00', '10:00', 50),
(UUID(), 'MATH201', '高等数学', 50, 0, 'T002', '李教授', 'li@example.edu.cn', 'TUESDAY', '10:00', '12:00', 40),
(UUID(), 'ENG101', '大学英语', 80, 0, 'T003', '王老师', 'wang@example.edu.cn', 'WEDNESDAY', '14:00', '16:00', 60);

