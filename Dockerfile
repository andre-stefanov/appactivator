FROM anapsix/alpine-java:8

MAINTAINER Andre Stefanov "andriy.stefanov@gmail.com"

VOLUME ["/tmp"]

EXPOSE 8080

COPY ./build/libs/appactivator.jar app.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
