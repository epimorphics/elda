FROM amazoncorretto:21-alpine3.20

ENV elda_port=8080
EXPOSE 8080

COPY target/elda-standalone-jar-with-dependencies.jar app.jar

CMD java -jar /app.jar
