FROM amazoncorretto:21-alpine-jdk
COPY target/*.jar main.jar
ENTRYPOINT ["java","-jar","/main.jar"]