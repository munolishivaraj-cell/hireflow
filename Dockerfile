# ---- build ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY src src
# Tests need Docker (Testcontainers), so they run locally / in CI rather than in the image build
RUN ./mvnw -q -B package -DskipTests && cp target/hireflow-*.jar app.jar

# ---- run ----
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 1001 hireflow
COPY --from=build /app/app.jar app.jar
USER hireflow

# Tuned for Render's free instance (512 MB RAM, 0.1 CPU):
#  - heap capped at 60% of the container so metaspace, threads and Kafka buffers fit in the rest
#  - SerialGC and C1-only JIT use the least memory and start fastest on a fraction of a CPU
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k -XX:MaxMetaspaceSize=160m -XX:ReservedCodeCacheSize=48m"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
