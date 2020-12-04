FROM openjdk:11-jdk-slim

ARG RUN_JAVA_VERSION=1.3.8

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'


WORKDIR '/app'

# Install java and the run-java script
RUN apt-get update \
    && apt-get -y install curl g++ \
    && curl https://repo1.maven.org/maven2/io/fabric8/run-java-sh/${RUN_JAVA_VERSION}/run-java-sh-${RUN_JAVA_VERSION}-sh.sh -o ./run-java.sh \
    && chmod "g+rwX" . \
    && chmod 540 ./run-java.sh

# Configure the JAVA_OPTIONS, you can add -XshowSettings:vm to also display the heap size.
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

COPY backend/target/lib/* ./lib/
COPY backend/target/*-runner.jar ./app.jar
COPY shapevl ./

EXPOSE 8080

ENV APP_RESOURCES='/app/resources'

ENTRYPOINT [ "./run-java.sh" ]