FROM dorowu/ubuntu-desktop-lxde-vnc

USER root

RUN rm -f /etc/apt/sources.list.d/google-chrome.list

RUN apt-get update && apt-get install -y \
    openjdk-21-jdk \
    maven

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH
ENV DISPLAY=:1

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY app_startup.sh .

RUN mvn clean package -DskipTests
RUN chmod +x /app/app_startup.sh

# Add app to supervisord
RUN cat >> /etc/supervisor/conf.d/supervisord.conf << 'EOF'

[program:app]
command=/app/app_startup.sh
autorestart=true
stdout_logfile=/var/log/app.log
stderr_logfile=/var/log/app.err
EOF
