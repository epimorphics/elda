FROM amazoncorretto:21-alpine3.20

ENV ELDA_PORT=8080
EXPOSE 8080

ENV ELDA_WEBAPP_PATH=/etc/elda

COPY target/elda-standalone-jar-with-dependencies.jar app.jar

CMD java -jar /app.jar
