# GRETL runtime
Docker image based on [Jenkins maven slave](https://hub.docker.com/r/openshift/jenkins-slave-maven-centos7/)
This base image contains Java and Gradle.

The GRETL plugin with all dependencies is also packed into the image.

The image was chosen because the GRETL runtime will be run as a Jenkins slave.

## Local build and execution

### build
The script ```build-gretl.sh``` builds the runtime as Docker image with the name **gretl-runtime**.

### run
The script ```start-gretl.sh``` runs the image *gretl-runtime*.
Therefore the image has to be built before, see the build section.

Script execution:  
```start-gretl.sh --job_directory /home/gretl --task_name gradleTaskName -Pparam1=1 -Pparam2=2```

The *n* parameter are passed directly to the gradle task execution.

### test
The script ```test-gretl.sh``` holds job name and parameter to test the runtime with the *afu_altlasten_pub* job.

Be sure to change the path of the job files according to the location on your machine.

Further test scripts use GRETL jobs from the *gretl/inttest/jobs* directory.

## Build pipeline
The build automation is done by [Travis CI](https://www.travis-ci.org/) on GitHub.

The pipeline is configured with the [travis.yml](../.travis.yml) file inside the root directory.
