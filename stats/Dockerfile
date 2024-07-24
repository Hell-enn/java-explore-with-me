FROM amazoncorretto:11-alpine-jdk
COPY server/target/*.jar stats.jar
ENTRYPOINT ["java","-jar","/stats.jar"]