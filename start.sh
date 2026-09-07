#!/bin/bash

# =============================================================================
# QQBot 可视化回复规则项目 - Linux启动脚本 (修复版)
# 版本: 1.0.0
# 描述: 用于管理 QQBot 后端服务的启动、停止、重启和状态查看
# 作者: Auto Generated
# =============================================================================

# --------------------------- 配置变量 ---------------------------
# JVM 参数配置 - 添加网络配置以解决IPv4/IPv6问题
JAVA_OPTS="-Xms1024m -Xmx2048m -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dserver.address=0.0.0.0 -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false -Djava.net.bindv6only=false"
JAVA_OPTS="$JAVA_OPTS -Dspring.config.location=classpath:/,file:./,file:./config/"
# 应用配置
PID_FILE="bot.pid"          # 进程ID文件
LOG_FILE="bot.log"          # 日志文件
JAR_FILE="bot.jar"          # 应用JAR文件名
PORT=8080                   # 应用端口

# 配置文件处理 - 支持外部配置文件优先加载
SPRING_CONFIG_OPTS=""
if [ -f "./config/application.yml" ]; then
    SPRING_CONFIG_OPTS="--spring.config.location=file:./config/application.yml"
elif [ -f "./application.yml" ]; then
    SPRING_CONFIG_OPTS="--spring.config.location=file:./application.yml"
else
    SPRING_CONFIG_OPTS="--spring.config.location=optional:file:./config/application.yml,optional:file:./application.yml"
fi

# 日志配置文件处理 - 支持外部 logback 配置文件
LOGGING_OPTS=""
if [ -f "./logback-spring.xml" ]; then
    LOGGING_OPTS="-Dlogging.config=./logback-spring.xml"
elif [ -f "./config/logback-spring.xml" ]; then
    LOGGING_OPTS="-Dlogging.config=./config/logback-spring.xml"
fi

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

# 检查并安装 Java 21（包管理器失败时自动回退为下载压缩包安装，兼容 CentOS 7 等无 Java 21 包的系统）
ensure_java21_installed() {
    local java_version
    echo_info "检查 Java 21 环境..."

    if command -v java &> /dev/null; then
        java_version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
        if [[ $java_version =~ ^21\. ]]; then
            echo_success "Java 21 已安装 (版本: $java_version)"
            return 0
        else
            echo_warning "检测到 Java 版本: $java_version，需要安装 Java 21"
        fi
    else
        echo_warning "Java 未安装，正在安装 Java 21..."
    fi

    # 尝试通过包管理器安装 OpenJDK 21（部分系统仓库没有 21，安装失败不影响后续回退）
    if command -v apt-get &> /dev/null; then
        sudo apt-get update
        sudo apt-get install -y openjdk-21-jre-headless
    elif command -v yum &> /dev/null; then
        sudo yum install -y java-21-openjdk-headless
    elif command -v dnf &> /dev/null; then
        sudo dnf install -y java-21-openjdk-headless
    else
        echo_warning "未找到可用的包管理器，将改用压缩包方式安装 Java 21"
    fi

    # 验证安装，不满足则走压缩包兜底安装
    if command -v java &> /dev/null; then
        java_version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
        if [[ $java_version =~ ^21\. ]]; then
            echo_success "Java 21 安装成功 (版本: $java_version)"
            return 0
        fi
        echo_warning "当前 Java 版本: $java_version，尝试压缩包方式安装 Java 21"
    else
        echo_warning "仓库中未找到 Java 21 或安装失败，尝试压缩包方式安装"
    fi

    install_java21_from_tarball
    if [ $? -ne 0 ]; then
        echo_error "Java 21 自动安装失败，请手动安装 OpenJDK 21 后重试"
        exit 1
    fi
}

