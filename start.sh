#!/bin/bash

# =============================================================================
# QQBot 可视化回复规则项目 - 启动脚本
# 版本: 1.0.0
# 描述: 用于管理 QQBot 后端服务的启动、停止、重启和状态查看
# 作者: Auto Generated
# =============================================================================

# --------------------------- 配置变量 ---------------------------
# JVM 参数配置
JAVA_OPTS="-Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dserver.address=0.0.0.0"
JAVA_OPTS="$JAVA_OPTS -Dspring.config.location=classpath:/,file:./,file:./config/"

# 应用配置
PID_FILE="bot.pid"          # 进程ID文件
LOG_FILE="bot.log"          # 日志文件
JAR_FILE="bot.jar"          # 应用JAR文件名
PORT=8080                   # 应用端口

# --------------------------- 颜色定义 ---------------------------
RED="\033[31m"
GREEN="\033[32m"
YELLOW="\033[33m"
BLUE="\033[34m"
PURPLE="\033[35m"
CYAN="\033[36m"
RESET="\033[0m"

# --------------------------- 工具函数 ---------------------------

# 彩色输出函数
echo_color() {
    local color="$1"
    local message="$2"
    echo -e "${color}${message}${RESET}"
}

# 成功信息
echo_success() {
    echo_color "$GREEN" "✓ $1"
}

# 错误信息
echo_error() {
    echo_color "$RED" "✗ $1"
}

# 警告信息
echo_warning() {
    echo_color "$YELLOW" "⚠ $1"
}

# 信息提示
echo_info() {
    echo_color "$BLUE" "ℹ $1"
}

# 标题信息
echo_title() {
    echo_color "$PURPLE" "$1"
}

# 分隔线
echo_separator() {
    echo_color "$CYAN" "------------------------------------------------------------"
}

# 获取进程ID
get_pid() {
    if [ -f "$PID_FILE" ]; then
        cat "$PID_FILE"
    fi
}

# 检查进程是否运行
is_running() {
    local pid=$(get_pid)
    if [ ! -z "$pid" ] && [ -d "/proc/$pid" ]; then
        return 0
    else
        return 1
    fi
}

# 检查并安装 Java 17
ensure_java17_installed() {
    local java_version
    echo_info "检查 Java 17 环境..."
    
    if command -v java &> /dev/null; then
        java_version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
        if [[ $java_version =~ ^17\. ]]; then
            echo_success "Java 17 已安装 (版本: $java_version)"
            return 0
        else
            echo_warning "检测到 Java 版本: $java_version，需要安装 Java 17"
        fi
    else
        echo_warning "Java 未安装，正在安装 Java 17..."
    fi

    # 尝试安装 OpenJDK 17
    if command -v apt-get &> /dev/null; then
        sudo apt-get update &> /dev/null
        sudo apt-get install -y openjdk-17-jre-headless &> /dev/null
    elif command -v yum &> /dev/null; then
        sudo yum install -y java-17-openjdk-headless &> /dev/null
    elif command -v dnf &> /dev/null; then
        sudo dnf install -y java-17-openjdk-headless &> /dev/null
    else
        echo_error "未找到包管理器，无法自动安装 Java 17"
        exit 1
    fi

    # 验证安装
    if ! command -v java &> /dev/null; then
        echo_error "Java 17 安装失败"
        exit 1
    fi

    java_version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
    if [[ $java_version =~ ^17\. ]]; then
        echo_success "Java 17 安装成功 (版本: $java_version)"
    else
        echo_error "安装的 Java 版本不是 17，当前版本: $java_version"
        exit 1
    fi
}

# 检查并安装 npm 和 nodejs
ensure_npm_installed() {
    echo_info "检查 Node.js 和 npm 环境..."
    
    # 检查 Node.js 是否已安装
    if ! node -v &> /dev/null; then
        echo_warning "Node.js 未安装，正在安装..."
        if command -v apt-get &> /dev/null; then
            sudo apt-get update &> /dev/null
            sudo apt-get install -y nodejs npm &> /dev/null
        elif command -v yum &> /dev/null; then
            sudo yum install -y nodejs npm &> /dev/null
        elif command -v dnf &> /dev/null; then
            sudo dnf install -y nodejs npm &> /dev/null
        else
            echo_error "未找到包管理器，无法自动安装 nodejs 和 npm"
            exit 1
        fi
    else
        echo_success "Node.js 已安装 (版本: $(node -v))"
    fi

    # 验证 npm 安装
    if ! command -v npm &> /dev/null; then
        echo_warning "npm 未安装，正在安装..."
        if command -v apt-get &> /dev/null; then
            sudo apt-get install -y npm &> /dev/null
        elif command -v yum &> /dev/null; then
            sudo yum install -y npm &> /dev/null
        elif command -v dnf &> /dev/null; then
            sudo dnf install -y npm &> /dev/null
        else
            echo_error "未找到包管理器，无法自动安装 npm"
            exit 1
        fi
    else
        echo_success "npm 已安装 (版本: $(npm -v))"
    fi

    # 验证安装
    if ! command -v node &> /dev/null || ! command -v npm &> /dev/null; then
        echo_error "nodejs 或 npm 安装失败"
        exit 1
    fi
    echo_success "Node.js 和 npm 环境检查完成"
}

