#!/bin/bash
#
# plan-core 启动脚本 (CentOS 7)
# 用法: ./plan-core.sh {start|stop|restart|status}
#

# # 赋予执行权限
# chmod +x scripts/plan-core.sh

# # 启动
# ./scripts/plan-core.sh start

# # 停止
# ./scripts/plan-core.sh stop

# # 重启
# ./scripts/plan-core.sh restart

# # 查看状态
# ./scripts/plan-core.sh status

APP_NAME=plan-core
JAR_NAME=plan-core-0.1-RELEASE.jar
# JAR 与脚本同目录，如 /opt/plancore
APP_HOME=$(cd "$(dirname "$0")" && pwd)
JAR_PATH=${APP_HOME}/${JAR_NAME}
PID_FILE=${APP_HOME}/logs/${APP_NAME}.pid
LOG_FILE=${APP_HOME}/logs/${APP_NAME}.log

# JVM 参数: -Xms2G -Xmx2G, G1 GC
JAVA_OPTS="-Xms2G -Xmx2G"
JAVA_OPTS="${JAVA_OPTS} -XX:+UseG1GC"
# 设置目标停顿时间（根据您的应用调整，通常100-200ms）
JAVA_OPTS="${JAVA_OPTS} -XX:MaxGCPauseMillis=200"
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError"
JAVA_OPTS="${JAVA_OPTS} -XX:HeapDumpPath=${APP_HOME}/logs/heapdump.hprof"

# 若需指定 JDK，取消下行注释并修改路径
# export JAVA_HOME=/usr/lib/jvm/java-21-openjdk

check_jar() {
    if [ ! -f "${JAR_PATH}" ]; then
        echo "错误: 找不到 JAR 文件 ${JAR_PATH}"
        exit 1
    fi
}

check_pid() {
    if [ -f "${PID_FILE}" ]; then
        PID=$(cat "${PID_FILE}")
        if ps -p "${PID}" > /dev/null 2>&1; then
            return 0
        fi
        rm -f "${PID_FILE}"
    fi
    return 1
}

start() {
    check_jar
    if check_pid; then
        echo "${APP_NAME} 已在运行 (PID: ${PID})"
        exit 0
    fi
    mkdir -p "${APP_HOME}/logs"
    echo "启动 ${APP_NAME} ..."
    nohup java ${JAVA_OPTS} -jar "${JAR_PATH}" >> "${LOG_FILE}" 2>&1 &
    echo $! > "${PID_FILE}"
    sleep 2
    if check_pid; then
        echo "${APP_NAME} 启动成功 (PID: ${PID})"
    else
        echo "${APP_NAME} 启动失败，请查看日志: ${LOG_FILE}"
        exit 1
    fi
}

stop() {
    if ! check_pid; then
        echo "${APP_NAME} 未运行"
        exit 0
    fi
    echo "停止 ${APP_NAME} (PID: ${PID}) ..."
    kill "${PID}" 2>/dev/null
    for i in $(seq 1 30); do
        if ! ps -p "${PID}" > /dev/null 2>&1; then
            rm -f "${PID_FILE}"
            echo "${APP_NAME} 已停止"
            exit 0
        fi
        sleep 1
    done
    echo "强制停止 ${APP_NAME} ..."
    kill -9 "${PID}" 2>/dev/null
    rm -f "${PID_FILE}"
    echo "${APP_NAME} 已强制停止"
}

status() {
    if check_pid; then
        echo "${APP_NAME} 运行中 (PID: ${PID})"
    else
        echo "${APP_NAME} 未运行"
        exit 1
    fi
}

case "$1" in
    start)   start ;;
    stop)    stop ;;
    restart) stop; start ;;
    status)  status ;;
    *)
        echo "用法: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac
