FROM bellsoft/liberica-openjdk-alpine:8u322-6

CMD ["gretl"]

WORKDIR /home/gradle
RUN chmod g+w .

RUN apk add --no-cache bash

ENV GRADLE_USER_HOME=/home/gradle/.gradle
ENV GRADLE_HOME /opt/gradle
ENV GRADLE_VERSION 4.2.1
ARG GRADLE_DOWNLOAD_SHA256=b551cc04f2ca51c78dd14edb060621f0e5439bdfafa6fd167032a09ac708fbc0
RUN set -o errexit -o nounset \
    && echo "Downloading Gradle" \
    && wget --no-verbose --output-document=gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
    \
    && echo "Checking download hash" \
    && echo "${GRADLE_DOWNLOAD_SHA256} *gradle.zip" | sha256sum -c - \
    \
    && echo "Installing Gradle" \
    && unzip gradle.zip \
    && rm gradle.zip \
    && mv "gradle-${GRADLE_VERSION}" "${GRADLE_HOME}/" \
    && ln -s "${GRADLE_HOME}/bin/gradle" /usr/bin/gradle \
    \
    && echo "Testing Gradle installation" \
    && (umask 0002 && gradle --version)

ENV ILI_CACHE=/home/gradle/.ilicache
COPY gretl /usr/local/bin/
COPY __jars4image /home/gradle/libs/
COPY init.gradle /home/gradle/

RUN apk add --no-cache curl

RUN ls -la /usr/local/bin/  && \
    ls -la /home/gradle && \
    ls -la /home/gradle/libs

WORKDIR /home/gradle/project
RUN chmod g+w .

USER 1001
