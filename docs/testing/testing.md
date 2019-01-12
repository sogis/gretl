Die Test-Pyramide ist wie folgt definiert

0) Unit Tests
1) Component Tests
2) Integration Tests
3) System Tests

System Tests rufen den Jenkins auf, welcher die GRETL Runtime aufruft.

Integration Tests rufen Gradle auf und benutzen das gebuildete Jar vom
Projekt (build/libs/*.jar)

Component Tests testen einen ganzen GRETL Task.

Unit Tests testen Teile von GRETL Tasks.


Im Projekt sind Unit- und Component-Tests in src/test zu finden.
Die Integration-Tests sind im inttest/src/testIntegration zu finden.
Die System-Tests sind im inttest/src/testSystem zu finden.

Unit Tests
-------------------

Component Tests
-------------------

Integration Tests
-------------------
Im inttest/jobs Directory sind die Gradle build-Skripts für einzelne Testfälle und im
inttest/src/testIntegration die Test Klassen dazu.

Lokal können die Jobs direkt (aber ohne Validierung der Ergebnisse!) ausgeführt werden mit:
"cd inttest/jobs/iliValidator"
"../../gradlew --init-script ../init.gradle --project-dir jobPath
parameter..."

Tests (also inkl. Validierung) werden im inttest Projekt ausgeführt.
"cd inttest"
"./gradlew testIntegration"


System Tests
-------------------