# 通过下载 OpenJDK 21 (Temurin) 压缩包安装 Java 21
# 适用场景: CentOS 7 等系统仓库无 Java 21 包，或系统没有包管理器
install_java21_from_tarball() {
    local SUDO=""
    if [ "$(id -u)" -ne 0 ]; then
        SUDO="sudo"
    fi

    # 如果目标目录已有可用的 JDK 21 则直接复用，跳过下载
    if [ -x "/usr/local/jdk-21/bin/java" ]; then
        local exist_ver=$(/usr/local/jdk-21/bin/java -version 2>&1 | head -n1 | cut -d'"' -f2)
        if [[ $exist_ver =~ ^21\. ]]; then
            echo_success "检测到 /usr/local/jdk-21 已安装 (版本: $exist_ver)，直接配置使用"
            $SUDO ln -sf /usr/local/jdk-21/bin/java /usr/local/bin/java
            $SUDO ln -sf /usr/local/jdk-21/bin/javac /usr/local/bin/javac
            export JAVA_HOME=/usr/local/jdk-21
            export PATH=$JAVA_HOME/bin:$PATH
            return 0
        fi
    fi

    # 检测 CPU 架构，选择对应的 JDK 包
    local arch=$(uname -m)
    local jdk_arch=""
    case "$arch" in
        x86_64)  jdk_arch="x64" ;;
        aarch64) jdk_arch="aarch64" ;;
        *)
            echo_error "暂不支持自动安装的架构: $arch，请手动安装 Java 21"
            return 1
            ;;
    esac

    # 检查必备下载工具
    if ! command -v curl &> /dev/null; then
        echo_error "未找到 curl 命令，无法下载 JDK，请先安装 curl"
        return 1
    fi

    local mirror_url="https://mirrors.tuna.tsinghua.edu.cn/Adoptium/21/jdk/${jdk_arch}/linux"
    local download_url=""
    local tmp_dir=$(mktemp -d)

    echo_info "正在从清华镜像获取 OpenJDK 21 最新版本信息..."
    local tarball=$(curl -fsSL --connect-timeout 10 "$mirror_url/" 2>/dev/null \
        | grep -oE "OpenJDK21U-jdk_${jdk_arch}_linux_hotspot_[0-9._]+\.tar\.gz" \
        | sort -Vu | tail -n1)

    if [ -n "$tarball" ]; then
        download_url="${mirror_url}/${tarball}"
        echo_info "发现最新版本: $tarball"
    else
        echo_warning "镜像信息获取失败，改用 Adoptium 官方源下载最新版..."
        download_url="https://api.adoptium.net/v3/binary/latest/21/ga/linux/${jdk_arch}/jdk/hotspot/normal/eclipse"
    fi

    cd "$tmp_dir"
    echo_info "正在下载 JDK 21 (约 200MB)，请耐心等待..."
    if ! curl -fSL --connect-timeout 15 --retry 2 -o jdk21.tar.gz "$download_url"; then
        echo_error "JDK 21 下载失败: $download_url"
        cd / && rm -rf "$tmp_dir"
        return 1
    fi

    echo_info "正在解压安装..."
    tar -xzf jdk21.tar.gz || { echo_error "JDK 21 解压失败"; cd / && rm -rf "$tmp_dir"; return 1; }
    local extracted_dir=$(find . -maxdepth 1 -type d -name "jdk-21*" | head -n1 | sed 's|^\./||')
    if [ -z "$extracted_dir" ]; then
        echo_error "未找到解压后的 JDK 目录"
        cd / && rm -rf "$tmp_dir"
        return 1
    fi

    # 安装到 /usr/local/jdk-21（旧目录先改名为备份，不直接删除）
    if [ -d "/usr/local/jdk-21" ]; then
        $SUDO rm -rf /usr/local/jdk-21.bak
        $SUDO mv /usr/local/jdk-21 /usr/local/jdk-21.bak
    fi
    $SUDO mv "$tmp_dir/$extracted_dir" /usr/local/jdk-21
    cd / && rm -rf "$tmp_dir"

    # 创建软链接并写入全局环境变量，确保任何方式启动都能找到 java
    $SUDO ln -sf /usr/local/jdk-21/bin/java /usr/local/bin/java
    $SUDO ln -sf /usr/local/jdk-21/bin/javac /usr/local/bin/javac
    echo 'export JAVA_HOME=/usr/local/jdk-21' | $SUDO tee /etc/profile.d/java.sh > /dev/null
    echo 'export PATH=$JAVA_HOME/bin:$PATH' | $SUDO tee -a /etc/profile.d/java.sh > /dev/null
    export JAVA_HOME=/usr/local/jdk-21
    export PATH=$JAVA_HOME/bin:$PATH

    # 验证安装结果
    local new_ver=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
    if [[ $new_ver =~ ^21\. ]]; then
        echo_success "Java 21 安装成功 (版本: $new_ver, 路径: /usr/local/jdk-21)"
        return 0
    else
        echo_error "Java 21 安装后验证失败，当前版本: $new_ver"
        return 1
    fi
}

