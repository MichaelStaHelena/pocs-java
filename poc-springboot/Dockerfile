# syntax=docker/dockerfile:1.7

# ---------- stage 1: build ----------
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# copia só o pom primeiro pra cachear o download das deps
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -B dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -B -DskipTests package

# quebra o jar em layers pra melhorar o cache da imagem final
RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# ---------- stage 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

# cada cópia vira uma layer — as que mudam menos ficam por baixo
ARG EXTRACTED=/app/target/extracted
COPY --from=build ${EXTRACTED}/dependencies/ ./
COPY --from=build ${EXTRACTED}/spring-boot-loader/ ./
COPY --from=build ${EXTRACTED}/snapshot-dependencies/ ./
COPY --from=build ${EXTRACTED}/application/ ./

EXPOSE 8080

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
