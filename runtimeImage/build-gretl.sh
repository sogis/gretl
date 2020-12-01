#!/bin/bash

echo "======================================================================="
echo "Build Docker container for GRETL runtime"
echo "Uses the following container definition:"
echo "https://github.com/openshift/jenkins/blob/master/slave-maven/Dockerfile"
echo "======================================================================="


../gradlew stageJars #copy all jar dependencies to __jars4image

# Set the image labels to the given shell params or sensitive defaults

githash=$1
if [ "x$githash" = "x" ]; then
    githash='localbuild'
fi

buildident=$2
if [ "x$buildident" = "x" ]; then
    buildident='localbuild'
fi

build_timestamp=$(date '+%Y-%m-%d_%H:%M:%S')

docker build \
    --no-cache --force-rm -t sogis/gretl-runtime:2.0.$buildident \
    --label gretl.created=$build_timestamp --label gretl.git_commit=$githash --label gretl.travis_build=2.1.$buildident \
    -f gretl/Dockerfile gretl

docker tag sogis/gretl-runtime:2.0.$buildident sogis/gretl-runtime:latest

rm gretl/__jars4image/*

# look into the container:
# docker run -it --entrypoint=/bin/sh sogis/gretl-runtime:$buildident

