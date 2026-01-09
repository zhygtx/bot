#!/bin/bash

# 设置 JVM 参数
JAVA_OPTS="-Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"
JAVA_OPTS="$JAVA_OPTS -Dspring.config.location=classpath:/,file:./,file:./config/"

PID_FILE="bot.pid"
LOG_FILE="bot.log"
JAR_FILE="bot.jar"
PORT=8080

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
    if command -v java &> /dev/null; then
        java_version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
        if [[ $java_version =~ ^17\. ]]; then
            echo "Java 17 已安装"
            return 0
        else
            echo "检测到 Java 版本: $java_version，需要安装 Java 17"
        fi
    else
        echo "Java 未安装，正在安装 Java 17..."
    fi

    # 尝试安装 OpenJDK 17
    if command -v apt-get &> /dev/null; then
        sudo apt-get update
        sudo apt-get install -y openjdk-17-jre-headless
    elif command -v yum &> /dev/null; then
        sudo yum install -y java-17-openjdk-headless
    elif command -v dnf &> /dev/null; then
        sudo dnf install -y java-17-openjdk-headless
    else
        echo "错误: 未找到包管理器，无法自动安装 Java 17"
        exit 1
    fi

    # 验证安装
    if ! command -v java &> /dev/null; then
        echo "错误: Java 17 安装失败"
        exit 1
    fi

    java_version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2)
    if [[ $java_version =~ ^17\. ]]; then
        echo "Java 17 安装成功"
    else
        echo "错误: 安装的 Java 版本不是 17，当前版本: $java_version"
        exit 1
    fi
}

# 检查并安装 npm 和 nodejs
ensure_npm_installed() {
    # 检查 Node.js 是否已安装
    if ! node -v &> /dev/null; then
        echo "Node.js 未安装，正在安装..."
        if command -v apt-get &> /dev/null; then
            sudo apt-get update
            sudo apt-get install -y nodejs npm
        elif command -v yum &> /dev/null; then
            sudo yum install -y nodejs npm
        elif command -v dnf &> /dev/null; then
            sudo dnf install -y nodejs npm
        else
            echo "错误: 未找到包管理器，无法自动安装 nodejs 和 npm"
            exit 1
        fi
    else
        echo "Node.js 已安装"
    fi

    # 验证 npm 安装
    if ! command -v npm &> /dev/null; then
        echo "npm 未安装，正在安装..."
        if command -v apt-get &> /dev/null; then
            sudo apt-get install -y npm
        elif command -v yum &> /dev/null; then
            sudo yum install -y npm
        elif command -v dnf &> /dev/null; then
            sudo dnf install -y npm
        else
            echo "错误: 未找到包管理器，无法自动安装 npm"
            exit 1
        fi
    else
        echo "npm 已安装"
    fi

    # 验证安装
    if ! command -v node &> /dev/null || ! command -v npm &> /dev/null; then
        echo "错误: nodejs 或 npm 安装失败"
        exit 1
    fi
    echo "nodejs 和 npm 安装成功"
}

# 检查并安装 Playwright
ensure_playwright_installed() {
    ensure_npm_installed

    # 检查 Playwright 是否已安装
    if ! npx playwright --version &> /dev/null; then
        echo "Playwright 未安装或安装不完整，正在安装..."
        npm init playwright@latest --yes
        echo "Playwright 安装成功"
    else
        echo "Playwright 已安装"
    fi
}

# 检查并安装 lsof
ensure_lsof_installed() {
    if ! command -v lsof &> /dev/null; then
        echo "lsof 命令未找到，正在安装..."
        if command -v apt-get &> /dev/null; then
            sudo apt-get update
            sudo apt-get install -y lsof
        elif command -v yum &> /dev/null; then
            sudo yum install -y lsof
        elif command -v dnf &> /dev/null; then
            sudo dnf install -y lsof
        else
            echo "错误: 未找到包管理器，无法自动安装 lsof"
            exit 1
        fi

        # 验证安装
        if ! command -v lsof &> /dev/null; then
            echo "错误: lsof 安装失败"
            exit 1
        fi
        echo "lsof 安装成功"
    fi
}

# 杀死占用端口的进程
kill_port_process() {
    ensure_lsof_installed

    local port=$1
    local pid=$(lsof -t -i:$port)
    if [ ! -z "$pid" ]; then
        echo "发现端口 $port 被进程 $pid 占用，正在终止..."
        kill -9 $pid
        echo "端口 $port 的占用进程已终止"
    else
        echo "端口 $port 未被占用"
    fi
}

case "$1" in
    start)
        # 启动前检查环境依赖
        echo "正在检查环境依赖..."
        ensure_java17_installed
        ensure_playwright_installed
        echo "环境依赖检查完成"

        if is_running; then
            echo "应用已在运行中 (PID: $(get_pid))"
        else
            echo "正在清理端口 $PORT..."
            kill_port_process $PORT

            echo "正在启动应用..."
            # 检查JAR文件是否存在
            if [ ! -f "$JAR_FILE" ]; then
                echo "错误: 未找到JAR文件 $JAR_FILE"
                exit 1
            fi
            nohup java $JAVA_OPTS -jar "$JAR_FILE" > "$LOG_FILE" 2>&1 &
            echo $! > "$PID_FILE"
            echo "应用已启动，PID: $(cat $PID_FILE)"
        fi
        ;;
    stop)
        if is_running; then
            pid=$(get_pid)
            kill "$pid"
            rm -f "$PID_FILE"
            echo "应用已停止 (PID: $pid)"
        else
            echo "应用未运行"
        fi
        # 清理端口占用
        kill_port_process $PORT
        ;;
    restart)
        $0 stop
        sleep 2
        $0 start
        ;;
    status)
        if is_running; then
            echo "应用正在运行 (PID: $(get_pid))"
            # 显示最后5行日志
            echo "最近日志:"
            tail -5 "$LOG_FILE"
        else
            echo "应用未运行"
        fi
        ;;
    logs)
        if [ -f "$LOG_FILE" ]; then
            tail -f "$LOG_FILE"
        else
            echo "日志文件不存在: $LOG_FILE"
        fi
        ;;
    *)
        echo "用法: $0 {start|stop|restart|status|logs}"
        echo "  start  - 启动应用"
        echo "  stop   - 停止应用"
        echo "  restart- 重启应用"
        echo "  status - 查看应用状态"
        echo "  logs   - 查看实时日志"
        exit 1
        ;;
esac
