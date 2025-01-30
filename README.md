![Build Status](https://github.com/sogis/gretl/actions/workflows/gretl.yml/badge.svg)

# GRETL

The [Gradle](http://www.gradle.org) _GRETL_ plugin extends Gradle for use as a sql-centric (geo)data ETL. GRETL = Gradle ETL.

## Manual

The german reference documentation can be found [here](https://gretl.app/reference.html).

For more insights (developing, runtimes) see here: [docs/dev/](docs/dev/index.md)

## License

_GRETL_ is licensed under the [MIT License](LICENSE).

## Status

_GRETL_ is in stable state.

## System requirements

For the current version of _GRETL_, you will need a JRE (Java Runtime Environment) installed on your system, version 11  and Gradle, version 7.6x. For convenience use the gradle wrapper. It may run with newer versions too.

## Subprojects

The _GRETL_ repository is organized as Gradle multi-project:

* `gretl`: _GRETL_ source code with unit tests _and_ integration tests.
* `runtimeImage`: Subproject for building the _GRETL_ runtime (docker) image. The docker image is tested against the integration tests, too.
