# ---------- Etapa 1: compilar el JAR ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Etapa 2: imagen liviana de ejecucion ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN useradd --system --create-home appuser
# Copia el JAR de la etapa 'build' (nombre fijado con <finalName> en el pom.xml)
COPY --from=build /app/target/lab12026p.jar lab12026p.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "lab12026p.jar"]
