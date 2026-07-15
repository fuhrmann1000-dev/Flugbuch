# =============================================================
#  Build Stage – JDK + Maven
# =============================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

RUN apk add --no-cache bash curl tar gzip

WORKDIR /build

# Download Maven
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.7/binaries/apache-maven-3.9.7-bin.tar.gz | tar xz -C /opt && \
    ln -s /opt/apache-maven-3.9.7/bin/mvn /usr/local/bin/mvn

# POM zuerst kopieren → Dependency-Layer wird gecached
COPY pom.xml .

RUN mvn dependency:go-offline -B

# Quellcode kopieren und JAR bauen
COPY src ./src
RUN mvn clean package -DskipTests -B

# Layered JAR extrahieren für optimiertes Layer-Caching im Runtime-Image
RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# =============================================================
#  Runtime Stage – nur JRE, kein Build-Tool
# =============================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Non-root User für Sicherheit
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Layered JAR in richtiger Reihenfolge kopieren:
# selten ändernde Layers zuerst → besseres Caching
COPY --from=builder --chown=spring:spring /build/target/extracted/dependencies/ ./
COPY --from=builder --chown=spring:spring /build/target/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /build/target/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /build/target/extracted/application/ ./

USER spring:spring

EXPOSE 8080

# Healthcheck via Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
