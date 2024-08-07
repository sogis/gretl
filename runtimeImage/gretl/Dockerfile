#FROM bellsoft/liberica-openjdk-alpine:11.0.19-7
FROM bellsoft/liberica-openjdk-alpine:8u362-9

CMD ["gretl"]

ENV HOME=/home/gradle
ARG UID=1001
RUN adduser -h $HOME -s /sbin/nologin -G root -u $UID -D appuser

WORKDIR $HOME
RUN chgrp root . && chmod g+w .

COPY ca-certificates/ /usr/local/share/ca-certificates/
RUN apk add --no-cache bash ca-certificates java-cacerts curl \
    && ln -sf /etc/ssl/certs/java/cacerts $JAVA_HOME/jre/lib/security/cacerts
#RUN apk add --no-cache bash ca-certificates java-cacerts curl \
#    && ln -sf /etc/ssl/certs/java/cacerts $JAVA_HOME/lib/security/cacerts

ENV JAVA_TOOL_OPTIONS=-Duser.home=${HOME}
ENV GRADLE_HOME /opt/gradle
ENV GRADLE_VERSION 5.1.1
ARG GRADLE_DOWNLOAD_SHA256=4953323605c5d7b89e97d0dc7779e275bccedefcdac090aec123375eae0cc798
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

COPY gretl /usr/local/bin/
COPY __jars4image /home/gradle/libs/
COPY init.gradle /home/gradle/
COPY known_hosts ${HOME}/.ssh/

RUN ls -la /usr/local/bin/  && \
    ls -la /home/gradle && \
    ls -la /home/gradle/libs

WORKDIR ${HOME}/project
RUN chgrp root . && chmod g+w .

USER $UID
