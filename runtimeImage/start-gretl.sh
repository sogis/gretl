#!/usr/bin/env bash

# use like this:
# ./start-gretl.sh --docker-image sogis/gretl-runtime:latest [--docker-network NETWORK] --job-directory $PWD/jobname [taskName...] [--option-name...]
# [--docker-network NETWORK] may be used for connecting the container to an already existing docker network
# [--option-name...] takes any Gradle option, including project properties
# (e.g. -Pmyprop=myvalue) and system properties (e.g. -Dmyprop=myvalue).
# See https://docs.gradle.org/current/userguide/command_line_interface.html
# or the output of gradle -h for available options.

gradle_options=()

while [ $# -gt 0 ]; do
    if [[ $1 == "--docker-image" ]]; then
        declare docker_image="$2"
        shift
    elif [[ $1 == "--docker-network" ]]; then
        declare docker_network="$2"
        shift
    elif [[ $1 == "--job-directory" ]]; then
        declare job_directory="$2"
        shift
    else
        gradle_options+=($1)
   fi
  shift
done

# Pass any environment variable starting with ORG_GRADLE_PROJECT_
# to the container. This makes it available as a variable in the Gradle script.
# Example: The value of ORG_GRADLE_PROJECT_dbUriPub can be accessed
# with the variable dbUriPub in the Gradle script.
for envvar in $(env | grep ORG_GRADLE_PROJECT_); do
    envvars_string+="--env ${envvar} "
done

# Build a string containing the --network ... option if the --docker-network option has been set
if [[ -v docker_network ]]; then
    declare network_string="--network ${docker_network}"
fi

# For accessing the "GRETL share", use the gretlShare variable.

declare gretl_cmd="gretl ${gradle_options[@]} -PgretlShare=/tmp/gretl-share"

echo "======================================================="
echo "Starts the GRETL runtime to execute the given GRETL job"
echo "Docker Image: $docker_image"
echo "Docker network: $docker_network"
echo "job directory: $job_directory"
echo "Gradle options: ${gradle_options[@]}"
echo "gretl_cmd: $gretl_cmd"
echo "======================================================="

# Create a directory that is going to be mounted as the "GRETL share"
mkdir -p /tmp/gretl-share

# special run configuration for jenkins-slave based image:
# 1. use a shell as entry point
# 2. mount job directory as volume
# 3. mount /tmp directory as GRETL share
# 4. Pass all environment variables starting with ORG_GRADLE_PROJECT_ to the container
# 5. run as current user to avoid permission problems on generated .gradle directory
# 6. connect container to a specific docker network if specified by the user
# 7. run gretl-runtime image passed by the user
# 8. executed commands seperated by semicolon:
#    a. jenkins jnlp client
#    b. change to project directory
#    c. run gradle with given task and parameter using init script from image

docker run -i --rm --name gretl \
    --entrypoint="//bin/sh" \
    -v "/${job_directory}":/home/gradle/project \
    -v //tmp/gretl-share:/tmp/gretl-share \
     ${envvars_string} \
    --user $UID \
    ${network_string} \
    "$docker_image" "-c" \
        "/usr/local/bin/run-jnlp-client > /dev/null 2>&1;cd /home/gradle/project;$gretl_cmd"