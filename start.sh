#!/bin/bash
# HenuGP 生产环境启动脚本
# 适用：阿里云 2C2G Ubuntu 服务器
# 前置：在项目根目录执行 mvn clean package -DskipTests，生成 henu-admin/target/henu-admin.jar

set -e
APP_JAR="henu-admin/target/henu-admin.jar"

if [ ! -f "$APP_JAR" ]; then
    echo "未找到 $APP_JAR，请先在项目根目录执行 mvn clean package -DskipTests"
    exit 1
fi

# JVM 调优：堆固定 512MB（为 MySQL/Redis/OS 预留约 1.5GB），Metaspace 上限 256MB
JVM_OPTS="-Xms512m -Xmx512m -XX:MaxMetaspaceSize=256m"

# 同时激活 druid（数据源）与 prod（生产调优）profile
SPRING_OPTS="--spring.profiles.active=druid,prod"

# 确保日志目录存在
mkdir -p /home/henu/logs

nohup java $JVM_OPTS -jar "$APP_JAR" $SPRING_OPTS > /home/henu/logs/startup.out 2>&1 &
echo "HenuGP 已后台启动，PID=$!"
echo "业务日志：/home/henu/logs/sys-info.log"
echo "启动输出：/home/henu/logs/startup.out"
