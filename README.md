# gretl

The [Gradle](http://www.gradle.org) gretl plugin extends gradle for use as a sql-centric (geo)data etl. gretl = gradle etl.

## Manual

A german user manual can be found here: [docs/user/](docs/user/index.md) 

## Licencse

_GRETL_ is licensed under the [MIT License](LICENSE).

## Status

_GRETL_ is in stable state.

## System requirements

For the current version of _GRETL_, you will need a JRE (Java Runtime Environment) installed on your system, version 1.8 or later and gradle, version 3.4 or later.
For convenience use the gradle wrapper.

## Subprojects

The _GRETL_ repository is organized as Gradle multi-project:

* `gretl`: _GRETL_ source code with unit tests _and_ integration tests.
* `runtimeImage`: Subproject for building the _GRETL_ runtime (docker) image. The docker image is tested against the integration tests, too.

## Testing

### Unit tests
Unit tests with "heavy" dependencies like PostgreSQL are categorized (`ch.so.agi.gretl.testutil.DbTest`) and can be run with `./gradlew gretl:dbTest`. This will manage the PostgreSQL database with the [https://www.testcontainers.org/](Testcontainers framework). Testcontainers start a docker container before every test method or every test class.

### Integration tests
The integrations are used for testing the resulting Jar file (`jarTest`) and the Docker image (`imageTest`). 

Since the integration tests are not done with the Gradle TestKit framework, the resulting Jar has to be deployed to the local maven repository (`./gradlew clean gretl:build gretl:publishPluginMavenPublicationToMavenLocal`). Then the Gradle build jobs (= integration tests) are run from a Java class as an external process. The jobs share all the same `init.gradle` that defines the maven repositories. Since _GRETL_ is not published in any other than the local maven repository  (except as _plugin_ (!= raw jar) in the Gradle plugin repo), it should really always use the local deployed one. With `classpath 'ch.so.agi:gretl:latest.integration'` it will use the latest anything - snapshot or release, whatever it finds newer.

The Docker image tests are done very similar. First the Docker image will be build with a shell script (TODO: with Gradle?). This build process will copy everything _GRETL_ needs into the image. This includes all the dependencies of _GRETL_, the _GRETL_ jar itself and any 3rd party plugin you want. The Docker image should be as offline capable as possible. The `start-gretl.sh` that will be used to start the Docker container is slightly different to the `start-gretl.sh` from `sogis/gretljobs` repository. The `sogis/gretljobs` one is more sophisticated.

## Release management / Versioning

It uses a simple release management and versioning mechanism: Local builds are tagged as `1.0.LOCALBUILD`. Builds on Travis or Jenkins will append the build number, e.g. `1.0.48`. Major version will be increased after "major" changes. After every commit to the repository a docker image will be build and pushed to `hub.docker.com`. It will be tagged as `latest` and with the build number (`1.0.48`).

## Jenkins (CI/CD)
For a working github webhook one have to choose the content type `application/x-www-form-urlencoded` and not `application/json`. And do not forget the trailing `/`.