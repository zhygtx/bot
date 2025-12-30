@echo off
chcp 65001
setlocal

REM 设置 JVM 参数
set JAVA_OPTS=-Xms512m -Xmx4096m
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8
set JAVA_OPTS=%JAVA_OPTS% -Dsun.jnu.encoding=UTF-8

REM 指定外部配置文件位置
set JAVA_OPTS=%JAVA_OPTS% -Dspring.config.location=classpath:/,file:./,file:./config/

REM 启动应用
echo 正在启动 Spring Boot 应用...
java %JAVA_OPTS% -jar bot.jar

pause
