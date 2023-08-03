FROM openjdk:1.8

ADD target/your-application.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
