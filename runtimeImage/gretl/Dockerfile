# FROM gradle:7.6.4-jdk11 AS build-stage 

# COPY stage-duckdb-extensions.gradle /tmp/build.gradle
# RUN gradle -p /tmp/
# RUN ls -la /root/.duckdb/extensions

FROM bellsoft/liberica-openjdk-alpine:11.0.18-10

CMD ["gretl"]

ENV HOME=/home/gradle
ARG UID=1001
RUN adduser -h $HOME -s /sbin/nologin -G root -u $UID -D appuser

WORKDIR $HOME
RUN chgrp root . && chmod g+w .

RUN apk add --no-cache bash ca-certificates java-cacerts curl libgcc libstdc++6 libstdc++ ncurses-libs \
    && ln -sf /etc/ssl/certs/java/cacerts $JAVA_HOME/lib/security/cacerts
RUN /usr/glibc/sbin/ldconfig

ENV JAVA_TOOL_OPTIONS=-Duser.home=${HOME}
ENV GRADLE_HOME /opt/gradle
ENV GRADLE_VERSION 7.6.4
ARG GRADLE_DOWNLOAD_SHA256=bed1da33cca0f557ab13691c77f38bb67388119e4794d113e051039b80af9bb1
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
#COPY --from=build-stage /root/.duckdb /home/gradle/.duckdb

# COPY stage-duckdb-extensions.gradle /tmp/build.gradle
# RUN gradle -p /tmp/ -s
# todo delete

RUN ls -la /usr/local/bin/  && \
    ls -la /home/gradle && \
    ls -la /home/gradle/libs
    # ls -la /home/gradle/libs && \
    # ls -la /home/gradle/.duckdb/extensions


WORKDIR ${HOME}/project
RUN chgrp root . && chmod g+w .

USER $UID
