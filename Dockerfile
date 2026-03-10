# 자바설치(컨테이너 내)
FROM eclipse-temurin:17-jdk-alpine as stage1

WORKDIR /app
# 폴더는 이름 지정. gradle이라는 폴더을 gradle이라는 이름으로 복사하겠다
COPY gradle gradle
COPY src src
# 파일은 위치 지정
COPY build.gradle .
COPY gradlew .
COPY settings.gradle .

RUN chmod +x gradlew
RUN ./gradlew bootJar
# 두번째 스테이지(컨테이너) : 이미지 경량화를 위해 스테이지 분리작업
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=stage1 /app/build/libs/*.jar ordersystem.jar


# cmd써도된다-이름이 만들어지는 rule = setting.gradel이름 + build.gradle버전 ->실수할 확률이 높다
# ENTRYPOINT [ "java", "-jar","build/libs/ordersystem-0.0.1-SNAPSHOT.jar" ]
ENTRYPOINT [ "java", "-jar","ordersystem.jar" ]



# 도커이미지 빌드 (본인 도커 레파지토리에 있는 이미지명 삭요)
# docker build -t ss19990222/myordersystem2:latest .
# ordersystem으로 들어와서

#push
#docker push ss19990222/myordersystem2:latest


# 도커컨테이너 실행
# sudo docker run --name myspring -d -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/ordersystem?useSSL=true -e SPRING_DATESOUSE_USERNAME=root -e SPRING_DATESOUSE_PASSWORD=test1234 -e SPRING_REDIS_HOST=host.docker.internal ss19990222/myordersystem2:latest

