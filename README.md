![Build Status](https://github.com/sogis/gretl/actions/workflows/gretl.yml/badge.svg)

# gretl

The [Gradle](http://www.gradle.org) gretl plugin extends gradle for use as a sql-centric (geo)data etl. gretl = gradle etl.

## Manual

A german user manual can be found here: [docs/user/](docs/user/index.md) 

The Publisher task is documented here: [Publisher](docs/user/Publisher.md) 

## Licencse

_GRETL_ is licensed under the [MIT License](LICENSE).

## Status

_GRETL_ is in stable state.

## System requirements

For the current version of _GRETL_, you will need a JRE (Java Runtime Environment) installed on your system, version 1.8 or later and Gradle, version 5.1.1 or later.
For convenience use the gradle wrapper.

## Subprojects

The _GRETL_ repository is organized as Gradle multi-project:

* `gretl`: _GRETL_ source code with unit tests _and_ integration tests.
* `runtimeImage`: Subproject for building the _GRETL_ runtime (docker) image. The docker image is tested against the integration tests, too.

## Oracle JDBC
There are still signs and wonders taking place: Since fall 2019 the Oracle JDBC library can be found on maven central. Oracle database support is now straight forward.

## Testing

```
./gradlew clean gretl:classes 
./gradlew gretl:test gretl:dbTest 
./gradlew gretl:s3Test -Ds3AccessKey=XXXXXXX -Ds3SecretKey=YYYYYYY -Ds3BucketName=ch.so.agi.gretl.test
./gradlew gretl:build gretl:publishPluginMavenPublicationToMavenLocal gretl:publishGretlPluginPluginMarkerMavenPublicationToMavenLocal -x test
./gradlew stageJars
cd runtimeImage/gretl
docker build -t sogis/gretl .
cd ../..
./gradlew gretl:jarTest 
./gradlew gretl:jarS3Test -Ds3AccessKey=XXXXXXX -Ds3SecretKey=YYYYYYY -Ds3BucketName=ch.so.agi.gretl.test
./gradlew gretl:imageTest 
./gradlew gretl:imageS3Test -Ds3AccessKey=XXXXXXX -Ds3SecretKey=YYYYYYY -Ds3BucketName=ch.so.agi.gretl.test
```

```
./gradlew clean gretl:build gretl:publishPluginMavenPublicationToMavenLocal gretl:jarTest -x test
```

E.g. if you want to test only the jar and only one test:
```
./gradlew clean gretl:build gretl:publishPluginMavenPublicationToMavenLocal gretl:jarTest -x test --tests ch.so.agi.gretl.jobs.Av2chTest.transformation_Ok

```

If you write a new custom task and the integration test thinks your new task type is not available, delete the artifacts in your local maven repository once. It seems that after this clean up, changes in the code will be deployed to the local maven repo. Not sure what the problem is. Maybe an deployed version with additional plugin meta data.

If you want to do some further testing with standalone jobs or use the plugin on your local machine and use the new plugin dsl syntax, you need to deploy the plugin as "plugin":

```
./gradlew gretl:publishGretlPluginPluginMarkerMavenPublicationToMavenLocal
```

Debugging the Docker image tests can be harder than debugging unit and jar tests. The `docker run` / `start-gretl.sh` command is printed on the console when the test is running. Use this command to run the test in the console manually. Most probably you will get more information.

### Unit tests

Unit tests with "heavy" dependencies like PostgreSQL are categorized (`ch.so.agi.gretl.testutil.TestTags.DB_TEST`) and
can be run with `./gradlew gretl:dbTest`. This will manage the PostgreSQL database with
the [Testcontainers framework](https://www.testcontainers.org) . Testcontainers start a docker container before every
test method or every test class.

### Integration tests
The integrations are used for testing the resulting Jar file (`jarTest`) and the Docker image (`imageTest`). 

Since the integration tests are not done with the Gradle TestKit framework, the resulting Jar has to be deployed to the local maven repository (`./gradlew clean gretl:build gretl:publishPluginMavenPublicationToMavenLocal`). Then the Gradle build jobs (= integration tests) are run from a Java class as an external process. The jobs share all the same `init.gradle` that defines the maven repositories. Since _GRETL_ is not published in any other than the local maven repository  (except as _plugin_ (!= raw jar) in the Gradle plugin repo), it should really always use the local deployed one. With `classpath 'ch.so.agi:gretl:latest.integration'` it will use the latest anything - snapshot or release, whatever it finds newer.

The Docker image tests are done very similar. First the Docker image will be build with a shell script (TODO: with Gradle?). This build process will copy everything _GRETL_ needs into the image. This includes all the dependencies of _GRETL_, the _GRETL_ jar itself and any 3rd party plugin you want. The Docker image should be as offline capable as possible. The `start-gretl.sh` that will be used to start the Docker container is slightly different to the `start-gretl.sh` from `sogis/gretljobs` repository. The `sogis/gretljobs` one is more sophisticated. If you use branches, it will not create a "latest" image. Hence you will have to use a tagged version in `start-gretl.sh`, e.g. 2.2.

If you use a Jenkins-Docker-Image for your CI/CD pipeline you will probably run into the "Docker-in-Docker" issue when doing the Docker image integration tests. It will not find the job directory you try to mount with the docker run command. Therefor you can simple create a symbolic link on the host machine, e.g. `ln -s /opt/jenkins_home /var/jenkins_home`.

### S3 tests
Instead of using a mocking library we to some real world testing with S3. Therefore a bucket must exist and the access key and secret key must be known. The keys used for CI belong to the user `gretl` (group: `gretl-group`, policy: `gretl-s3`).

### Github Action
The pipeline cannot run parallel since some tests write to S3 buckets. If something goes wrong they need to be cleaned manually (needs to be fixed...).

## Release management / Versioning

It uses a simple release management and versioning mechanism: Local builds are tagged as `2.2.LOCALBUILD`. Builds on Github Action will append the build number, e.g. `2.2.230`. Major version will be increased after "major" changes. After every commit to the repository a docker image will be build and pushed to `hub.docker.com`. It will be tagged as `latest`, with the build number (`2.2.230`) and with major and minor version number (`2` and `2.2`).

You have to bump the version number in three files:

- versioning.gradle
- runtimeImage/build.gradle
- .github/workflows/gretl.yml

