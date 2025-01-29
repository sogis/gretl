# Developing

## Interne Struktur

### Versionierung

Die Version für _GRETL_ wird in der Gradle-Datei _versioning.gradle_ definiert. Es wird nur die Major- und Minor-Version gesetzt. Die Patch-Version soll durch die Pipeline gesetzt werden. Falls lokal gebuildet wird, ist die Patch-Version "LOCALBUILD".

Ein Minorupdate erfolgte bisher, wenn wir Majorupdates der ilitools durchführten. Oder ausnahmsweisen, wenn Minorupdates der ilitools nicht in jedem Fall rückwärtskompatibel sind.

### Projektstruktur

Es gibt die beiden Unterprojekte _gretl_ und _runtimeImage_. Ersteres ist für die eigentliche Entwicklung und das _runtimeImage_-Unterprojekt dient dem Herstellen des Dockerimages.

### Gradle
_GRETL_ ist ein Gradle Plugin und entsprechend benötigt es die Gradle API. Wenn man Gradle im Projekt updatet, bedeutet dies, dass auch das Plugin mit dieser API-Version entwickelt wird. Das Dockerimage ist entsprechend abzugleichen. Gradle wird im Projekt wie folgt upgedatet:

```
./gradlew wrapper --gradle-version 8.10.2
```

Das Dockerimage ist an folgenden Stellen anzupassen:

```
ENV GRADLE_VERSION 8.10.2
ARG GRADLE_DOWNLOAD_SHA256=31c55713e40233a8303827ceb42ca48a47267a0ad4bab9177123121e71524c26
```

Ein Update sollte nicht leichtfertig vorgenommen werden, da es verschiedene Auswirkungen haben kann. Andererseits läuft nicht jede Gradle-Version mit jeder neusten Java-Version (was für uns momentan nicht relevant ist).

Die momentan eingesetzte Version 7 funktioniert bis und mit Java 19. Die Version 8 braucht beim allerersten Durchlauf nochmals länger (siehe Kapitel "Running") und loggt auch mehr im Lifecycle-Level.

### Dependencies

_GRETL_ verwendet als Schnittstellenwerkzeug sehr viele Abhängigkeiten, die wiederum Abhängigkeiten verwenden. Aus diesem Grund kann es zu Versionskonflikten führen. Diese können mittels `./gradlew gretl:dependencies > dependencies.log` herausgefunden werden. Die Konflikte kann man auflösen, indem man z.B. gewissen transitive Abhängigkeiten einer Abhängigkeit exkludiert oder die Version explizit forsiert. Für die ilitools werden die Versionen bewusst forciert. Versionskonflikte können auch das Dockerimage speziell betreffen, da wir dort zusätzlich Gradle-Plugins reinkopieren. In den Ordner _stageJars_ werden sämtliche Jar-Dateien kopiert, was ebenfalls einen guten Überblick gibt. Eine weitere Variante zum Auflösen des Versionskonfliktes von transitiven Abhängigkeiten ist das Verwenden anderer Versionen der Bibliotheken.

### Steps vs Tasks

Für jeden selber programmierten Customtask gibt es (in der Regel) auch einen dazugehörigen Step. Sämtliche Businesslogik sollte im Step programmiert werden (und ohne Gradle-Abhänigkeiten auskommen). Der Task ist nur die öffentliche API ohne viel mehr Logik.

### Gradle-Annotationen

Gradle bietet verschiedene Annotationen für die Task Properties an. So können Properties als optional gekennzeichnet werden oder explizit als Input-Datei. Wird etwas als Input oder Output annotiert, berücksichtig Gradle diese Properties bei der Evaluation, ob ein Task up-to-date ist. Hat sich z.B. ein INTERLIS-Datei nicht geändert, muss sie ja nicht nochmals validiert werden. Wir haben ganz geklärt, ob wir diese Verhalten wünschen. Aus diesem Grund gibt es auch Custom-Tasks, bei denen die Properties mit `Internal` annotiert sind. Diese Properties werden bei der Evaluierung nicht berücksichtigt. Bei unseren Gretljobs haben wird das so gelöst, dass wir den dazu benötigten Cache explizit im Container behalten und nicht lokal speichern. Ein anderes Dockerimage kann das jedoch anders handhaben. Ebenso beim Einsatz des Gradle-Plugins direkt. Hier dient es sich an, dass mit mit `--rerun-tasks` das Ausführen der Tasks forciert. Diese Option wird auch bei den Integrationstests verwendet.

### Configuring Tasks Lazily

... todo ...

## Testing

Es gibt Unit- und Integrationstests. Die Unittests dienen zum Testen der Steps (und einigen anderen Helfer-Klassen). Es gibt zwei zusätzliche Testgruppen, die separat aufgerufen werden müssen: `dbTest` und `s3Test`. 

```
./gradlew gretl:test gretl:dbTest gretl:s3Test
```

Beide benötigten Infrastruktur, die wir via Testcontainers (PostgreSQL und Localstack) bereitstellen. Es finden bereits auch in den "normalen" Tests Datenbanktests statt. In diesem Fall wird eine dateibasierte Datenbank verwendet.

Die Integrationstest sind Tests mit richtigen _build.gradle_-Dateien und simulieren so die fertige Software. Sie werden zweifach durchgeführt. Zuerst als Gradle-Plugin ("Java pur") und anschliessend werden alle Jobs nochmals mit dem Dockercontainer geprüft. Aus historischen Gründen (damals kein Localstack verwendet) werden die S3-Tests speziell getaggt.

Nach erfolgreichen Unittests wird das Plugin erstellt und in das lokale Maven-Repository (für das Dockerimage) publiziert:

