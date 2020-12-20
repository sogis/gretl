FROM openshift/jenkins-slave-maven-centos7

USER root

#RUN yum -y install https://download.postgresql.org/pub/repos/yum/reporpms/EL-7-x86_64/pgdg-redhat-repo-latest.noarch.rpm
#RUN yum -y --nogpgcheck install postgresql10

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
