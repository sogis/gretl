#!/bin/bash

# use like this:
# start-gretl.sh --job_directory /home/gretl --task_name gradleTaskName -Pparam1=1 -Pparam2=2

task_parameter=()
log_level=""

while [ $# -gt 0 ]; do
    case "$1" in
        --job_directory|--task_name)
            v="${1#--}"
            declare "$v=$2"
            shift 2
            ;;
        --info|--debug)
            log_level="$1"
            shift
            ;;
        -P*)
            task_parameter+=("$1")
            shift
            ;;
        *)
            shift
            ;;
    esac
done

declare gretl_cmd="gretl $log_level $task_name ${task_parameter[@]}"

echo "===================================================================================="
echo "Starts the GRETL runtime to execute the given GRETL job"
echo "task name: $task_name"
echo "job directory:"
echo "$job_directory"
echo "task_parameter: ${task_parameter[@]}"
echo "gretl_cmd: $gretl_cmd"
echo "===================================================================================="

# special run configuration for jenkins-slave based image:
# 1. use a shell as entry point
# 2. mount job directory as volume
# 3. run as current user to avoid permission problems on generated .gradle directory
# 4. gretl-runtime image with tag latest
# 5. executed commands seperated by semicolon:
#    a. jenkins jnlp client
#    b. change to project directory
#    c. run gradle with given task and parameter using init script from image

docker run -i --rm \
    --entrypoint="/bin/sh" \
    --network="host" \
    -v "$job_directory":/home/gradle/project \
    -v "$HOME"/gradlecache:/home/gradle/.gradle/caches \
    --user $UID \
    sogis/gretl:test "-c" \
        "/usr/local/bin/run-jnlp-client > /dev/null 2>&1;cd /home/gradle/project;$gretl_cmd"
