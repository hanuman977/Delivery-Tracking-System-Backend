# Multi-stage Dockerfile for delivery-service
FROM eclipse-temurin:21-jdk AS build
ARG MAVEN_VERSION=3.6.3
RUN apt-get update && apt-get install -y --no-install-recommends wget ca-certificates tar \
 && wget -qO /tmp/apache-maven.tar.gz https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
 && tar -xzf /tmp/apache-maven.tar.gz -C /opt \
 && ln -s /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/local/bin/mvn \
 && rm -rf /var/lib/apt/lists/* /tmp/apache-maven.tar.gz
WORKDIR /workspace
COPY pom.xml mvnw* ./
COPY .mvn .mvn
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
ARG JAR_FILE=target/*.jar
COPY --from=build ${JAR_FILE} /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
