# =============================================================
#  Build Stage – JDK + Maven
# =============================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Maven-Wrapper und POM zuerst kopieren → Dependency-Layer wird gecached,
# solange sich pom.xml nicht ändert
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline -B

# Quellcode kopieren und JAR bauen
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

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