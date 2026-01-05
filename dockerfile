# 1단계: 빌드
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# gradlew 실행 권한 문제 방지
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# 소스 복사 후 빌드
COPY . .
RUN ./gradlew bootJar --no-daemon

# 2단계: 실행
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
