####
#
# Before building the docker image run:
#
# mvn package
#
# Then, build the image with:
#
# docker build -t ptmm .
#
# Then run the container using:
#
# docker run -i --rm ptmm
#
###
FROM registry.access.redhat.com/ubi8/ubi-minimal:8.1

ARG JAVA_PACKAGE=java-11-openjdk-headless
ARG RUN_JAVA_VERSION=1.3.8

ARG USER_ID=1000
ARG GROUP_ID=1000

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

# Install java and the run-java script
# Also set up permissions for user `1001`
RUN microdnf install curl ca-certificates ${JAVA_PACKAGE} \
    && microdnf update \
    && microdnf clean all \
    && mkdir /deployments \
    && chown ${USER_ID} /deployments \
    && chmod "g+rwX" /deployments \
    && chown ${USER_ID}:root /deployments \
    && curl https://repo1.maven.org/maven2/io/fabric8/run-java-sh/${RUN_JAVA_VERSION}/run-java-sh-${RUN_JAVA_VERSION}-sh.sh -o /deployments/run-java.sh \
    && chown ${USER_ID} /deployments/run-java.sh \
    && chmod 540 /deployments/run-java.sh \
    && echo "securerandom.source=file:/dev/urandom" >> /etc/alternatives/jre/lib/security/java.security

# Configure the JAVA_OPTIONS, you can add -XshowSettings:vm to also display the heap size.
ENV JAVA_OPTIONS=""

COPY target/ptmm.jar /deployments/ptmm.jar

EXPOSE 8080
USER ${USER_ID}

ENTRYPOINT [ "/deployments/run-java.sh" ]