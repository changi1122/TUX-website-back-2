# 자바 소스를 빌드해 JAR 생성
FROM eclipse-temurin:21 AS int-build
LABEL description="tux docker image builder"

# 패키지 설치 및 클론
RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*

# 캐시 무효화용 인자 추가
ARG CACHE_BUST=1

RUN git clone https://github.com/changi1122/TUX-website-back-2.git /app
WORKDIR /app

RUN chmod +x gradlew
RUN ./gradlew clean bootJar --no-daemon

# 빌드된 JAR를 경량화 이미지에 복사
FROM gcr.io/distroless/java21-debian12
LABEL description="tux application"
EXPOSE 4001

COPY --from=int-build /app/build/libs/*.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
