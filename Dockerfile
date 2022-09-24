FROM openjdk:8-jre

EXPOSE 28087

COPY target/scala-2.13/webhook.jar /app.jar

CMD ["java","-jar","app.jar"]
