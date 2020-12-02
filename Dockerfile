FROM registry.access.redhat.com/ubi8/ubi-minimal:8.1

ARG JAVA_PACKAGE=java-11-openjdk-headless
ARG RUN_JAVA_VERSION=1.3.8

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'


WORKDIR '/app'

# Install java and the run-java script
# Also set up permissions for user `1001`
RUN microdnf install curl ca-certificates ${JAVA_PACKAGE}
RUN microdnf update
RUN microdnf clean all
RUN chown 1001 .
RUN chmod "g+rwX" .
RUN chown 1001:root .
RUN curl https://repo1.maven.org/maven2/io/fabric8/run-java-sh/${RUN_JAVA_VERSION}/run-java-sh-${RUN_JAVA_VERSION}-sh.sh -o ./run-java.sh
RUN chown 1001 ./run-java.sh
RUN chmod 540 ./run-java.sh
RUN echo "securerandom.source=file:/dev/urandom" >> /etc/alternatives/jre/lib/security/java.security

# Configure the JAVA_OPTIONS, you can add -XshowSettings:vm to also display the heap size.
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

COPY backend/target/lib/* ./lib/
COPY backend/target/*-runner.jar ./app.jar
COPY shapevl ./

EXPOSE 8080
#USER 1001

ENV APP_RESOURCES='/app/resources'

ENTRYPOINT [ "./run-java.sh" ]