```
./gradlew gretl:build gretl:publishPluginMavenPublicationToMavenLocal -x test
```

Es folgen die Gradle-Plugin-Tests:

```
./gradlew gretl:jarTest gretl:jarS3Test
```

Nach dem Builden des Dockerimages

```
./gradlew runtimeImage:buildImage
```

folgen die Tests mit dem Dockercontainer:

```
./gradlew gretl:imageTest gretl:imageS3Test
```

Die Tests können - wie erwähnt - dank Testcontainers komplett lokal durchgeführt werden. Falls in der Github Action Test fehlschlagen, steht der Report zur Verfügung.

Das Testen des Gradle-Plugins wird mit einem `GradleRunner` gemacht. Hier ist die Herausforderung, dass der Classpath dem geforkten Java-Prozess (der den Gradle-Job durchführt) bekannt gemacht wird. Nicht ganz nachvollziehbar ist, warum die Lösung mittels `.withPluginClasspath()` nicht funktioniert. Aus diesem Grund wird der Classpath in eine Datei geschrieben, die wiederum als Grundlage für die Dependencies in der verwendeteten _init.gradle_-Datei dient. Die Jobs werden mit `--rerun-tasks` ausgeführt. Dieses Argument ist momentan hardcodiert. Damit wird sichergestellt, dass immer alle Tasks (unabhängig der Annotationen) ausgeführt werden.

## Building

### Gradle-Plugin

Das Gradle-Plugin kann problemlos mit Boardmitteln erstellt und publiziert (https://plugins.gradle.org) werden.

### Dockerimage

Ziel war es von Beginn weg eine Dockerimage zu erstellen, das sämtliche Abhängigkeiten beinhalten ("offline-fähig"). Das wird erreicht, indem alle Abhängigkeiten zuerst mittel Gradle-Job in den Ordner _jars4image_ kopiert werden (inkl. zusätzlichen Gradle-Plugins). 

Die Docker-Befehle sind als Exec-Aufruf ebenfalls im Gradle-Job. Einzig das Einloggen in eine Docker-Registry und das Vorbereiten der buildx-Umgebung wird in der Github Action Konfiguration gemacht (oder muss lokal einmalig gemacht werden). Für die Integrations-Image-Tests wird ein Dockerimage mit dem Tag "test" hergestellt. Erst ganz am Schluss wird der Multi-Arch-Dockerbuild gemacht. Grund dafür ist, dass dieser Build das passende Image nicht in die lokale Registry publiziert und somit auch nicht zur Verfügung steht und vor allem das Publizieren in die externen Registry ist ohne die Build-und-Push-Action sehr mühselig (Manifest-Files etc. pp.).

Für DuckDB werden auch einige Extensions in das Dockerimage gebrannt. Dies ist notwendig, da sonst bei jedem Run die Extension neu installiert (und heruntergeladen) werden müsste. Dazu werden in der _stage-duckdb-extensions.gradle_-Datei direkt via JDBC die notwendigen Installationsbefehle ausgeführt. Achtung: Die DuckDB-Version muss hier manuell angepasst werden.

## Running

Gradle cached verschiedene Informationen. Gewisses Verhalten ist von uns eher gewollt als anderes: 

Beim Einsatz des Gradle-Plugins (ohne Dockerimage) kann es sein, dass gewisse Tasks nicht durchgeführt werden, weil sich der Input (z.B. eine INTERLIS-Datei) nicht geändert hat. Die Ausführung des Tasks kann man mit `--rerun-tasks` forcieren. Bei unseren Gretljobs ist dies nicht notwendig, weil wir den Projektcache explizit im Container behalten und nicht lokal mounten (`--project-cache-dir=/home/gradle/projectcache`). 

Beim lokalen Entwickeln ist es jedoch wünschenswert, wenn der Ordner `/home/gradle/.gradle/caches` gemounted wird, da die Ausführungszeiten (nach der allerersten) massiv schneller werden. Die Integrationstests mit dem Dockerimage werden ebenfalls so durchgeführt, was die Laufzeit auf einen Drittel reduziert (siehe _start-gretl.sh_ und Pipeline "mkdir"). Es scheint auch, dass mit jeder Gradle Major-Version immer mehr beim allerersten Durchlauf evaluiert wird. Das Mounten dieses Caches war mit Gradle Version 5.x noch nicht nötig (resp. war praktisch nicht spürbar). Mit Gradle Version 8.x geht es nochmals länger und es wird auch mehr geloggt.

Es gibt relativ neu (mit Version 7 immer noch experimental) den Konfigurationscache. Dieser müsste freigeschaltet werden, bringt nach einigen Tests jedoch geschwindigkeitsmässig nicht viel. Zudem müssten Tasks umgeschrieben werden, weil nicht mehr auf das Project-Objekt in gleicher, einfacher Form zugegriffen werden kann.

Gradle-Daemons können mit dem Dockerimage nicht persistiert werden (weil der JVM-Prozess mit dem Container stirbt).

## Dokumentation

TODO ...

## Varia

### Eclipse

Since `java.xml` is part of the JDK but is also a dependency of the Gradle API (which is automatically added by the `java-gradle-plugin`) you will get the famous `The package javax.xml.transform.stream is accessible from more than one module: ,java.xml` errors. Excluding `xml-apis` with `all*.exclude group: 'xml-apis'` should be done but will not work for the Gradle API. Workaround: 

- Clone the repository
- Run `./gradlew eclipse`
- Add `org.eclipse.jdt.core.compiler.ignoreUnnamedModuleForSplitPackage=enabled` to _gretl/.settings/org.eclipse.jdt.core.prefs_.

