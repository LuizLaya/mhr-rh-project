# Build multi-stage: a imagem final só leva o .jar, não o JDK completo nem o
# cache do Gradle - fica bem menor (importa no free tier do Render, que limita
# o tamanho da imagem).
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copia só o necessário pra resolver dependências antes do código-fonte, pra
# cachear essa camada entre builds (só muda quando build.gradle.kts muda).
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon || true

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# $PORT é definido automaticamente pelo Render (ver application.yml, server.port)
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
