FROM amazoncorretto:21-alpine-jdk
COPY server/target/*.jar stats.jar
ENTRYPOINT ["java","-jar","/stats.jar"]