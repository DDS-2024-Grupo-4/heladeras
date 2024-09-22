# syntax = docker/dockerfile:1.2
#
# Build stage
#

FROM maven:3.8.6-openjdk-18 AS build
COPY . .
RUN mvn clean package assembly:single -DskipTests

#
# Package stage
#

FROM openjdk:17-jdk-slim
COPY --from=build /target/TPDDSApp.jar TPDDSApp.jar

ENV QUEUE_HOST=prawn.rmq.cloudamqp.com
ENV QUEUE_USERNAME=zturjdgw
ENV QUEUE_PASSWORD=ezy9hZZ36GrMrRkICe_qnGrmsSJIdwiX
ENV VHOST=zturjdgw
ENV QUEUE_NAME=Temperaturas
EXPOSE 8080

CMD ["java","-classpath","TPDDSApp.jar","ar.edu.utn.dds.k3003.app.WebApp"]