FROM openjdk:23-jdk

WORKDIR /app

COPY build/libs/task-scheduler-0.0.1-SNAPSHOT.jar /app/task-scheduler.jar

EXPOSE 8081

CMD ["java", "-jar", "/app/task-scheduler.jar"]