# 检查并安装 Playwright
ensure_playwright_installed() {
    ensure_npm_installed
    
    echo_info "检查 Playwright 环境..."
    
    # 检查 Playwright 是否已安装
    if ! npx playwright --version &> /dev/null; then
        echo_warning "Playwright 未安装或安装不完整，正在安装..."
        npm init playwright@latest --yes &> /dev/null
        echo_success "Playwright 安装成功"
    else
        echo_success "Playwright 已安装"
    fi
}

# 检查并安装 lsof
ensure_lsof_installed() {
    echo_info "检查 lsof 工具..."
    
    if ! command -v lsof &> /dev/null; then
        echo_warning "lsof 命令未找到，正在安装..."
        if command -v apt-get &> /dev/null; then
            sudo apt-get update &> /dev/null
            sudo apt-get install -y lsof &> /dev/null
        elif command -v yum &> /dev/null; then
            sudo yum install -y lsof &> /dev/null
        elif command -v dnf &> /dev/null; then
            sudo dnf install -y lsof &> /dev/null
        else
            echo_error "未找到包管理器，无法自动安装 lsof"
            exit 1
        fi

        # 验证安装
        if ! command -v lsof &> /dev/null; then
            echo_error "lsof 安装失败"
            exit 1
        fi
        echo_success "lsof 安装成功"
    else
        echo_success "lsof 已安装"
    fi
}

# 杀死占用端口的进程
kill_port_process() {
    ensure_lsof_installed
    
    local port=$1
    local pid=$(lsof -t -i:$port)
    if [ ! -z "$pid" ]; then
        echo_warning "发现端口 $port 被进程 $pid 占用，正在终止..."
        kill -9 $pid &> /dev/null
        echo_success "端口 $port 的占用进程已终止"
    else
        echo_success "端口 $port 未被占用"
    fi
}

# --------------------------- 主程序 ---------------------------

# 显示启动信息
echo_separator
echo_title "QQBot 可视化回复规则项目启动脚本"
echo_separator

case "$1" in
    start)
        echo_title "启动应用服务"
        echo_separator
        
        # 启动前检查环境依赖
        ensure_java17_installed
        ensure_playwright_installed
        echo_success "环境依赖检查完成"
        
        echo_separator
        
        if is_running; then
            echo_warning "应用已在运行中 (PID: $(get_pid))"
        else
            echo_info "正在清理端口 $PORT..."
            kill_port_process $PORT
            
            echo_separator
            
            echo_info "正在启动应用..."
            # 检查JAR文件是否存在
            if [ ! -f "$JAR_FILE" ]; then
                echo_error "未找到JAR文件 $JAR_FILE"
                exit 1
            fi
            
            # 启动应用
            nohup java $JAVA_OPTS -jar "$JAR_FILE" > "$LOG_FILE" 2>&1 &
            echo $! > "$PID_FILE"
            
            echo_separator
            echo_success "应用已成功启动！"
            echo_info "PID: $(cat $PID_FILE)"
            echo_info "端口: $PORT"
            echo_info "日志: $LOG_FILE"
            echo_info "访问地址: http://$(hostname -I | awk '{print $1}'):$PORT"
            echo_separator
        fi
        ;;
        
    stop)
        echo_title "停止应用服务"
        echo_separator
        
        if is_running; then
            pid=$(get_pid)
            kill "$pid" &> /dev/null
            rm -f "$PID_FILE"
            echo_success "应用已成功停止 (PID: $pid)"
        else
            echo_warning "应用未运行"
        fi
        
        echo_separator
        
        # 清理端口占用
        echo_info "清理端口 $PORT 占用..."
        kill_port_process $PORT
        echo_separator
        ;;
        
    restart)
        echo_title "重启应用服务"
        echo_separator
        $0 stop
        echo_info "等待 2 秒..."
        sleep 2
        $0 start
        ;;
        
    status)
        echo_title "应用服务状态"
        echo_separator
        
        if is_running; then
            echo_success "应用正在运行中"
            echo_info "PID: $(get_pid)"
            echo_info "端口: $PORT"
            echo_info "日志: $LOG_FILE"
            
            echo_separator
            echo_info "最近 5 行日志:"
            if [ -f "$LOG_FILE" ]; then
                tail -5 "$LOG_FILE"
            else
                echo_warning "日志文件不存在"
            fi
        else
            echo_warning "应用未运行"
        fi
        
        echo_separator
        ;;
        
    logs)
        echo_title "查看应用日志"
        echo_separator
        echo_info "按 Ctrl+C 退出日志查看"
        echo_separator
        
        if [ -f "$LOG_FILE" ]; then
            tail -f "$LOG_FILE"
        else
            echo_error "日志文件不存在: $LOG_FILE"
            exit 1
        fi
        ;;
        
    *)
        echo_title "使用帮助"
        echo_separator
        echo "用法: $0 {start|stop|restart|status|logs}"
        echo ""
        echo "  start    - 启动应用服务"
        echo "  stop     - 停止应用服务"
        echo "  restart  - 重启应用服务"
        echo "  status   - 查看应用状态"
        echo "  logs     - 查看实时日志"
        echo ""
        echo_separator
        exit 1
        ;;
esac

# 脚本执行完成
echo_info "脚本执行完成"