# 检查并安装 npm 和 nodejs
ensure_npm_installed() {
    echo_info "检查 Node.js 和 npm 环境..."

    # 检查 Node.js 是否已安装
    if ! node -v &> /dev/null; then
        echo_warning "Node.js 未安装，正在安装..."
        if command -v apt-get &> /dev/null; then
            sudo apt-get update
            sudo apt-get install -y nodejs npm
        elif command -v yum &> /dev/null; then
            sudo yum install -y nodejs npm
        elif command -v dnf &> /dev/null; then
            sudo dnf install -y nodejs npm
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
            sudo apt-get install -y npm
        elif command -v yum &> /dev/null; then
            sudo yum install -y npm
        elif command -v dnf &> /dev/null; then
            sudo dnf install -y npm
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

# 检查并安装 lsof
ensure_lsof_installed() {
    echo_info "检查 lsof 工具..."

    if ! command -v lsof &> /dev/null; then
        echo_warning "lsof 命令未找到，正在安装..."
        if command -v apt-get &> /dev/null; then
            sudo apt-get update
            sudo apt-get install -y lsof
        elif command -v yum &> /dev/null; then
            sudo yum install -y lsof
        elif command -v dnf &> /dev/null; then
            sudo dnf install -y lsof
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
echo_title "QQBot 可视化回复规则项目启动脚本 (修复版)"
echo_separator

case "$1" in
    start)
        echo_title "启动应用服务"
        echo_separator
        
        # 启动前检查环境依赖
        ensure_java21_installed
        
        echo_success "核心环境依赖检查完成"

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
            nohup java $JAVA_OPTS $CONFIG_OPTS -jar "$JAR_FILE" > "$LOG_FILE" 2>&1 &
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
            echo_info "正在停止应用进程 (PID: $pid)..."
            
            kill "$pid" &> /dev/null
            if [ $? -ne 0 ]; then
                echo_warning "发送终止信号失败，尝试强制终止..."
                kill -9 "$pid" &> /dev/null
            fi
            
            local wait_count=0
            local max_wait=20
            while is_running && [ $wait_count -lt $max_wait ]; do
                sleep 0.5
                wait_count=$((wait_count + 1))
            done
            
            if is_running; then
                echo_warning "优雅终止超时，尝试杀死进程组..."
                kill -TERM -"$pid" &> /dev/null
                sleep 1
                
                if is_running; then
                    echo_info "执行强制杀死..."
                    kill -9 "$pid" &> /dev/null
                    kill -9 -"$pid" &> /dev/null
                    sleep 0.5
                fi
            fi
            
            rm -f "$PID_FILE"
            
            if is_running; then
                echo_error "应用停止失败，进程仍在运行 (PID: $pid)"
            else
                echo_success "应用已成功停止 (PID: $pid)"
            fi
        else
            echo_warning "应用未运行"
        fi

        echo_separator

        echo_info "检查并清理端口 $PORT 占用..."
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