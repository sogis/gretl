FROM quay.io/openshift/origin-jenkins-agent-base:latest

USER root

RUN dnf clean all
RUN rm -r /var/cache/dnf
#RUN rm -r /var/cache/PackageKit
RUN dnf upgrade

RUN dnf -y install https://download.postgresql.org/pub/repos/yum/reporpms/EL-8-x86_64/pgdg-redhat-repo-latest.noarch.rpm
RUN dnf --disablerepo AppStream -y install postgresql12
#RUN yum -y install https://download.postgresql.org/pub/repos/yum/reporpms/EL-8-x86_64/pgdg-redhat-repo-latest.noarch.rpm
#RUN yum -y --nogpgcheck install postgresql13

COPY gretl /usr/local/bin/
COPY __jars4image /home/gradle/libs/
#COPY __jars4image/ilivalidator-custom-functions-*.jar /home/gradle/plugins/ilivalidator-custom-functions.jar

COPY init.gradle /home/gradle/

RUN mkdir -p ~/.gradle && echo "org.gradle.daemon=false" >> ~/.gradle/gradle.properties \
    chown -R 1001:0 /home/gradle && \
    chmod -R g+rw /home/gradle && \
    ls -la /usr/local/bin/  && \
    ls -la /home/gradle && \
    ls -la /home/gradle/libs

USER 1001
