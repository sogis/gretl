FROM openshift/jenkins-slave-maven-centos7

USER root

COPY gretl /usr/local/bin/
COPY __jars4image /home/gradle/libs/

COPY init.gradle /home/gradle/

RUN mkdir -p ~/.gradle && echo "org.gradle.daemon=false" >> ~/.gradle/gradle.properties \
    chown -R 1001:0 /home/gradle && \
    chmod -R g+rw /home/gradle && \
    ls -la /usr/local/bin/  && \
    ls -la /home/gradle && \
    ls -la /home/gradle/libs

USER 1001
