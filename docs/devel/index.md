# GRETL-Entwickler-Handbuch

## 3rd party plugins

Um ein offline-fähiges Docker-Image zu erhalten, müssen sämtliche Abhängigkeiten in das Image kopiert werden.
Dazu gehören auch allenfalls benötigte Fremdplugins (z.B. `gradle-download-task`).
Dazu muss in der Datei `runtimeImage/gretl/build.gradle` das benötigte Repository angegeben werden
(in der Regel: `https://plugins.gradle.org/m2/`) und als Compile-Dependency das Plugin selber
(z.B. `compile "de.undercouch:gradle-download-task:3.4.1"`).

Beim Herstellen des Images wird am Ende der Inhalt des Verzeichnisses `/home/gradle/libs` ausgegeben und man kann verifizieren,
ob die zusätzlichen Bibliotheken im Image vorhanden sind.

## Lokale Entwicklung

### Build
Gradle Projekt bauen, im  Root-Ordner:

```
./gradlew build
```

Dabei werden die Klassen kompiliert, getestet und das Gretl (Gradle Plugin) jar erstellt.

### Test mit Datenbank (PostgreSQL / postgis)
Dazu wird zuerst eine Test-Datenbank in Docker gestartet:

```
cd tooling
./start-test-database-pg.sh
```

Tests ausführen, im Root Ordner:

```
./gradlew -Pgretltest_dburi_pg=jdbc:postgresql:gretl -Ddbusr=postgres -Ddbpwd=admin1234 build gretl:dbTest
```

#### Integration Tests
Es gibt Integration Tests, welche das Jar oder das Docker Image testen.

Die Testklassen sind hier zu finden [gretl/inttest/src/testIntegration](../../gretl/inttest/src/testIntegration)

Diese Testklassen führen Gretl Jobs aus. Die Job Konfigurationen liegen im [gretl/inttest/jobs](../../gretl/inttest/jobs) Ordner.
Dazu verwenden sie den [Gradle Wrapper](../../gretl/inttest/gradlew), welcher auf den selben Jars agiert wie der Wrapper im Root Ordner.

##### Jar basiert
Zuerst müssen alle Dependencies ins lokale Maven Repo publiziert werden; im Root Ordner:

```
./gradlew -Pgretltest_dburi_pg=jdbc:postgresql:gretl -Ddbusr=postgres -Ddbpwd=admin1234 build gretl:publishToMavenLocal
```

Integration Tests ausführen, im Root Ordner (brauchen die selbe DB wie die Datenbank Tests):

```    
./gradlew -Pgretltest_dburi_pg=jdbc:postgresql:gretl -Ddbusr=postgres -Ddbpwd=admin1234 --project-dir gretl/inttest testJar
```
