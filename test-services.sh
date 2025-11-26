#!/bin/bash

# 校园选课系统微服务测试脚本
echo "=== 校园选课系统微服务测试 ==="
echo "开始时间: $(date)"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 函数：打印带颜色的消息
print_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
print_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 等待服务启动
wait_for_service() {
    local service_url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1

    print_info "等待 $service_name 启动..."

    while [ $attempt -le $max_attempts ]; do
        if curl -s $service_url > /dev/null; then
            print_info "$service_name 已启动"
            return 0
        fi
        print_warn "尝试 $attempt/$max_attempts: $service_name 尚未就绪，等待 2 秒..."
        sleep 2
        attempt=$((attempt + 1))
    done

    print_error "$service_name 启动超时"
    return 1
}

# 检查 Docker 服务状态
check_docker_services() {
    print_info "检查 Docker 服务状态..."
    docker-compose ps
}

# 测试课程目录服务
test_catalog_service() {
    print_info "=== 测试课程目录服务 ==="

    # 获取所有课程
    print_info "1. 获取所有课程"
    curl -s http://localhost:8081/api/courses | jq '.'

    # 创建新课程
    print_info "2. 创建新课程"
    CREATE_RESPONSE=$(curl -s -X POST http://localhost:8081/api/courses \
        -H "Content-Type: application/json" \
        -d '{
            "courseCode": "CS301",
            "title": "分布式系统",
            "instructor": {
                "id": "T003",
                "name": "王教授",
                "email": "wang@example.edu.cn"
            },
            "schedule": {
                "dayOfWeek": "WEDNESDAY",
                "startTime": "14:00",
                "endTime": "16:00",
                "expectedAttendance": 40
            },
            "capacity": 50,
            "enrolledCount": 0
        }')
    echo $CREATE_RESPONSE | jq '.'

    # 获取课程ID
    COURSE_ID=$(echo $CREATE_RESPONSE | jq -r '.data.id')
    print_info "新创建课程ID: $COURSE_ID"

    # 查询单个课程
    print_info "3. 查询单个课程"
    curl -s http://localhost:8081/api/courses/$COURSE_ID | jq '.'
}

# 测试选课服务
test_enrollment_service() {
    print_info "=== 测试选课服务 ==="

    # 获取所有学生
    print_info "1. 获取所有学生"
    curl -s http://localhost:8082/api/students | jq '.'

    # 创建新学生
    print_info "2. 创建新学生"
    STUDENT_RESPONSE=$(curl -s -X POST http://localhost:8082/api/students \
        -H "Content-Type: application/json" \
        -d '{
            "studentId": "2024003",
            "name": "王五",
            "major": "人工智能",
            "grade": 2024,
            "email": "wangwu@example.edu.cn"
        }')
    echo $STUDENT_RESPONSE | jq '.'

    # 获取学生ID
    STUDENT_ID=$(echo $STUDENT_RESPONSE | jq -r '.data.id')
    print_info "新创建学生ID: $STUDENT_ID"

    # 学生选课（服务间调用测试）
    print_info "3. 学生选课（测试服务间调用）"
    ENROLLMENT_RESPONSE=$(curl -s -X POST http://localhost:8082/api/enrollments \
        -H "Content-Type: application/json" \
        -d '{
            "courseId": "'$COURSE_ID'",
            "studentId": "'$STUDENT_ID'"
        }')
    echo $ENROLLMENT_RESPONSE | jq '.'

    # 查询选课记录
    print_info "4. 查询所有选课记录"
    curl -s http://localhost:8082/api/enrollments | jq '.'

    # 按学生查询选课
    print_info "5. 按学生查询选课记录"
    curl -s http://localhost:8082/api/enrollments/student/$STUDENT_ID | jq '.'

    # 按课程查询选课
    print_info "6. 按课程查询选课记录"
    curl -s http://localhost:8082/api/enrollments/course/$COURSE_ID | jq '.'
}

# 测试错误处理
test_error_cases() {
    print_info "=== 测试错误处理 ==="

    # 测试课程不存在的情况
    print_info "1. 测试课程不存在的情况"
    curl -s -X POST http://localhost:8082/api/enrollments \
        -H "Content-Type: application/json" \
        -d '{
            "courseId": "non-existent-course",
            "studentId": "'$STUDENT_ID'"
        }' | jq '.'

    # 测试学生不存在的情况
    print_info "2. 测试学生不存在的情况"
    curl -s -X POST http://localhost:8082/api/enrollments \
        -H "Content-Type: application/json" \
        -d '{
            "courseId": "'$COURSE_ID'",
            "studentId": "non-existent-student"
        }' | jq '.'

    # 测试重复选课
    print_info "3. 测试重复选课"
    curl -s -X POST http://localhost:8082/api/enrollments \
        -H "Content-Type: application/json" \
        -d '{
            "courseId": "'$COURSE_ID'",
            "studentId": "'$STUDENT_ID'"
        }' | jq '.'
}

# 性能测试
performance_test() {
    print_info "=== 性能测试 ==="

    # 测试课程目录服务响应时间
    print_info "测试课程目录服务响应时间..."
    time curl -s http://localhost:8081/api/courses > /dev/null

    # 测试选课服务响应时间
    print_info "测试选课服务响应时间..."
    time curl -s http://localhost:8082/api/students > /dev/null
}

# 主测试流程
main() {
    print_info "开始微服务测试..."

    # 等待服务启动
    wait_for_service "http://localhost:8081/api/courses" "catalog-service"
    wait_for_service "http://localhost:8082/api/students" "enrollment-service"

    # 检查 Docker 服务
    check_docker_services

    # 执行测试
    test_catalog_service
    test_enrollment_service
    test_error_cases
    performance_test

    print_info "=== 测试完成 ==="
    print_info "结束时间: $(date)"

    # 输出访问信息
    echo ""
    print_info "服务访问地址:"
    echo "  - 课程目录服务: http://localhost:8081/api/courses"
    echo "  - 选课服务(学生): http://localhost:8082/api/students"
    echo "  - 选课服务(选课): http://localhost:8082/api/enrollments"
    echo "  - 课程数据库: localhost:3307 (用户: catalog_user)"
    echo "  - 选课数据库: localhost:3308 (用户: enrollment_user)"
}

# 执行主函数
main "$@"