# Multi-stage build for Enlighten Platform (Spring Boot 3 / Java 17)
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy POMs first for dependency caching
COPY pom.xml ./
COPY enlighten-platform-api/pom.xml enlighten-platform-api/pom.xml
COPY enlighten-platform-biz/pom.xml enlighten-platform-biz/pom.xml
COPY enlighten-platform-web/pom.xml enlighten-platform-web/pom.xml

# Prepare Maven settings for China mirrors
RUN mkdir -p /root/.m2
COPY enlighten-platform-web/maven-settings.xml /root/.m2/settings.xml

# Pre-fetch dependencies for offline build speed
RUN mvn -q -B -DskipTests dependency:go-offline

# Copy the rest of the source and build
COPY . .
RUN mvn -q -B -DskipTests clean package

FROM eclipse-temurin:17-jre

ENV TZ=Asia/Shanghai
WORKDIR /app

# Create non-root user
RUN useradd -ms /bin/bash appuser

# Copy jar from previous stage
COPY --from=build /app/enlighten-platform-web/target/enlighten-platform-web-*.jar /app/app.jar

# Ensure permissions
RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Duser.timezone=$TZ -jar /app/app.jar"]


