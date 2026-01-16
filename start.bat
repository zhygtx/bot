@echo off
chcp 65001
setlocal

REM 设置 JVM 参数
set JAVA_OPTS=-Xms512m -Xmx4096m
set JAVA_OPTS=%JAVA_OPTS% -Dfile.encoding=UTF-8
set JAVA_OPTS=%JAVA_OPTS% -Dsun.jnu.encoding=UTF-8

REM 检查并添加外部配置文件
if exist "config\application.yml" (
    set CONFIG_OPTS=--spring.config.location=file:./config/application.yml
) else if exist "application.yml" (
    set CONFIG_OPTS=--spring.config.location=file:./application.yml
) else (
    set CONFIG_OPTS=--spring.config.location=optional:file:./config/application.yml,optional:file:./application.yml
)

REM 检查并添加外部日志配置文件
if exist "logback-spring.xml" (
    set LOGGING_OPTS=-Dlogging.config=./logback-spring.xml
) else if exist "config\\logback-spring.xml" (
    set LOGGING_OPTS=-Dlogging.config=./config/logback-spring.xml
)

REM 启动应用
echo 正在启动 Spring Boot 应用...
java %JAVA_OPTS% %LOGGING_OPTS% -jar bot.jar %CONFIG_OPTS%

pause
