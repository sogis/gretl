# Referenzdokumentation


TEST

Das Datenmanagement-Tool *GRETL* ist ein Werkzeug, das für Datenimports,
Datenumbauten (Modellumbau) und Datenexports eingesetzt wird. *GRETL*
führt Jobs aus, wobei ein Job aus mehreren atomaren Tasks besteht. Damit
ein Job als vollständig ausgeführt gilt, muss jeder zum Job gehörende
Task vollständig ausgeführt worden sein. Schlägt ein Task fehl, gilt
auch der Job als fehlgeschlagen.

Ein Job besteht aus einem oder mehreren Tasks, die gemäss einem
gerichteten Graphen (Directed Acyclic Graph; DAG) miteinander verknüpft
sind.

Ein Job kann aus z.B. aus einer linearen Kette von Tasks bestehen:

``` bash
Task 1 – Task 2 – Task 3 – Task n
```

Beispiel: Datenimport aus INTERLIS-Datei – Datenumbau – Datenexport nach
Shapefile.

Ein Job kann sich nach einem Task aber auch auf zwei oder mehr
verschiedene weitere Tasks verzweigen:

``` bash
       – Task 2 – Task 3 – Task n
Task 1 –
       – Task 4 – Task 5 – Task m
```

Beispiel: Datenimport aus INTERLIS-Datei – Datenumbau in Zielschema 1
und ein zweiter Datenumbau in Zielschema 2.

Es ist auch möglich, dass zuerst zwei oder mehr Tasks unabhängig
voneinander ausgeführt werden müssen, bevor ein einzelner weiterer Task
ausgeführt wird.

``` bash
Task 1 –
       – Task 3 – Task 4 – Task n
Task 2 –
```

Die Tasks eines Jobs werden per Konfigurationsfile konfiguriert.

## Systemanforderungen

Für die aktuelle GRETL-Version wird Java 11 und *Gradle* 7.6 benötigt.

## Installation

*GRETL* selbst muss als Gradle-Plugin nicht explizit installiert werden,
sondern wird dynamisch durch das Internet bezogen.

## Kleines Beispiel

Erstellen Sie in einem neuen Verzeichnis `gretldemo` eine neue Datei
`build.gradle`:

``` groovy
import ch.so.agi.gretl.tasks.*
import ch.so.agi.gretl.api.*

apply plugin: 'ch.so.agi.gretl'

buildscript {
    repositories {
        maven { url "https://jars.interlis.ch" }
        maven { url "https://repo.osgeo.org/repository/release/" }
        maven { url "https://plugins.gradle.org/m2/" }
        maven { url "https://s01.oss.sonatype.org/service/local/repositories/releases/content/" }
        maven { url "https://s01.oss.sonatype.org/service/local/repositories/snapshots/content/" }
        mavenCentral()
    }
    dependencies {
        classpath group: 'ch.so.agi', name: 'gretl',  version: '3.0.+'
    }
}

defaultTasks 'validate'

task validate(type: IliValidator){
    dataFiles = ["BeispielA.xtf"]
}
```

Die Datei `build.gradle` ist die Job-Konfiguration. Dieser kleine
Beispiel-Job besteht nur aus einem einzigen Task: `validate`.

Erstellen Sie nun noch die Datei `BeispielA.xtf` (damit danach der Job
erfolgreich ausgeführt werden kann).

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<TRANSFER xmlns="http://www.interlis.ch/INTERLIS2.3">
    <HEADERSECTION SENDER="gretldemo" VERSION="2.3">
    </HEADERSECTION>
    <DATASECTION>
        <OeREBKRMtrsfr_V1_1.Transferstruktur BID="B01">
        </OeREBKRMtrsfr_V1_1.Transferstruktur>
    </DATASECTION>
</TRANSFER>
```

Um den Job auszuführen, wechseln Sie ins Verzeichnis mit der
Job-Konfiguration, und geben da das Kommando `gradle` ohne Argument ein:

``` bash
cd gretldemo
gradle
```

`BUILD SUCCESSFUL` zeigt an, dass der Job (die Validierung der Datei
`BeispielA.xtf`) erfolgreich ausgeführt wurde.

Um dieselbe Job-Konfiguration für verschiedene Datensätze verwenden zu
können, muss es parametrisierbar sein. Die Jobs/Tasks können so
generisch konfiguriert werden, dass dieselbe Konfiguration z.B. für
Daten aus verschiedenen Gemeinden benutzt werden kann. Parameter für die
Job Konfiguration können z.B. mittels gradle-Properties ([Gradle
properties and system
properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_properties_and_system_properties))
dem Job mitgegeben werden, also z.B.

``` bash
cd gretldemo
gradle -Pdataset=Olten
```

## Ausführen

Um *GRETL* auszuführen, geben Sie auf der Kommandozeile folgendes
Kommando ein (wobei `jobfolder` der absolute Pfad zu ihrem Verzeichnis
mit der Job Konfiguration ist.)

``` bash
gradle --project-dir jobfolder
```

Alternativ können Sie auch ins Verzeichnis mit der Job Konfiguration
wechseln, und da das Kommando `gradle` ohne Argument verwenden:

``` bash
cd jobfolder
gradle
```

## Tasks

### Av2ch

Transformiert eine INTERLIS1-Transferdatei im kantonalen AV-DM01-Modell
in das Bundesmodell. Unterstützt werden die Sprachen *Deutsch* und
*Italienisch* und der Bezugrahmen *LV95*. Getestet mit Daten aus den
Kantonen Solothurn, Glarus und Tessin. Weitere Informationen sind in der
Basisbibliothek zu finden: <https://github.com/sogis/av2ch>.

Das Bundes-ITF hat denselben Namen wie das Kantons-ITF.

Aufgrund der sehr vielen Logging-Messages einer verwendeten Bibliothek,
wird der `System.err`-Ouput nach `dev/null`
[https://github.com/sogis/av2ch/blob/master/src/main/java/ch/so/agi/av/Av2ch.java#L75](gemappt).

``` groovy
tasks.register('transform', Av2ch) {
    inputFile = file("254900.itf")
    outputDirectory = file("output")
}
```

Es können auch mehrere Dateien gleichzeitig transformiert werden:

``` groovy
tasks.register('transform', Av2ch) {
    inputFile = fileTree(".").matching {
            include"*.itf"
        }
    outputDirectory = file("output")
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| inputFile | `Object` | Zu transformierende ITF-Datei(en). File- oder FileCollection-Objekt. |  nein |
| language | `String` | Sprache des Modelles / der Datei (de, it). Default: de |  ja |
| modeldir | `String` | INTERLIS-Modellrepository. String separiert mit Semikolon (analog ili2db, ilivalidator). |  ja |
| outputDirectory | `Object` | Name des Verzeichnisses in das die zu erstellende Datei geschrieben wird. |  nein |
| zip | `Boolean` | Die zu erstellende Datei wird gezippt. Default: false |  ja |

### Av2geobau

Av2geobau konvertiert eine Interlis-Transferdatei (itf) in eine
DXF-Geobau Datei. Av2geobau funktioniert ohne Datenbank.

Die ITF-Datei muss dem Modell DM01AVCH24LV95D entsprechen. Die Daten
werden nicht validiert.

Die Datenstruktur der DXF-Datei ist im Prinzip sehr einfach: Die
verschiedenen Informationen aus dem Datenmodell DM01 werden in
verschiedene DXF-Layer abgebildet, z.B. die begehbaren LFP1 werden in
den Layer “01111” abgebildet. Oder die Gebäude in den Layer “01211”.

Der Datenumbau ist nicht konfigurierbar.

``` groovy
tasks.register('av2geobau', Av2geobau) {
    itfFiles = "ch_254900.itf"
    dxfDirectory = "./out/"
}
```

Es können auch mehrere Dateien angegeben werden.

``` groovy
tasks.register('av2geobau', Av2geobau) {
    itfFiles = fileTree(".").matching {
        include"*.itf"
    }
    dxfDirectory = "./out/"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| dxfDirectory | `Object` | Verzeichnis, in das die DXF-Dateien gespeichert werden. |  nein |
| isZip | `Boolean` | Die zu erstellende Datei wird gezippt und es werden zusätzliche Dateien (Musterplan, Layerbeschreibung, Hinweise) hinzugefügt. Default: false |  ja |
| itfFiles | `Object` | ITF-Datei, die nach DXF transformiert werden soll. Es können auch mehrere Dateien angegeben werden. File- oder FileCollection-Objekt. |  nein |
| logFile | `Object` | Schreibt die log-Meldungen der Konvertierung in eine Text-Datei. |  ja |
| modeldir | `String` | INTERLIS-Modellrepository. String separiert mit Semikolon (analog ili2db, ilivalidator). |  ja |
| proxy | `String` | Proxy-Server für den Zugriff auf Modell-Repositories. |  ja |
| proxyPort | `Integer` | Proxy-Port für den Zugriff auf Modell-Repositories. |  ja |

### Csv2Excel (incubating)

Konvertiert eine CSV-Datei in eine Excel-Datei (\*.xlsx). Datentypen
werden anhand eines INTERLIS-Modelles eruiert. Fehlt das Modell, wird
alles als Text gespeichert. Die Daten werden vollständig im Speicher
vorgehalten. Falls grosse Dateien geschrieben werden müssen, kann das zu
Problemen führen. Dann müsste die Apache POI SXSSF Implementierung
(Streaming) verwendet werden.

Beispiel:

``` groovy
tasks.register('convertData', Csv2Excel) {
    csvFile = file("./20230124_sap_Gebaeude.csv")
    firstLineIsHeader = true
    valueDelimiter = null
    valueSeparator = ";"
    encoding = "ISO-8859-1";
    models = "SO_HBA_Gebaeude_20230111";
    outputDir = file(".");
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| csvFile | `File` | CSV-Datei, die konvertiert werden soll. |  nein |
| encoding | `String` | Zeichencodierung der CSV-Datei, z.B. `UTF-8`. Default: Systemeinstellung |  ja |
| firstLineIsHeader | `Boolean` | Definiert, ob eine Headerzeile geschrieben werden soll, oder nicht. Default: true |  ja |
| modeldir | `String` | INTERLIS-Modellrepository. String separiert mit Semikolon (analog ili2db, ilivalidator). |  ja |
| models | `String` | INTERLIS-Modell für Definition der Datentypen in der Excel-Datei. |  ja |
| outputDir | `File` | Verzeichnis, in das die Excel-Datei gespeichert wird. Default: Verzeichnis, in dem die CSV-Datei vorliegt. |  ja |
| valueDelimiter | `Character` | Zeichen, das am Anfang und Ende jeden Wertes geschrieben werden soll. Default `"` |  ja |
| valueSeparator | `Character` | Zeichen, das als Trennzeichen zwischen den Werten verwendet werden soll. Default: `,` |  ja |

### CsvExport

Daten aus einer bestehenden Datenbanktabelle werden in eine CSV-Datei
exportiert. Geometriespalten können nicht exportiert werden.

Beispiel:

``` groovy
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('csvexport', CsvExport) {
    database = [db_uri, db_user, db_pass]
    schemaName = "csvexport"
    tableName = "exportdata"
    firstLineIsHeader = true
    attributes = [ "t_id","Aint"]
    dataFile = "data.csv"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| attributes | `String[]` | Spalten der DB-Tabelle, die exportiert werden sollen. Definiert die Reihenfolge der Spalten in der CSV-Datei. Default: alle Spalten |  ja |
| dataFile | `Object` | Name der CSV-Datei, die erstellt werden soll. |  nein |
| database | `Connector` | Datenbank aus der exportiert werden soll. |  nein |
| encoding | `String` | Zeichencodierung der CSV-Datei, z.B. “UTF-8”. Default: Systemeinstellung |  ja |
| firstLineIsHeader | `Boolean` | Definiert, ob eine Headerzeile geschrieben werden soll, oder nicht. Default: true |  ja |
| schemaName | `String` | Name des DB-Schemas, in dem die DB-Tabelle ist. |  ja |
| tableName | `String` | Name der DB-Tabelle, die exportiert werden soll. |  nein |
| valueDelimiter | `Character` | Zeichen, das am Anfang und Ende jeden Wertes geschrieben werden soll. Default `"` |  ja |
| valueSeparator | `Character` | Zeichen, das als Trennzeichen zwischen den Werten verwendet werden soll. Default: `,` |  ja |

### CsvImport

Daten aus einer CSV-Datei werden in eine bestehende Datenbanktabelle
importiert.

Die Tabelle kann weitere Spalten enthalten, die in der CSV-Datei nicht
vorkommen. Sie müssen aber NULLable sein, oder einen Default-Wert
definiert haben.

Geometriepalten können nicht importiert werden.

Die Gross-/Kleinschreibung der CSV-Spaltennamen wird für die Zuordnung
zu den DB-Spalten ignoriert.

Beispiel:

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('csvimport',  CsvImport) {
    database = [db_uri, db_user, db_pass]
    schemaName = "csvimport"
    tableName = "importdata"
    firstLineIsHeader = true
    dataFile = "data1.csv"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| attributes | `String[]` | Spalten der DB-Tabelle, die exportiert werden sollen. Definiert die Reihenfolge der Spalten in der CSV-Datei. Default: alle Spalten |  ja |
| dataFile | `Object` | Name der CSV-Datei, die erstellt werden soll. |  nein |
| database | `Connector` | Datenbank aus der exportiert werden soll. |  nein |
| encoding | `String` | Zeichencodierung der CSV-Datei, z.B. “UTF-8”. Default: Systemeinstellung |  ja |
| firstLineIsHeader | `Boolean` | Definiert, ob eine Headerzeile geschrieben werden soll, oder nicht. Default: true |  ja |
| schemaName | `String` | Name des DB-Schemas, in dem die DB-Tabelle ist. |  ja |
| tableName | `String` | Name der DB-Tabelle, die exportiert werden soll. |  nein |
| valueDelimiter | `Character` | Zeichen, das am Anfang und Ende jeden Wertes geschrieben werden soll. Default `"` |  ja |
| valueSeparator | `Character` | Zeichen, das als Trennzeichen zwischen den Werten verwendet werden soll. Default: `,` |  ja |

### CsvValidator

Prüft eine CSV-Datei gegenüber einem INTERLIS-Modell. Basiert auf dem
[*ilivalidator*](https://github.com/claeis/ilivalidator). Das
Datenmodell darf die OID nicht als UUID modellieren
(`OID AS INTERLIS.UUIDOID`).

Beispiel:

``` groovy
tasks.register('validate', CsvValidator) {
    models = "CsvModel"
    firstLineIsHeader = true
    dataFiles = ["data1.csv"]
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| allObjectsAccessible | `Boolean` | Mit der Option nimmt der Validator an, dass er Zugriff auf alle Objekte hat. D.h. es wird z.B. auch die Multiplizität von Beziehungen auf externe Objekte geprüft. Default: false |  ja |
| configFile | `Object` | Konfiguriert die Datenprüfung mit Hilfe einer ini-Datei (um z.B. die Prüfung von einzelnen Constraints auszuschalten). Siehe https://github.com/claeis/ilivalidator/blob/master/docs/ilivalidator.rst#konfiguration |  ja |
| dataFiles | `Object` | Liste der Dateien, die validiert werden sollen. `FileCollection` oder `List`. Eine leere Liste ist kein Fehler. |  nein |
| disableAreaValidation | `Boolean` | Schaltet die AREA-Topologieprüfung aus. Default: false |  ja |
| encoding | `String` | Zeichencodierung der CSV-Datei, z.B. `UTF-8`. Default: Systemeinstellung |  ja |
| failOnError | `Boolean` | Steuert, ob der Task bei einem Validierungsfehler fehlschlägt. Default: true |  ja |
| firstLineIsHeader | `Boolean` | Definiert, ob die CSV-Datei einer Headerzeile hat, oder nicht. Default: true |  ja |
| forceTypeValidation | `Boolean` | Ignoriert die Konfiguration der Typprüfung aus der TOML-Datei, d.h. es kann nur die Multiplizität aufgeweicht werden. Default: false |  ja |
| logFile | `Object` | Schreibt die log-Meldungen der Validierung in eine Text-Datei. |  ja |
| metaConfigFile | `Object` | Konfiguriert den Validator mit Hilfe einer ini-Datei. Siehe https://github.com/claeis/ilivalidator/blob/master/docs/ilivalidator.rst#konfiguration |  ja |
| modeldir | `String` | INTERLIS-Modellrepository. String separiert mit Semikolon (analog ili2db, ilivalidator). |  ja |
| models | `String` | INTERLIS-Modell, gegen das die Dateien geprüft werden sollen (mehrere Modellnamen durch Semikolon trennen). Default: Der Name der CSV-Datei. |  ja |
| multiplicityOff | `Boolean` | Schaltet die Prüfung der Multiplizität generell aus. Default: false |  ja |
| pluginFolder | `Object` | Verzeichnis mit JAR-Dateien, die Zusatzfunktionen enthalten. |  ja |
| proxy | `String` | Proxy-Server für den Zugriff auf Modell-Repositories. |  ja |
| proxyPort | `Integer` | Proxy-Port für den Zugriff auf Modell-Repositories. |  ja |
| skipPolygonBuilding | `Boolean` | Schaltet die Bildung der Polygone aus (nur ITF). Default: false |  ja |
| validationOk | `boolean` | OUTPUT: Ergebnis der Validierung. Nur falls failOnError=false. |  nein |
| valueDelimiter | `Character` | Zeichen, das am Anfang und Ende jeden Wertes vorhanden ist. Default `"` |  ja |
| valueSeparator | `Character` | Zeichen, das als Trennzeichen zwischen den Werten interpretiert werden soll. Default: `,` |  ja |
| xtflogFile | `Object` | Schreibt die log-Meldungen in eine INTERLIS-2-Datei. Die Datei result.xtf entspricht dem Modell IliVErrors. |  ja |

Falls die CSV-Datei eine Header-Zeile enthält (mit den Spaltennamen),
wird im gegebenen Modell eine Klasse gesucht, welche die
Header-Spaltennamen enthält (Gross-/Kleinschreibung sowie optionale
“Spalten” der Modell-Klasse werden ignoriert). Wird keine solche Klasse
gefunden, gilt das als Validierungsfehler.

Falls die CSV-Datei keine Header-Zeile enthält (mit den Spaltennamen),
wird im gegebenen Modell eine Klasse gesucht, die die selbe Anzahl
Attribute hat. Wird keine solche Klasse gefunden, gilt das als
Validierungsfehler.

Die Prüfung von gleichzeitig mehreren CSV-Dateien führt zu
Fehlermeldungen wie
`OID o3158 of object <Modelname>.<Topicname>.<Klassenname> already exists in ...`.
Beim Öffnen und Lesen einer CSV-Datei wird immer der Zähler, der die
interne (in der CSV-Datei nicht vorhandene) `OID` generiert,
zurückgesetzt. Somit kann immer nur eine CSV-Datei pro Task geprüft
werden.

### Curl

Simuliert mit einem `HttpClient` einige Curl-Befehle.

Beispiele:

``` groovy
import ch.so.agi.gretl.tasks.Curl.MethodType;

tasks.register('uploadData', Curl) {
    serverUrl = "https://geodienste.ch/data_agg/interlis/import"
    method = MethodType.POST
    formData = ["topic": "npl_waldgrenzen", "lv95_file": file("./test.xtf.zip"), "publish": "true", "replace_all": "true"]
    user = "fooUser"
    password = "barPwd"
    expectedStatusCode = 200
    expectedBody = "\"success\":true"
}
```

``` groovy
import ch.so.agi.gretl.tasks.Curl.MethodType;

tasks.register('uploadData', Curl) {
    serverUrl = "https://testweb.so.ch/typo3/api/digiplan"
    method = MethodType.POST
    headers = ["Content-Type": "application/xml", "Content-Encoding": "gzip"]
    dataBinary = file("./planregister.xml.gz")
    user = "fooUser"
    password = "barPwd"
    expectedStatusCode = 202
}
```

``` groovy
import ch.so.agi.gretl.tasks.Curl.MethodType;

tasks.register('downloadData', Curl) {
    serverUrl = "https://raw.githubusercontent.com/sogis/gretl/master/README.md"
    method = MethodType.GET
    outputFile = file("./README.md")
    expectedStatusCode = 200
}
```

``` groovy
import groovy.json.JsonSlurper

def basicHeader = "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes())
def token = ""

tasks.register('getToken', Curl) {
    serverUrl = "https://auth.example.ch/oauth2/token"
    method = MethodType.POST
    outputFile = file("./token.json")
    expectedStatusCode = 200
    data = "grant_type=client_credentials&client_id=$clientId"
    headers = [
        "Content-Type": "application/x-www-form-urlencoded",
        "Authorization": basicHeader
    ]

    doLast {
        def slurper = new JsonSlurper();
        def slurped = slurper.parseText(outputFile.text)

        if (slurped.access_token != null)
            token = slurped.access_token

        if(token.length() == 0)
            throw new GradleException("Failed to retrieve access token")

        println "Length of access token is: " + token.length()
    }
}

tasks.register('downloadJson', Curl) {
    dependsOn 'getToken'
    serverUrl = "https://obs.example.ch/rest/v4/docs.json?projects=83505"
    method = MethodType.GET
    outputFile = file("./data.json")
    expectedStatusCode = 200
    headers.put("Authorization", providers.provider { "Bearer " + token })
} 
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| data | `String` | String, der via POST hochgeladen werden soll. Entspricht `curl [URL] --data`. |  ja |
| dataBinary | `File` | Datei, die hochgeladen werden soll. Entspricht `curl [URL] --data-binary`. |  ja |
| expectedBody | `String` | Erwarteter Text, der vom Server als Body zurückgelieferd wird. |  ja |
| expectedStatusCode | `Integer` | Erwarteter Status Code, der vom Server zurückgeliefert wird. |  nein |
| formData | `Map<String,Object>` | Form data parameters. Entspricht `curl [URL] -F key1=value1 -F file1=@my_file.xtf`. |  ja |
| headers | `MapProperty<String,String>` | Request-Header. Entspricht `curl [URL] -H ... -H ....`. |  ja |
| method | `MethodType` | HTTP-Request-Methode. Unterstützt werden `GET` und `POST`. |  ja |
| outputFile | `File` | Datei, in die der Output gespeichert wird. Entspricht `curl [URL] -o`. |  ja |
| password | `String` | Passwort. Wird zusammen mit `user` in einen Authorization-Header umgewandelt. Entspricht `curl [URL] -u user:password`. |  ja |
| serverUrl | `String` | Die URL des Servers inklusive Pfad und Queryparameter. |  nein |
| user | `String` | Benutzername. Wird zusammen mit `password` in einen Authorization-Header umgewandelt. Entspricht `curl [URL] -u user:password`. |  ja |

### DatabaseDocumentExport (deprecated)

Speichert Dokumente, deren URL in einer Spalte einer Datenbanktabelle
gespeichert sind, in einem lokalen Verzeichnis. Zukünftig und bei Bedarf
kann der Task so erweitert werden, dass auch BLOBs aus der Datenbank
gespeichert werden können.

Redirect von HTTP nach HTTPS funktionieren nicht. Dies
[korrekterweise](https://stackoverflow.com/questions/1884230/httpurlconnection-doesnt-follow-redirect-from-http-to-https)
(?) wegen der verwendeten Java-Bibliothek.

Wegen der vom Kanton Solothurn eingesetzten self-signed Zertifikate muss
ein unschöner Handstand gemacht werden. Leider kann dieser Usecase
schlecht getestet werden, da die Links nur in der privaten Zone
verfügbar sind und die zudem noch häufig ändern können. Manuell getestet
wurde es jedoch.

Als Dateiname wird der letzte Teil des URL-Pfades verwendet, z.B.
`https://artplus.verw.rootso.org/MpWeb-apSolothurnDenkmal/download/2W8v0qRZQBC0ahDnZGut3Q?mode=gis`
wird mit den Prefix und Extension zu `ada_2W8v0qRZQBC0ahDnZGut3Q.pdf`.

Es wird `DISTINCT ON (<documentColumn>)` und ein Filter
`WHERE <documentColumn> IS NOT NULL` verwendet.

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('exportDocuments', DatabaseDocumentExport) {
    database = [db_uri, db_user, db_pass]
    qualifiedTableName = "ada_denkmalschutz.fachapplikation_rechtsvorschrift_link"
    documentColumn = "multimedia_link"
    targetDir = file(".")
    fileNamePrefix = "ada_"
    fileNameExtension = "pdf"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| database | `Connector` | Datenbank, aus der die Dokumente exportiert werden sollen. |  nein |
| documentColumn | `String` | DB-Tabellenspalte mit dem Dokument resp. der URL zum Dokument. |  nein |
| fileNameExtension | `String` | Dateinamen-Extension. |  ja |
| fileNamePrefix | `String` | Prefix für Dateinamen. |  ja |
| qualifiedTableName | `String` | Qualifizierter Tabellenname. |  nein |
| targetDir | `File` | Verzeichnis in das die Dokumente exportiert werden sollen. |  nein |

### Db2Db

Dies ist prinzipiell ein 1:1-Datenkopie, d.h. es findet kein Datenumbau
statt, die Quell- und die Ziel- Tabelle hat jeweils identische
Attribute. Es werden auf Seite Quelle in der Regel also simple
SELECT-Queries ausgeführt und die Resultate dieser Queries in Tabellen
der Ziel-DB eingefügt. Unter bestimmten Bedingungen (insbesondere wenn
es sich um einen wenig komplexen Datenumbau handelt), kann dieser Task
aber auch zum Datenumbau benutzt werden.

Die Queries können auf mehrere .sql-Dateien verteilt werden, d.h. der
Task muss die Queries mehrerer .sql-Dateien zu einer Transaktion
kombinieren können. Jede .sql-Datei gibt genau eine Resultset
(RAM-Tabelle) zurück. Das Resultset wird in die konfigurierte
Zieltabelle geschrieben. Die Beziehungen sind: Eine bis mehrere
Quelltabellen ergeben ein Resultset; das Resultset entspricht bezüglich
den Attributen genau der Zieltabelle und wird 1:1 in diese geschrieben.
Der Db2Db- Task verarbeitet innerhalb einer Transaktion 1-n Resultsets
und wird entsprechend auch mit 1-n SQL-Dateien konfiguriert.

Die Reihenfolge der .sql-Dateien ist relevant. Dies bedeutet, dass die
SQL-Befehle der zuerst angegebenen .sql-Datei zuerst ausgeführt werden
müssen, danach die SQL-Befehle der an zweiter Stelle angegebenen
.sql-Datei, usw.

Alle SELECT-Statements werden in einer Transaktion ausgeführt werden,
damit ein konsistenter Datenstand gelesen wird. Alle INSERT-Statements
werden in einer Transaktion ausgeführt werden, damit bei einem Fehler
der bisherige Datenstand bestehen bleibt und also kein unvollständiger
Import zurückgelassen wird.

Damit dieselbe .sql-Datei für verschiedene Datensätze benutzt werden
kann, ist es möglich innerhalb der .sql-Datei Parameter zu verwenden und
diesen Parametern beim Task einen konkreten Wert zuzuweisen. Innerhalb
der .sql-Datei werden Paramter mit folgender Syntax verwendet:
`${paramName}`.

Es ist pro .sql-Datei nur ein SELECT-Statement erlaubt. Ausser das erste
Statement ist ein “SET search_path TO”-Statement.

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('transferSomeData', Db2Db) {
    sourceDb = [db_uri, db_user, db_pass]
    targetDb = ['jdbc:sqlite:gretldemo.sqlite',null,null]
    sqlParameters = [dataset:'Olten']
    transferSets = [
        new TransferSet('some.sql', 'albums_dest', true)
    ];
}
```

Damit mit einer einzigen Task-Definition mehrere Datensätze verarbeitet
werden können, kann auch eine Liste von Parametern angegeben werden.

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('transferSomeData', Db2Db) {
    sourceDb = [db_uri, db_user, db_pass]
    targetDb = ['jdbc:sqlite:gretldemo.sqlite',null,null]
    sqlParameters = [[dataset:'Olten'],[dataset:'Grenchen']]
    transferSets = [
        new TransferSet('some.sql', 'albums_dest', true)
    ];
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| batchSize | `Property<Integer>` | Anzahl der Records, die pro Batch in die Ziel-Datenbank geschrieben werden (Default: 5000). Für sehr grosse Tabellen muss ein kleinerer Wert gewählt werden. |  ja |
| fetchSize | `Property<Integer>` | Anzahl der Records, die auf einmal vom Datenbank-Cursor von der Quell-Datenbank zurückgeliefert werden (Standard: 5000). Für sehr grosse Tabellen muss ein kleinerer Wert gewählt werden. |  ja |
| sourceDb | `ListProperty<String>` | Datenbank, aus der gelesen werden soll. |  nein |
| sqlParameters | `Property<Object>` | Eine Map mit Paaren von Parameter-Name und Parameter-Wert (`Map<String,String>`). Oder eine Liste mit Paaren von Parameter-Name und Parameter-Wert (`List<Map<String,String>>`). |  ja |
| targetDb | `ListProperty<String>` | Datenbank, in die geschrieben werden soll. |  nein |
| transferSets | `ListProperty<TransferSet>` | Eine Liste von `TransferSet`s. |  nein |

Eine `TransferSet` ist

- eine SQL-Datei (mit SQL-Anweisungen zum Lesen der Daten aus der
  sourceDb),
- dem Namen der Ziel-Tabelle in der targetDb, und
- der Angabe ob in der Ziel-Tabelle vor dem INSERT zuerst alle Records
  gelöscht werden sollen.
- einem optionalen vierten Parameter, der verwendet werden kann um den
  zu erzeugenden SQL-Insert-String zu beeinflussen, u.a. um einen
  WKT-Geometrie-String in eine PostGIS-Geometrie umzuwandeln

Beispiel, Umwandlung Rechtswert/Hochwertspalten in eine
PostGIS-Geometrie (siehe auch GRETL-Job
[afu_onlinerisk_transfer](https://github.com/sogis/gretljobs/tree/main/afu_onlinerisk_transfer)
der eine Punktgeometriespalten aus einer Nicht-Postgis-DB übernimmt):

``` groovy
new TransferSet('untersuchungseinheit.sql', 'afu_qrcat_v1.onlinerisk_untersuchungseinheit', true, (String[])["geom:wkt:2056"])
```

`geom` ist der Geometrie-Spalten-Name der verwendet wird.

Dazugehöriger Auszug aus SQL-Datei zur Erzeugung des WKT-Strings mit
Hilfe von concatenation:

``` sql
'Point(' || ue.koordinate_x::text || ' ' || ue.koordinate_y::text || ')' AS geom
```

Unterstützte Datenbanken: PostgreSQL, SQLite, Oracle, Derby und DuckDB.

### FtpDelete

Löscht Daten auf einem FTP-Server.

Beispiel, löscht alle Daten in einem Verzeichnis:

``` groovy
tasks.register('ftpdelete', FtpDelete) {
    server= "ftp.server.org"
    user= "Hans"
    password= "dummy"
    remoteDir = "\\dm01avso24lv95\\itf"
    remoteFile = fileTree(pathToTempFolder) { include '*.zip' } 
    //remoteFile = "*.zip"
}
```

Um bestimmte Dateien zu löschen:

``` groovy
tasks.register('ftpdelete', FtpDelete) {
    server= "ftp.server.org"
    user= "Hans"
    password= "dummy"
    remoteDir = "\\dm01avso24lv95\\itf"
    remoteFile = "*.zip"
}
```

Um heruntergeladene Daten zu löschen:

``` groovy
tasks.register('ftpdelete', FtpDelete) {
    server= "ftp.server.org"
    user= "Hans"
    password= "dummy"
    remoteDir = "\\dm01avso24lv95\\itf"
    remoteFile = fileTree(pathToDownloadFolder) { include '*.zip' } 
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| controlKeepAliveTimeout | `Long` | Timeout bis ein NOOP über den Kontroll-Kanal versendet wird. Default: 300s (=5 Minuten) |  ja |
| fileSeparator | `String` | Default: `/`. (Falls systemType Windows ist, ist der Default `\`. |  ja |
| passiveMode | `Boolean` | Aktiv- oder Passiv-Verbindungsmodus. Default: Passiv (true) |  ja |
| password | `String` | Passwort für den Zugriff auf dem Server. |  nein |
| remoteDir | `String` | Verzeichnis auf dem Server. |  nein |
| remoteFile | `Object` | Dateiname oder Liste der Dateinamen auf dem Server (kann auch ein Muster sein (\* oder ?)). Ohne diesen Parameter werden alle Dateien aus dem Remoteverzeichnis gelöscht. |  ja |
| server | `String` | Name des Servers (ohne ftp://). |  nein |
| systemType | `String` | `UNIX` oder `WINDOWS`. Default: `UNIX`. |  ja |
| user | `String` | Benutzername auf dem Server. |  nein |

### FtpDownload

Lädt alle Dateien aus dem definierten Verzeichnis des Servers in ein
lokales Verzeichnis herunter.

Beispiel:

``` groovy
tasks.register('ftpdownload', FtpDownload) {
    server= "ftp.server.org"
    user= "Hans"
    password= "dummy"
    localDir= "downloads"
    remoteDir= ""
}
```

Um eine bestimmte Datei herunterzuladen:

``` groovy
tasks.register('ftpdownload', FtpDownload) {
    server="ftp.infogrips.ch"
    user= "Hans"
    password= "dummy"
    systemType="WINDOWS"
    localDir= "downloads"
    remoteDir="\\dm01avso24lv95\\itf"
    remoteFile="240100.zip"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| controlKeepAliveTimeout | `Long` | Timeout bis ein NOOP über den Kontroll-Kanal versendet wird. Default: 300s (=5 Minuten) |  ja |
| fileSeparator | `String` | Default: `/`. (Falls systemType Windows ist, ist der Default `\`. |  ja |
| fileType | `String` | `ASCII` oder `BINARY`. Default: `ASCII`. |  ja |
| localDir | `String` | Lokales Verzeichnis, in dem die Dateien gespeichert werden. |  nein |
| passiveMode | `Boolean` | Aktiv- oder Passiv-Verbindungsmodus. Default: Passiv (true) |  ja |
| password | `String` | Passwort für den Zugriff auf dem Server. |  nein |
| remoteDir | `String` | Verzeichnis auf dem Server. |  nein |
| remoteFile | `Object` | Dateiname oder Liste der Dateinamen auf dem Server (kann auch ein Muster sein (\* oder ?)). Ohne diesen Parameter werden alle Dateien aus dem Remoteverzeichnis heruntergeladen. |  ja |
| server | `String` | Name des Servers (ohne ftp://). |  nein |
| systemType | `String` | `UNIX` oder `WINDOWS`. Default: `UNIX`. |  ja |
| user | `String` | Benutzername auf dem Server. |  nein |

### FtpList

Liefert eine Liste der Dateien aus dem definierten Verzeichnis des
Servers.

Beispiel:

``` groovy
tasks.register('ftplist', FtpList) {
    server= "ftp.server.org"
    user= "Hans"
    password= "dummy"
    remoteDir= ""
    doLast {
        println files
    }
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| controlKeepAliveTimeout | `Long` | Timeout bis ein NOOP über den Kontroll-Kanal versendet wird. Default: 300s (=5 Minuten) |  ja |
| fileSeparator | `String` | Default: `/`. (Falls systemType Windows ist, ist der Default `\`. |  ja |
| passiveMode | `Boolean` | Aktiv- oder Passiv-Verbindungsmodus. Default: Passiv (true) |  ja |
| password | `String` | Passwort für den Zugriff auf dem Server. |  nein |
| remoteDir | `String` | Verzeichnis auf dem Server. |  nein |
| server | `String` | Name des Servers (ohne ftp://). |  nein |
| systemType | `String` | `UNIX` oder `WINDOWS`. Default: `UNIX`. |  ja |
| user | `String` | Benutzername auf dem Server. |  nein |

### Gpkg2Dxf

Exportiert alle Tabellen einer GeoPackage-Datei in DXF-Dateien. Als
Input wird eine von *ili2gpkg* erzeugte GeoPackage-Datei benötigt.

Es werden alle INTERLIS-Klassen exportiert
(`SELECT tablename FROM T_ILI2DB_TABLE_PROP WHERE setting = 'CLASS'`).
Der eigentliche SELECT-Befehl ist komplizierter weil für das Layern der
einzelnen DXF-Objekte das INTERLIS-Metaattribut `!!@dxflayer="true"`
ausgelesen wird. Gibt es kein solches Metaattribut wird alles in den
gleichen DXF-Layer (`default`) geschrieben.

**Encoding**: Die DXF-Dateien sind `ISO-8859-1` encodiert.

**Achtung**: Task sollte verbessert werden (siehe E-Mail Claude im
Rahmen des Publisher-Projektes).

``` groovy
tasks.register('gpkg2dxf', Gpkg2Dxf) {
    dataFile = file("data.gpkg")
    outputDir = file("./out/")
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| dataFile | `File` | GeoPackage-Datei, die nach DXF transformiert werden soll. |  nein |
| outputDir | `File` | Verzeichnis, in das die DXF-Dateien gespeichert werden. |  nein |

### Gpkg2Shp

Exportiert alle Tabellen einer GeoPackage-Datei in Shapefiles. Als Input
wird eine von *ili2gpkg* erzeugte GeoPackage-Datei benötigt.

Es werden alle INTERLIS-Klassen exportiert
(`SELECT tablename FROM T_ILI2DB_TABLE_PROP WHERE setting = 'CLASS'`).
Je nach, bei der Erstellung der GeoPackage-Datei, verwendeten
Parametern, muss die Query angepasst werden. Es muss jedoch darauf
geachtet werden, dass es nur eine Query gibt (für alle Datensätze). Für
den vorgesehenen Anwendungsfall (sehr einfache, flache Modelle) dürfte
das kein Problem darstellen.

**Encoding**: Die Shapefiles sind neu `UTF-8` encodiert. Standard ist
`ISO-8859-1`, scheint aber v.a. in QGIS nicht standardmässig zu
funktionieren (keine Umlaute).

**Achtung**: Task sollte verbessert werden (siehe E-Mail Claude im
Rahmen des Publisher-Projektes).

``` groovy
tasks.register('gpkg2shp', Gpkg2Shp) {
    dataFile = file("data.gpkg")
    outputDir = file("./out/")
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| dataFile | `File` | GeoPackage-Datei, die nach Shapefile transformiert werden soll. |  nein |
| outputDir | `File` | Verzeichnis, in das die Shapefile gespeichert werden. |  nein |

### GpkgExport

Daten aus einer bestehenden Datenbanktabelle werden in eine
GeoPackage-Datei exportiert.

Beispiele:

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('gpkgexport', GpkgExport) {
    database = [db_uri, db_user, db_pass]
    schemaName = "gpkgexport"
    srcTableName = "exportdata"
    dataFile = "data.gpkg"
    dstTableName = "exportdata"
}
```

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('gpkgexport', GpkgExport) {
    database = [db_uri, db_user, db_pass]
    schemaName = "gpkgexport"
    srcTableName = ["exportTable1", "exportTable2"]
    dataFile = "data.gpkg"
    dstTableName = ["exportTable1", "exportTable2"]
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| batchSize | `Integer` | Anzahl der Records, die pro Batch in die Ziel-Datenbank (GeoPackage) geschrieben werden. Default: 5000 |  ja |
| dataFile | `Object` | Name der GeoPackage-Datei, die erstellt werden soll. |  nein |
| database | `Connector` | Datenbank aus der exportiert werden soll. |  nein |
| dstTableName | `Object` | Name der Tabelle(n) in der GeoPackage-Datei. `String` oder `List`. |  nein |
| fetchSize | `Integer` | Anzahl der Records, die pro Fetch aus der Quell-Datenbank gelesen werden. Default: 5000 |  ja |
| schemaName | `String` | Name des DB-Schemas, in dem die DB-Tabelle ist. |  ja |
| srcTableName | `Object` | Name der DB-Tabelle(n), die exportiert werden soll(en). `String` oder `List`. |  nein |

### GpkgImport

Daten aus einer GeoPackage-Datei in eine bestehende Datenbanktabelle
importieren.

Die Tabelle kann weitere Spalten enthalten, die in der GeoPackage-Datei
nicht vorkommen. Sie müssen aber NULLable sein, oder einen Default-Wert
definiert haben.

Die Gross-/Kleinschreibung der GeoPackage-Spaltennamen wird für die
Zuordnung zu den DB-Spalten ignoriert.

Beispiel:

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('gpkgimport', GpkgImport) {
    database = [db_uri, db_user, db_pass]
    schemaName = "gpkgimport"
    srcTableName = "Point"
    dstTableName = "importdata"
    dataFile = "point.gpkg"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| batchSize | `Integer` | Anzahl der Records, die pro Batch in die Ziel-Datenbank geschrieben werden. Default: 5000 |  ja |
| dataFile | `Object` | Name der GeoPackage-Datei, die gelesen werden soll. |  nein |
| database | `Connector` | Datenbank, in die importiert werden soll. |  nein |
| dstTableName | `String` | Name der DB-Tabelle, in die importiert werden soll. |  nein |
| fetchSize | `Integer` | Anzahl der Records, die pro Fetch aus der Quell-Datenbank gelesen werden. Default: 5000 |  ja |
| schemaName | `String` | Name des DB-Schemas, in dem die DB-Tabelle ist. |  ja |
| srcTableName | `String` | Name der GeoPackage-Tabelle, die importiert werden soll. |  nein |

### GpkgValidator

Prüft eine GeoPackage-Datei gegenüber einem INTERLIS-Modell. Basiert auf
dem [*ilivalidator*](https://github.com/claeis/ilivalidator).

Beispiel:

``` groovy
tasks.register('validate', GpkgValidator) {
    models = "GpkgModel"
    dataFiles = ["attributes.gpkg"]
    tableName = "Attributes"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| allObjectsAccessible | `Boolean` | Mit der Option nimmt der Validator an, dass er Zugriff auf alle Objekte hat. D.h. es wird z.B. auch die Multiplizität von Beziehungen auf externe Objekte geprüft. Default: false |  ja |
| configFile | `Object` | Konfiguriert die Datenprüfung mit Hilfe einer ini-Datei (um z.B. die Prüfung von einzelnen Constraints auszuschalten). Siehe https://github.com/claeis/ilivalidator/blob/master/docs/ilivalidator.rst#konfiguration |  ja |
| dataFiles | `Object` | Liste der Dateien, die validiert werden sollen. `FileCollection` oder `List`. Eine leere Liste ist kein Fehler. |  nein |
| disableAreaValidation | `Boolean` | Schaltet die AREA-Topologieprüfung aus. Default: false |  ja |
| failOnError | `Boolean` | Steuert, ob der Task bei einem Validierungsfehler fehlschlägt. Default: true |  ja |
| forceTypeValidation | `Boolean` | Ignoriert die Konfiguration der Typprüfung aus der TOML-Datei, d.h. es kann nur die Multiplizität aufgeweicht werden. Default: false |  ja |
| logFile | `Object` | Schreibt die log-Meldungen der Validierung in eine Text-Datei. |  ja |
| metaConfigFile | `Object` | Konfiguriert den Validator mit Hilfe einer ini-Datei. Siehe https://github.com/claeis/ilivalidator/blob/master/docs/ilivalidator.rst#konfiguration |  ja |
| modeldir | `String` | INTERLIS-Modellrepository. String separiert mit Semikolon (analog ili2db, ilivalidator). |  ja |
| models | `String` | INTERLIS-Modell, gegen das die Dateien geprüft werden sollen (mehrere Modellnamen durch Semikolon trennen). Default: Der Name der CSV-Datei. |  ja |
| multiplicityOff | `Boolean` | Schaltet die Prüfung der Multiplizität generell aus. Default: false |  ja |
| pluginFolder | `Object` | Verzeichnis mit JAR-Dateien, die Zusatzfunktionen enthalten. |  ja |
| proxy | `String` | Proxy-Server für den Zugriff auf Modell-Repositories. |  ja |
| proxyPort | `Integer` | Proxy-Port für den Zugriff auf Modell-Repositories. |  ja |
| skipPolygonBuilding | `Boolean` | Schaltet die Bildung der Polygone aus (nur ITF). Default: false |  ja |
| tableName | `String` | Name der Tabelle in den GeoPackage-Dateien. |  nein |
| validationOk | `boolean` | OUTPUT: Ergebnis der Validierung. Nur falls failOnError=false. |  nein |
| xtflogFile | `Object` | Schreibt die log-Meldungen in eine INTERLIS-2-Datei. Die Datei result.xtf entspricht dem Modell IliVErrors. |  ja |

### Gzip

Gzipped eine einzelne Datei. Es gibt einen eingebauten Tar-Task, der -
nomen est omen - aber immer zuerst eine Tar-Datei erstellt.

Beispiel:

``` groovy
tasks.register('compressFile', Gzip) {
    dataFile = file("./planregister.xml");
    gzipFile = file("./planregister.xml.gz");
}
```

| Parameter | Datentyp         | Beschreibung                     | Optional |
|-----------|------------------|----------------------------------|----------|
| dataFile  | `Property<File>` | Datei, die gezipped werden soll. |  nein    |
| gzipFile  | `Property<File>` | Output-Datei                     |  nein    |

### Ili2gpkgImport

Importiert Daten aus einer INTERLIS-Transferdatei in eine
GeoPackage-Datei.

Die Tabellen werden implizit auch angelegt, falls sie noch nicht
vorhanden sind. Falls die Tabellen in der Datenbank schon vorhanden
sind, können sie zusätzliche Spalten enthalten (z.B. `bfsnr`, `datum`
etc.), welche beim Import leer bleiben.

Falls beim Import ein Datensatz-Identifikator (dataset) definiert wird,
darf dieser Datensatz-Identifikator in der Datenbank noch nicht
vorhanden sein.

Um die bestehenden (früher importierten) Daten zu ersetzen, kann der
Task Ili2gpkgReplace (**not yet implemented**) verwendet werden.

Die Option `--doSchemaImport` wird automatisch gesetzt.

Beispiel:

``` groovy
tasks.register('importData', Ili2gpkgImport) {
    models = "SO_AGI_AV_GB_Administrative_Einteilungen_20180613"
    dataFile = file("data.xtf");
    dbfile = file("data.gpkg")    
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| baskets | `String` | Entspricht der ili2gpkg-Option `--baskets` |  ja |
| dataFile | `Object` | Name der XTF-/ITF-Datei, die gelesen werden soll. Es können auch mehrere Dateien sein. `FileCollection` oder `File` |  nein |
| dataset | `Object` | Entspricht der ili2gpkg-Option `--dataset` |  ja |
| dbfile | `File` | GeoPackage-Datei in die importiert werden soll. |  nein |
| defaultSrsCode | `String` | Entspricht der ili2gpkg-Option `--defaultSrsCode` |  ja |
| isCoalesceJson | `Boolean` | Entspricht der ili2gpkg-Option `--coalesceJson` |  ja |
| isCreateEnumTabs | `Boolean` | Entspricht der ili2gpkg-Option `--createEnumTabs` |  ja |
| isCreateGeomIdx | `Boolean` | Entspricht der ili2gpkg-Option `--createGeomIdx` |  ja |
| isCreateMetaInfo | `Boolean` | Entspricht der ili2gpkg-Option `--createMetaInfo` |  ja |
| isDeleteData | `Boolean` | Entspricht der ili2gpkg-Option `--deleteData` |  ja |
| isDisableAreaValidation | `Boolean` | Entspricht der ili2gpkg-Option `--disableAreaValidation` |  ja |
| isDisableValidation | `Boolean` | Entspricht der ili2gpkg-Option `--disableValidation` |  ja |
| isForceTypeValidation | `Boolean` | Entspricht der ili2gpkg-Option `--forceTypeValidation` |  ja |
| isIligml20 | `Boolean` | Entspricht der ili2gpkg-Option `--iligml20` |  ja |
| isImportTid | `Boolean` | Entspricht der ili2gpkg-Option `--importTid` |  ja |
| isNameByTopic | `Boolean` | Entspricht der ili2gpkg-Option `--nameByTopic` |  ja |
| isSkipGeometryErrors | `Boolean` | Entspricht der ili2gpkg-Option `--skipGeometryErrors` |  ja |
| isSkipPolygonBuilding | `Boolean` | Entspricht der ili2gpkg-Option `--skipPolygonBuilding` |  ja |
| isStrokeArcs | `Boolean` | Entspricht der ili2gpkg-Option `--strokeArcs` |  ja |
| isTrace | `Boolean` | Entspricht der ili2gpkg-Option `--trace` |  ja |
| logFile | `Object` | Entspricht der ili2gpkg-Option `--logFile` |  ja |
| modeldir | `String` | Entspricht der ili2gpkg-Option `--modeldir` |  ja |
| models | `String` | Entspricht der ili2gpkg-Option `--models` |  ja |
| postScript | `File` | Entspricht der ili2gpkg-Option `--postScript` |  ja |
| preScript | `File` | Entspricht der ili2gpkg-Option `--preScript` |  ja |
| proxy | `String` | Entspricht der ili2gpkg Option `--proxy` |  ja |
| proxyPort | `Integer` | Entspricht der ili2gpkg-Option `--proxyPort` |  ja |
| topics | `String` | Entspricht der ili2gpkg-Option `--topics` |  ja |
| validConfigFile | `File` | Entspricht der ili2gpkg-Option `--validConfigFile` |  ja |

Für die Beschreibung der einzenen ili2gpkg-Optionen:
https://github.com/claeis/ili2db/blob/master/docs/ili2db.rst#aufruf-syntax

### Ili2pgDelete

Löscht einen Datensatz in der PostgreSQL-Datenbank anhand eines
Datensatz-Identifikators. Diese Funktion bedingt, dass das
Datenbankschema mit der Option createBasketCol erstellt wurde (via Task
Ili2pgImportSchema). Der Parameter `failOnException` muss `false` sein,
ansonsten bricht der Job ab.

Beispiel:

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('deleteDataset', Ili2pgDelete) {
    database = [db_uri, db_user, db_pass]
    models = "DM01AVSO24LV95"
    dbschema = "dm01"
    dataset = "kammersrohr"
}
```

Es können auch mehrere Datensätze pro Task gelöscht werden:

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('deleteDataset', Ili2pgDelete) {
    database = [db_uri, db_user, db_pass]
    dbschema = "dm01"
    dataset = ["Olten","Grenchen"]
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| baskets | `Property<String>` | Entspricht der ili2pg-Option `--baskets`. |  ja |
| database | `ListProperty<String>` | Datenbank aus der exportiert werden soll. |  nein |
| dataset | `Property<Object>` | Entspricht der ili2pg-Option `--dataset`. |  ja |
| datasetSubstring | `ListProperty<Integer>` | Entspricht der ili2pg-Option `--datasetSubstring`. |  ja |
| dbschema | `Property<String>` | Entspricht der ili2pg-Option `--dbschema`. |  ja |
| deleteData | `Property<Boolean>` | Entspricht der ili2pg-Option `--deleteData`. |  ja |
| disableAreaValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableAreaValidation`. |  ja |
| disableRounding | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableRounding`. |  ja |
| disableValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableValidation`. |  ja |
| exportTid | `Property<Boolean>` | Entspricht der ili2pg-Option `--exportTid`. |  ja |
| failOnException | `Property<Boolean>` | Entspricht der ili2pg-Option `--failOnException`. |  ja |
| forceTypeValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--forceTypeValidation`. |  ja |
| iligml20 | `Property<Boolean>` | Entspricht der ili2pg-Option `--iligml20`. |  ja |
| importBid | `Property<Boolean>` | Entspricht der ili2pg-Option `--importBid`. |  ja |
| importTid | `Property<Boolean>` | Entspricht der ili2pg-Option `--importTid`. |  ja |
| logFile | `Property<Object>` | Entspricht der ili2pg-Option `--logFile`. |  ja |
| modeldir | `Property<String>` | Entspricht der ili2pg-Option `--modeldir`. |  ja |
| models | `Property<String>` | Entspricht der ili2pg-Option `--models`. |  ja |
| postScript | `Property<File>` | Entspricht der ili2pg-Option `--postScript`. |  ja |
| preScript | `Property<File>` | Entspricht der ili2pg-Option `--preScript`. |  ja |
| proxy | `Property<String>` | Entspricht der ili2pg-Option `--proxy`. |  ja |
| proxyPort | `Property<Integer>` | Entspricht der ili2pg-Option `--proxyPort`. |  ja |
| skipGeometryErrors | `Property<Boolean>` | Entspricht der ili2pg-Option `--skipGeometryErrors`. |  ja |
| skipPolygonBuilding | `Property<Boolean>` | Entspricht der ili2pg-Option `--skipPolygonBuilding`. |  ja |
| strokeArcs | `Property<Boolean>` | Entspricht der ili2pg-Option `--strokeArcs`. |  ja |
| topics | `Property<String>` | Entspricht der ili2pg-Option `--topics`. |  ja |
| trace | `Property<Boolean>` | Entspricht der ili2pg-Option `--trace`. |  ja |
| validConfigFile | `Property<File>` | Entspricht der ili2pg-Option `--validConfigFile`. |  ja |

Für die Beschreibung der einzenen ili2pg-Optionen:
https://github.com/claeis/ili2db/blob/master/docs/ili2db.rst#aufruf-syntax

### Ili2pgExport

Exportiert Daten aus der PostgreSQL-Datenbank in eine
INTERLIS-Transferdatei.

Mit dem Parameter `models`, `topics`, `baskets` oder `dataset` wird
definiert, welche Daten exportiert werden.

Ob die Daten im INTERLIS-1-, INTERLIS-2- oder GML-Format geschrieben
werden, ergibt sich aus der Dateinamenserweiterung der Ausgabedatei. Für
eine INTERLIS-1-Transferdatei muss die Erweiterung .itf verwendet
werden. Für eine GML-Transferdatei muss die Erweiterung .gml verwendet
werden.

Beispiel:

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('exportData', Ili2pgExport) {
    database = [db_uri, db_user, db_pass]
    dataFile = "lv03_254900-out.itf"
    dataset = "254900"
    logFile = "ili2pg.log"
}
```

Damit mit einer einzigen Task-Definition mehrere Datensätze verarbeitet
werden können, kann auch eine Liste von Dateinamen und Datensätzen
angegeben werden.

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('exportData', Ili2pgExport) {
    database = [db_uri, db_user, db_pass]
    dataFile = ["lv03_254900-out.itf","lv03_255000-out.itf"]
    dataset = ["254900","255000"]
    logFile = "ili2pg.log"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| baskets | `Property<String>` | Entspricht der ili2pg-Option `--baskets`. |  ja |
| database | `ListProperty<String>` | Datenbank aus der exportiert werden soll. |  nein |
| dataset | `Property<Object>` | Entspricht der ili2pg-Option `--dataset`. |  ja |
| datasetSubstring | `ListProperty<Integer>` | Entspricht der ili2pg-Option `--datasetSubstring`. |  ja |
| dbschema | `Property<String>` | Entspricht der ili2pg-Option `--dbschema`. |  ja |
| deleteData | `Property<Boolean>` | Entspricht der ili2pg-Option `--deleteData`. |  ja |
| disableAreaValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableAreaValidation`. |  ja |
| disableRounding | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableRounding`. |  ja |
| disableValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableValidation`. |  ja |
| export3 | `Property<Boolean>` | Entspricht der ili2pg-Option `--export3`. |  ja |
| exportModels | `Property<String>` | Entspricht der ili2pg-Option `--exportModels`. |  ja |
| exportTid | `Property<Boolean>` | Entspricht der ili2pg-Option `--exportTid`. |  ja |
| failOnException | `Property<Boolean>` | Entspricht der ili2pg-Option `--failOnException`. |  ja |
| forceTypeValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--forceTypeValidation`. |  ja |
| iligml20 | `Property<Boolean>` | Entspricht der ili2pg-Option `--iligml20`. |  ja |
| importBid | `Property<Boolean>` | Entspricht der ili2pg-Option `--importBid`. |  ja |
| importTid | `Property<Boolean>` | Entspricht der ili2pg-Option `--importTid`. |  ja |
| logFile | `Property<Object>` | Entspricht der ili2pg-Option `--logFile`. |  ja |
| modeldir | `Property<String>` | Entspricht der ili2pg-Option `--modeldir`. |  ja |
| models | `Property<String>` | Entspricht der ili2pg-Option `--models`. |  ja |
| postScript | `Property<File>` | Entspricht der ili2pg-Option `--postScript`. |  ja |
| preScript | `Property<File>` | Entspricht der ili2pg-Option `--preScript`. |  ja |
| proxy | `Property<String>` | Entspricht der ili2pg-Option `--proxy`. |  ja |
| proxyPort | `Property<Integer>` | Entspricht der ili2pg-Option `--proxyPort`. |  ja |
| skipGeometryErrors | `Property<Boolean>` | Entspricht der ili2pg-Option `--skipGeometryErrors`. |  ja |
| skipPolygonBuilding | `Property<Boolean>` | Entspricht der ili2pg-Option `--skipPolygonBuilding`. |  ja |
| strokeArcs | `Property<Boolean>` | Entspricht der ili2pg-Option `--strokeArcs`. |  ja |
| topics | `Property<String>` | Entspricht der ili2pg-Option `--topics`. |  ja |
| trace | `Property<Boolean>` | Entspricht der ili2pg-Option `--trace`. |  ja |
| validConfigFile | `Property<File>` | Entspricht der ili2pg-Option `--validConfigFile`. |  ja |

Für die Beschreibung der einzenen ili2pg-Optionen:
https://github.com/claeis/ili2db/blob/master/docs/ili2db.rst#aufruf-syntax

### Ili2pgImport

Importiert Daten aus einer INTERLIS-Transferdatei in die
PostgreSQL-Datenbank.

Die Tabellen werden implizit auch angelegt, falls sie noch nicht
vorhanden sind. Falls die Tabellen in der Datenbank schon vorhanden
sind, können sie zusätzliche Spalten enthalten (z.B. bfsnr, datum etc.),
welche beim Import leer bleiben.

Falls beim Import ein Datensatz-Identifikator (dataset) definiert wird,
darf dieser Datensatz-Identifikator in der Datenbank noch nicht
vorhanden sein.

Falls man mehrere Dateien importieren will, diese jedoch erst zur
Laufzeit eruiert werden können, muss der Parameter `dataFile` eine
Gradle `FileCollection` resp. eine implementierende Klasse (z.B.
`FileTree`) sein. Gleiches gilt für den `dataset`-Parameter. Als
einzelner Wert für das Dataset wird in diesem Fall der Name der Datei
*ohne* Extension und *ohne* Pfad verwendet. Leider kann nicht bereits in
der Task-Definition aus dem Filetree eine Liste gemacht werden, z.B.
`fileTree(pathToUnzipFolder) { include '*.itf' }.files.name`. Diese
Liste ist leer.

Um die bestehenden (früher importierten) Daten zu ersetzen, kann der
Task Ili2pgReplace verwendet werden.

Beispiel 1:

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('importData', Ili2pgImport) {
    database = [db_uri, db_user, db_pass]
    dataFile = "lv03_254900.itf"
    logFile = "ili2pg.log"
}
```

Beispiel 2:

Import der AV-Daten. In der `t_datasetname`-Spalte soll die BFS-Nummer
stehen. Die BFS-Nummer entspricht den ersten vier Zeichen des
Filenamens.

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('importData', Ili2pgImport) {
    database = [db_uri, db_user, db_pass]
    dataFile = fileTree(pathToUnzipFolder) { include '*.itf' }
    dataset = dataFile
    datasetSubstring = (0..4).toList()
    logFile = "ili2pg.log"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| baskets | `Property<String>` | Entspricht der ili2pg-Option `--baskets`. |  ja |
| dataFile | `Property<Object>` | Name der XTF-/ITF-Datei, die gelesen werden soll. Es können auch mehrere Dateien sein. `FileCollection` oder `String`. |  nein |
| database | `ListProperty<String>` | Datenbank aus der exportiert werden soll. |  nein |
| dataset | `Property<Object>` | Entspricht der ili2pg-Option `--dataset`. |  ja |
| datasetSubstring | `ListProperty<Integer>` | Entspricht der ili2pg-Option `--datasetSubstring`. |  ja |
| dbschema | `Property<String>` | Entspricht der ili2pg-Option `--dbschema`. |  ja |
| deleteData | `Property<Boolean>` | Entspricht der ili2pg-Option `--deleteData`. |  ja |
| disableAreaValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableAreaValidation`. |  ja |
| disableRounding | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableRounding`. |  ja |
| disableValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableValidation`. |  ja |
| exportTid | `Property<Boolean>` | Entspricht der ili2pg-Option `--exportTid`. |  ja |
| failOnException | `Property<Boolean>` | Entspricht der ili2pg-Option `--failOnException`. |  ja |
| forceTypeValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--forceTypeValidation`. |  ja |
| iligml20 | `Property<Boolean>` | Entspricht der ili2pg-Option `--iligml20`. |  ja |
| importBid | `Property<Boolean>` | Entspricht der ili2pg-Option `--importBid`. |  ja |
| importTid | `Property<Boolean>` | Entspricht der ili2pg-Option `--importTid`. |  ja |
| logFile | `Property<Object>` | Entspricht der ili2pg-Option `--logFile`. |  ja |
| modeldir | `Property<String>` | Entspricht der ili2pg-Option `--modeldir`. |  ja |
| models | `Property<String>` | Entspricht der ili2pg-Option `--models`. |  ja |
| postScript | `Property<File>` | Entspricht der ili2pg-Option `--postScript`. |  ja |
| preScript | `Property<File>` | Entspricht der ili2pg-Option `--preScript`. |  ja |
| proxy | `Property<String>` | Entspricht der ili2pg-Option `--proxy`. |  ja |
| proxyPort | `Property<Integer>` | Entspricht der ili2pg-Option `--proxyPort`. |  ja |
| skipGeometryErrors | `Property<Boolean>` | Entspricht der ili2pg-Option `--skipGeometryErrors`. |  ja |
| skipPolygonBuilding | `Property<Boolean>` | Entspricht der ili2pg-Option `--skipPolygonBuilding`. |  ja |
| strokeArcs | `Property<Boolean>` | Entspricht der ili2pg-Option `--strokeArcs`. |  ja |
| topics | `Property<String>` | Entspricht der ili2pg-Option `--topics`. |  ja |
| trace | `Property<Boolean>` | Entspricht der ili2pg-Option `--trace`. |  ja |
| validConfigFile | `Property<File>` | Entspricht der ili2pg-Option `--validConfigFile`. |  ja |

Für die Beschreibung der einzenen ili2pg-Optionen:
https://github.com/claeis/ili2db/blob/master/docs/ili2db.rst#aufruf-syntax

### Ili2pgImportSchema

Erstellt die Tabellenstruktur in der PostgreSQL-Datenbank anhand eines
INTERLIS-Modells.

Der Parameter `iliFile` oder `models` muss gesetzt werden.

Beispiel:

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('importSchema', Ili2pgImportSchema) {
    database = [db_uri, db_user, db_pass]
    models = "DM01AVSO24"
    dbschema = "gretldemo"
    logFile = "ili2pg.log"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| baskets | `Property<String>` | Entspricht der ili2pg-Option `--baskets`. |  ja |
| beautifyEnumDispName | `Property<Boolean>` | Entspricht der ili2pg-Option `--beautifyEnumDispName`. |  ja |
| coalesceArray | `Property<Boolean>` | Entspricht der ili2pg-Option `--coalesceArray`. |  ja |
| coalesceCatalogueRef | `Property<Boolean>` | Entspricht der ili2pg-Option `--coalesceCatalogueRef`. |  ja |
| coalesceJson | `Property<Boolean>` | Entspricht der ili2pg-Option `--coalesceJson`. |  ja |
| coalesceMultiLine | `Property<Boolean>` | Entspricht der ili2pg-Option `--coalesceMultiline`. |  ja |
| coalesceMultiSurface | `Property<Boolean>` | Entspricht der ili2pg-Option `--coalesceMultiSurface`. |  ja |
| createBasketCol | `Property<Boolean>` | Entspricht der ili2pg-Option `--createBasketCol`. |  ja |
| createDatasetCol | `Property<Boolean>` | Entspricht der ili2pg-Option `--createDatasetCol`. |  ja |
| createDateTimeChecks | `Property<Boolean>` | Entspricht der ili2pg-Option `--createDateTimeChecks`. |  ja |
| createEnumColAsItfCode | `Property<Boolean>` | Entspricht der ili2pg-Option `--createEnumColAsItfCode`. |  ja |
| createEnumTabs | `Property<Boolean>` | Entspricht der ili2pg-Option `--createEnumTabs`. |  ja |
| createEnumTabsWithId | `Property<Boolean>` | Entspricht der ili2pg-Option `--createEnumTabsWithId`. |  ja |
| createEnumTxtCol | `Property<Boolean>` | Entspricht der ili2pg-Option `--createEnumTxtCol`. |  ja |
| createFk | `Property<Boolean>` | Entspricht der ili2pg-Option `--createFk`. |  ja |
| createFkIdx | `Property<Boolean>` | Entspricht der ili2pg-Option `--createFkIdx`. |  ja |
| createGeomIdx | `Property<Boolean>` | Entspricht der ili2pg-Option `--createGeomIdx`. |  ja |
| createImportTabs | `Property<Boolean>` | Entspricht der ili2pg-Option `--createImportTabs`. |  ja |
| createMetaInfo | `Property<Boolean>` | Entspricht der ili2pg-Option `--createMetaInfo`. |  ja |
| createNumChecks | `Property<Boolean>` | Entspricht der ili2pg-Option `--createNumChecks`. |  ja |
| createSingleEnumTab | `Property<Boolean>` | Entspricht der ili2pg-Option `--createSingleEnumTab`. |  ja |
| createStdCols | `Property<Boolean>` | Entspricht der ili2pg-Option `--createStdCols`. |  ja |
| createTextChecks | `Property<Boolean>` | Entspricht der ili2pg-Option `--createTextChecks`. |  ja |
| createTidCol | `Property<Boolean>` | Entspricht der ili2pg-Option `--createTidCol`. |  ja |
| createTypeConstraint | `Property<Boolean>` | Entspricht der ili2pg-Option `--createTypeConstraint`. |  ja |
| createTypeDiscriminator | `Property<Boolean>` | Entspricht der ili2pg-Option `--createTypeDescriminator`. |  ja |
| createUnique | `Property<Boolean>` | Entspricht der ili2pg-Option `--createUnique`. |  ja |
| createscript | `Property<Object>` | Entspricht der ili2pg-Option `--createscript`. |  ja |
| database | `ListProperty<String>` | Datenbank aus der exportiert werden soll. |  nein |
| dataset | `Property<Object>` | Entspricht der ili2pg-Option `--dataset`. |  ja |
| datasetSubstring | `ListProperty<Integer>` | Entspricht der ili2pg-Option `--datasetSubstring`. |  ja |
| dbschema | `Property<String>` | Entspricht der ili2pg-Option `--dbschema`. |  ja |
| defaultSrsAuth | `Property<String>` | Entspricht der ili2pg-Option `--defaultSrsAuth`. |  ja |
| defaultSrsCode | `Property<String>` | Entspricht der ili2pg-Option `--defaultSrsCode`. |  ja |
| deleteData | `Property<Boolean>` | Entspricht der ili2pg-Option `--deleteData`. |  ja |
| disableAreaValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableAreaValidation`. |  ja |
| disableNameOptimization | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableNameOptimization`. |  ja |
| disableRounding | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableRounding`. |  ja |
| disableValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableValidation`. |  ja |
| dropscript | `Property<Object>` | Entspricht der ili2pg-Option `--dropscript`. |  ja |
| expandMultilingual | `Property<Boolean>` | Entspricht der ili2pg-Option `--expandMultilingual`. |  ja |
| exportTid | `Property<Boolean>` | Entspricht der ili2pg-Option `--exportTid`. |  ja |
| failOnException | `Property<Boolean>` | Entspricht der ili2pg-Option `--failOnException`. |  ja |
| forceTypeValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--forceTypeValidation`. |  ja |
| idSeqMax | `Property<Long>` | Entspricht der ili2pg-Option `--idSeqMax`. |  ja |
| idSeqMin | `Property<Long>` | Entspricht der ili2pg-Option `--idSeqMin`. |  ja |
| iliFile | `Property<Object>` | Name der ili-Datei, die gelesen werden soll. |  ja |
| iliMetaAttrs | `Property<Object>` | Entspricht der ili2pg-Option `--iliMetaAttrs`. |  ja |
| iligml20 | `Property<Boolean>` | Entspricht der ili2pg-Option `--iligml20`. |  ja |
| importBid | `Property<Boolean>` | Entspricht der ili2pg-Option `--importBid`. |  ja |
| importTid | `Property<Boolean>` | Entspricht der ili2pg-Option `--importTid`. |  ja |
| keepAreaRef | `Property<Boolean>` | Entspricht der ili2pg-Option `--keepAreaRef`. |  ja |
| logFile | `Property<Object>` | Entspricht der ili2pg-Option `--logFile`. |  ja |
| maxNameLength | `Property<Integer>` | Entspricht der ili2pg-Option `--maxNameLength`. |  ja |
| metaConfig | `Property<String>` | Entspricht der ili2pg-Option `--metaConfig`. |  ja |
| modeldir | `Property<String>` | Entspricht der ili2pg-Option `--modeldir`. |  ja |
| models | `Property<String>` | Entspricht der ili2pg-Option `--models`. |  ja |
| nameByTopic | `Property<Boolean>` | Entspricht der ili2pg-Option `--nameByTopic`. |  ja |
| noSmartMapping | `Property<Boolean>` | Entspricht der ili2pg-Option `--noSmartMapping`. |  ja |
| oneGeomPerTable | `Property<Boolean>` | Entspricht der ili2pg-Option `--oneGeomPerTable`. |  ja |
| postScript | `Property<File>` | Entspricht der ili2pg-Option `--postScript`. |  ja |
| preScript | `Property<File>` | Entspricht der ili2pg-Option `--preScript`. |  ja |
| proxy | `Property<String>` | Entspricht der ili2pg-Option `--proxy`. |  ja |
| proxyPort | `Property<Integer>` | Entspricht der ili2pg-Option `--proxyPort`. |  ja |
| setupPgExt | `Property<Boolean>` | Entspricht der ili2pg-Option `--setupPgExt`. |  ja |
| skipGeometryErrors | `Property<Boolean>` | Entspricht der ili2pg-Option `--skipGeometryErrors`. |  ja |
| skipPolygonBuilding | `Property<Boolean>` | Entspricht der ili2pg-Option `--skipPolygonBuilding`. |  ja |
| smart1Inheritance | `Property<Boolean>` | Entspricht der ili2pg-Option `--smart1Inheritance`. |  ja |
| smart2Inheritance | `Property<Boolean>` | Entspricht der ili2pg-Option `--smart2Inheritance`. |  ja |
| sqlColsAsText | `Property<Boolean>` | Entspricht der ili2pg-Option `--sqlColsAsText`. |  ja |
| sqlEnableNull | `Property<Boolean>` | Entspricht der ili2pg-Option `--sqlEnableNull`. |  ja |
| sqlExtRefCols | `Property<Boolean>` | Entspricht der ili2pg-Option `--sqlExtRefCols`. |  ja |
| strokeArcs | `Property<Boolean>` | Entspricht der ili2pg-Option `--strokeArcs`. |  ja |
| t_id_Name | `Property<String>` | Entspricht der ili2pg-Option `--t_id_Name`. |  ja |
| topics | `Property<String>` | Entspricht der ili2pg-Option `--topics`. |  ja |
| trace | `Property<Boolean>` | Entspricht der ili2pg-Option `--trace`. |  ja |
| translation | `Property<String>` | Entspricht der ili2pg-Option `--translation`. |  ja |
| validConfigFile | `Property<File>` | Entspricht der ili2pg-Option `--validConfigFile`. |  ja |

Für die Beschreibung der einzenen ili2pg-Optionen:
https://github.com/claeis/ili2db/blob/master/docs/ili2db.rst#aufruf-syntax

### Ili2pgReplace

Ersetzt die Daten in der PostgreSQL-Datenbank anhand eines
Datensatz-Identifikators (dataset) mit den Daten aus einer
INTERLIS-Transferdatei. Diese Funktion bedingt, dass das Datenbankschema
mit der Option createBasketCol erstellt wurde (via Task
Ili2pgImportSchema).

Beispiel:

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('replaceData', Ili2pgReplace) {
    database = [db_uri, db_user, db_pass]
    dataFile = "lv03_254900.itf"
    dataset = "254900"
    logFile = "ili2pg.log"
}
```

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('importData', Ili2pgReplace) {
    database = [db_uri, db_user, db_pass]
    models = 'DM01AVSO24LV95'
    dbschema = 'agi_dm01avso24'
    dataFile = fileTree(dir: dm01SoDir, include: '*.itf')
    dataset = dataFile
    datasetSubstring = 0..4
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| baskets | `Property<String>` | Entspricht der ili2pg-Option `--baskets`. |  ja |
| dataFile | `Property<Object>` | null |  nein |
| database | `ListProperty<String>` | Datenbank aus der exportiert werden soll. |  nein |
| dataset | `Property<Object>` | Entspricht der ili2pg-Option `--dataset`. |  ja |
| datasetSubstring | `ListProperty<Integer>` | Entspricht der ili2pg-Option `--datasetSubstring`. |  ja |
| dbschema | `Property<String>` | Entspricht der ili2pg-Option `--dbschema`. |  ja |
| deleteData | `Property<Boolean>` | Entspricht der ili2pg-Option `--deleteData`. |  ja |
| disableAreaValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableAreaValidation`. |  ja |
| disableRounding | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableRounding`. |  ja |
| disableValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableValidation`. |  ja |
| exportTid | `Property<Boolean>` | Entspricht der ili2pg-Option `--exportTid`. |  ja |
| failOnException | `Property<Boolean>` | Entspricht der ili2pg-Option `--failOnException`. |  ja |
| forceTypeValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--forceTypeValidation`. |  ja |
| iligml20 | `Property<Boolean>` | Entspricht der ili2pg-Option `--iligml20`. |  ja |
| importBid | `Property<Boolean>` | Entspricht der ili2pg-Option `--importBid`. |  ja |
| importTid | `Property<Boolean>` | Entspricht der ili2pg-Option `--importTid`. |  ja |
| logFile | `Property<Object>` | Entspricht der ili2pg-Option `--logFile`. |  ja |
| modeldir | `Property<String>` | Entspricht der ili2pg-Option `--modeldir`. |  ja |
| models | `Property<String>` | Entspricht der ili2pg-Option `--models`. |  ja |
| postScript | `Property<File>` | Entspricht der ili2pg-Option `--postScript`. |  ja |
| preScript | `Property<File>` | Entspricht der ili2pg-Option `--preScript`. |  ja |
| proxy | `Property<String>` | Entspricht der ili2pg-Option `--proxy`. |  ja |
| proxyPort | `Property<Integer>` | Entspricht der ili2pg-Option `--proxyPort`. |  ja |
| skipGeometryErrors | `Property<Boolean>` | Entspricht der ili2pg-Option `--skipGeometryErrors`. |  ja |
| skipPolygonBuilding | `Property<Boolean>` | Entspricht der ili2pg-Option `--skipPolygonBuilding`. |  ja |
| strokeArcs | `Property<Boolean>` | Entspricht der ili2pg-Option `--strokeArcs`. |  ja |
| topics | `Property<String>` | Entspricht der ili2pg-Option `--topics`. |  ja |
| trace | `Property<Boolean>` | Entspricht der ili2pg-Option `--trace`. |  ja |
| validConfigFile | `Property<File>` | Entspricht der ili2pg-Option `--validConfigFile`. |  ja |

Für die Beschreibung der einzenen ili2pg-Optionen:
https://github.com/claeis/ili2db/blob/master/docs/ili2db.rst#aufruf-syntax

### Ili2pgUpdate

Aktualisiert die Daten in der PostgreSQL-Datenbank anhand einer
INTERLIS-Transferdatei, d.h. neue Objekte werden eingefügt, bestehende
Objekte werden aktualisiert und in der Transferdatei nicht mehr
vorhandene Objekte werden gelöscht.

Diese Funktion bedingt, dass das Datenbankschema mit der Option
`--createBasketCol` erstellt wurde (via Task Ili2pgImportSchema), und
dass die Klassen und Topics eine stabile OID haben.

Beispiel:

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('updateData', Ili2pgUpdate) {
    database = [db_uri, db_user, db_pass]
    dataFile = "lv03_254900.itf"
    dataset = "254900"
    logFile = "ili2pg.log"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| baskets | `Property<String>` | Entspricht der ili2pg-Option `--baskets`. |  ja |
| dataFile | `Property<Object>` | null |  nein |
| database | `ListProperty<String>` | Datenbank aus der exportiert werden soll. |  nein |
| dataset | `Property<Object>` | Entspricht der ili2pg-Option `--dataset`. |  ja |
| datasetSubstring | `ListProperty<Integer>` | Entspricht der ili2pg-Option `--datasetSubstring`. |  ja |
| dbschema | `Property<String>` | Entspricht der ili2pg-Option `--dbschema`. |  ja |
| deleteData | `Property<Boolean>` | Entspricht der ili2pg-Option `--deleteData`. |  ja |
| disableAreaValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableAreaValidation`. |  ja |
| disableRounding | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableRounding`. |  ja |
| disableValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableValidation`. |  ja |
| exportTid | `Property<Boolean>` | Entspricht der ili2pg-Option `--exportTid`. |  ja |
| failOnException | `Property<Boolean>` | Entspricht der ili2pg-Option `--failOnException`. |  ja |
| forceTypeValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--forceTypeValidation`. |  ja |
| iligml20 | `Property<Boolean>` | Entspricht der ili2pg-Option `--iligml20`. |  ja |
| importBid | `Property<Boolean>` | Entspricht der ili2pg-Option `--importBid`. |  ja |
| importTid | `Property<Boolean>` | Entspricht der ili2pg-Option `--importTid`. |  ja |
| logFile | `Property<Object>` | Entspricht der ili2pg-Option `--logFile`. |  ja |
| modeldir | `Property<String>` | Entspricht der ili2pg-Option `--modeldir`. |  ja |
| models | `Property<String>` | Entspricht der ili2pg-Option `--models`. |  ja |
| postScript | `Property<File>` | Entspricht der ili2pg-Option `--postScript`. |  ja |
| preScript | `Property<File>` | Entspricht der ili2pg-Option `--preScript`. |  ja |
| proxy | `Property<String>` | Entspricht der ili2pg-Option `--proxy`. |  ja |
| proxyPort | `Property<Integer>` | Entspricht der ili2pg-Option `--proxyPort`. |  ja |
| skipGeometryErrors | `Property<Boolean>` | Entspricht der ili2pg-Option `--skipGeometryErrors`. |  ja |
| skipPolygonBuilding | `Property<Boolean>` | Entspricht der ili2pg-Option `--skipPolygonBuilding`. |  ja |
| strokeArcs | `Property<Boolean>` | Entspricht der ili2pg-Option `--strokeArcs`. |  ja |
| topics | `Property<String>` | Entspricht der ili2pg-Option `--topics`. |  ja |
| trace | `Property<Boolean>` | Entspricht der ili2pg-Option `--trace`. |  ja |
| validConfigFile | `Property<File>` | Entspricht der ili2pg-Option `--validConfigFile`. |  ja |

Für die Beschreibung der einzenen ili2pg-Optionen:
https://github.com/claeis/ili2db/blob/master/docs/ili2db.rst#aufruf-syntax

### Ili2pgValidate

Prüft die Daten ohne diese in eine Datei zu exportieren. Der Task ist
erfolgreich, wenn keine Fehler gefunden werden und ist nicht
erfolgreich, wenn Fehler gefunden werden. Mit der Option
`failOnException=false` ist der Task erfolgreich, auch wenn Fehler
gefunden werden.

Mit dem Parameter `--models`, `--topics`, `--baskets` oder `--dataset`
wird definiert, welche Daten geprüft werden. Parameter `--dataset`
akzeptiert auch eine Liste von Datasets.

Beispiel:

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('validate', Ili2pgValidate) {
    database = [db_uri, db_user, db_pass]
    models = "SO_AGI_AV_GB_Administrative_Einteilungen_20180613"
    modeldir = rootProject.projectDir.toString() + ";http://models.interlis.ch"
    dbschema = "agi_av_gb_admin_einteilungen_fail"
    logFile = file("fubar.log")
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| baskets | `Property<String>` | Entspricht der ili2pg-Option `--baskets`. |  ja |
| database | `ListProperty<String>` | Datenbank aus der exportiert werden soll. |  nein |
| dataset | `Property<Object>` | Entspricht der ili2pg-Option `--dataset`. |  ja |
| datasetSubstring | `ListProperty<Integer>` | Entspricht der ili2pg-Option `--datasetSubstring`. |  ja |
| dbschema | `Property<String>` | Entspricht der ili2pg-Option `--dbschema`. |  ja |
| deleteData | `Property<Boolean>` | Entspricht der ili2pg-Option `--deleteData`. |  ja |
| disableAreaValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableAreaValidation`. |  ja |
| disableRounding | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableRounding`. |  ja |
| disableValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--disableValidation`. |  ja |
| exportTid | `Property<Boolean>` | Entspricht der ili2pg-Option `--exportTid`. |  ja |
| failOnException | `Property<Boolean>` | Entspricht der ili2pg-Option `--failOnException`. |  ja |
| forceTypeValidation | `Property<Boolean>` | Entspricht der ili2pg-Option `--forceTypeValidation`. |  ja |
| iligml20 | `Property<Boolean>` | Entspricht der ili2pg-Option `--iligml20`. |  ja |
| importBid | `Property<Boolean>` | Entspricht der ili2pg-Option `--importBid`. |  ja |
| importTid | `Property<Boolean>` | Entspricht der ili2pg-Option `--importTid`. |  ja |
| logFile | `Property<Object>` | Entspricht der ili2pg-Option `--logFile`. |  ja |
| modeldir | `Property<String>` | Entspricht der ili2pg-Option `--modeldir`. |  ja |
| models | `Property<String>` | Entspricht der ili2pg-Option `--models`. |  ja |
| postScript | `Property<File>` | Entspricht der ili2pg-Option `--postScript`. |  ja |
| preScript | `Property<File>` | Entspricht der ili2pg-Option `--preScript`. |  ja |
| proxy | `Property<String>` | Entspricht der ili2pg-Option `--proxy`. |  ja |
| proxyPort | `Property<Integer>` | Entspricht der ili2pg-Option `--proxyPort`. |  ja |
| skipGeometryErrors | `Property<Boolean>` | Entspricht der ili2pg-Option `--skipGeometryErrors`. |  ja |
| skipPolygonBuilding | `Property<Boolean>` | Entspricht der ili2pg-Option `--skipPolygonBuilding`. |  ja |
| strokeArcs | `Property<Boolean>` | Entspricht der ili2pg-Option `--strokeArcs`. |  ja |
| topics | `Property<String>` | Entspricht der ili2pg-Option `--topics`. |  ja |
| trace | `Property<Boolean>` | Entspricht der ili2pg-Option `--trace`. |  ja |
| validConfigFile | `Property<File>` | Entspricht der ili2pg-Option `--validConfigFile`. |  ja |

Für die Beschreibung der einzenen ili2pg-Optionen:
https://github.com/claeis/ili2db/blob/master/docs/ili2db.rst#aufruf-syntax

### IliValidator

Prüft eine INTERLIS-Datei (.itf oder .xtf) gegenüber einem
INTERLIS-Modell (.ili). Basiert auf dem
[*ilivalidator*](https://github.com/claeis/ilivalidator).

Beispiel:

``` groovy
tasks.register('validate', IliValidator) {
    dataFiles = ["Beispiel2a.xtf"]
    logFile = "ilivalidator.log"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| allObjectsAccessible | `Boolean` | Mit der Option nimmt der Validator an, dass er Zugriff auf alle Objekte hat. D.h. es wird z.B. auch die Multiplizität von Beziehungen auf externe Objekte geprüft. Default: false |  ja |
| configFile | `Object` | Konfiguriert die Datenprüfung mit Hilfe einer ini-Datei (um z.B. die Prüfung von einzelnen Constraints auszuschalten). Siehe https://github.com/claeis/ilivalidator/blob/master/docs/ilivalidator.rst#konfiguration |  ja |
| dataFiles | `Object` | Liste der Dateien, die validiert werden sollen. `FileCollection` oder `List`. Eine leere Liste ist kein Fehler. |  nein |
| disableAreaValidation | `Boolean` | Schaltet die AREA-Topologieprüfung aus. Default: false |  ja |
| failOnError | `Boolean` | Steuert, ob der Task bei einem Validierungsfehler fehlschlägt. Default: true |  ja |
| forceTypeValidation | `Boolean` | Ignoriert die Konfiguration der Typprüfung aus der TOML-Datei, d.h. es kann nur die Multiplizität aufgeweicht werden. Default: false |  ja |
| logFile | `Object` | Schreibt die log-Meldungen der Validierung in eine Text-Datei. |  ja |
| metaConfigFile | `Object` | Konfiguriert den Validator mit Hilfe einer ini-Datei. Siehe https://github.com/claeis/ilivalidator/blob/master/docs/ilivalidator.rst#konfiguration |  ja |
| modeldir | `String` | INTERLIS-Modellrepository. String separiert mit Semikolon (analog ili2db, ilivalidator). |  ja |
| models | `String` | INTERLIS-Modell, gegen das die Dateien geprüft werden sollen (mehrere Modellnamen durch Semikolon trennen). Default: Der Name der CSV-Datei. |  ja |
| multiplicityOff | `Boolean` | Schaltet die Prüfung der Multiplizität generell aus. Default: false |  ja |
| pluginFolder | `Object` | Verzeichnis mit JAR-Dateien, die Zusatzfunktionen enthalten. |  ja |
| proxy | `String` | Proxy-Server für den Zugriff auf Modell-Repositories. |  ja |
| proxyPort | `Integer` | Proxy-Port für den Zugriff auf Modell-Repositories. |  ja |
| skipPolygonBuilding | `Boolean` | Schaltet die Bildung der Polygone aus (nur ITF). Default: false |  ja |
| validationOk | `boolean` | OUTPUT: Ergebnis der Validierung. Nur falls failOnError=false. |  nein |
| xtflogFile | `Object` | Schreibt die log-Meldungen in eine INTERLIS-2-Datei. Die Datei result.xtf entspricht dem Modell IliVErrors. |  ja |

Zusatzfunktionen (Custom Functions): Die `pluginFolder`-Option ist zum
jetzigen Zeitpunkt ohne Wirkung. Die Zusatzfunktionen werden als normale
Abhängigkeit definiert und in der ilivalidator-Task-Implementierung
registriert. Das Laden der Klassen zur Laufzeit in *iox-ili* hat nicht
funktioniert (`NoClassDefFoundError`…). Der Plugin-Mechanismus von
*ilivalidator* wird momentan ohnehin geändert (“Ahead-Of-Time-tauglich”
gemacht).

### JsonImport

Daten aus einer Json-Datei in eine Datenbanktabelle importieren. Die
gesamte Json-Datei (muss UTF-8 encoded sein) wird als Text in eine
Spalte importiert. Ist das Json-Objekt in der Datei ein Top-Level-Array
wird für jedes Element des Arrays ein Record in der Datenbanktabelle
erzeugt.

Beispiel:

``` groovy
tasks.register('importJson', JsonImport) {
    database = [db_uri, db_user, db_pass]
    jsonFile = "data.json"
    qualifiedTableName = "jsonimport.jsonarray"
    columnName = "json_text_col"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| columnName | `String` | Spaltenname der Tabelle, in die importiert werden soll. |  nein |
| database | `Connector` | Datenbank, in die importiert werden soll. |  nein |
| isDeleteAllRows | `Boolean` | Inhalt der Tabelle vorgängig löschen? |  ja |
| jsonFile | `String` | JSON-Datei, die importiert werden soll. |  nein |
| qualifiedTableName | `String` | Qualifizierter Namen der Tabelle (“schema.tabelle”), in die importiert werden soll. |  nein |

### PostgisRasterExport

Exportiert eine PostGIS-Raster-Spalte in eine Raster-Datei mittels
SQL-Query. Die SQL-Query darf nur einen Record zurückliefern, d.h. es
muss unter Umständen `ST_Union()` verwendet werden. Es wird angenommen,
dass die erste *bytea*-Spalte des Resultsets die Rasterdaten enthält.
Weitere *bytea*-Spalten werden ignoriert. Das Outputformat und die
Formatoptionen müssen in der SQL-Datei (in der Select-Query) angegeben
werden, z.B.:

``` sql
SELECT
    1::int AS foo, ST_AsGDALRaster((ST_AsRaster(ST_Buffer(ST_Point(2607880,1228287),10),150, 150)), 'AAIGrid', ARRAY[''], 2056) AS raster
;
```

Beispiel:

``` groovy
tasks.register('exportTiff', PostgisRasterExport) {
    database = [db_uri, db_user, db_pass]
    sqlFile = "raster.sql"
    dataFile = "export.tif"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| dataFile | `Object` | Name der Rasterdatei, die erstellt werden soll. |  nein |
| database | `Connector` | Datenbank, aus der exportiert werden soll. |  nein |
| sqlFile | `String` | Name der SQL-Datei aus der das SQL-Statement gelesen und ausgeführt wird. |  nein |
| sqlParameters | `Map<String,String>` | Eine Map mit Paaren von Parameter-Name und Parameter-Wert. |  ja |

### Publisher

Stellt für Vektordaten die aktuellsten Geodaten-Dateien bereit und
pflegt das Archiv der vorherigen Zeitstände.

[Details](Publisher.md)

### S3Bucket2Bucket

Kopiert Objekte von einem Bucket in einen anderen. Die Buckets müssen in
der gleichen Region sein. Die Permissions werden nicht mitkopiert und
müssen explizit gesetzt werden.

Beispiel:

``` groovy
tasks.register('copyFiles', S3Bucket2Bucket) {
    accessKey = s3AccessKey
    secretKey = s3SecretKey
    sourceBucket = s3SourceBucket
    targetBucket = s3TargetBucket
    acl = "public-read"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| accessKey | `String` | AccessKey |  nein |
| acl | `String` | Access Control Layer `[private, public-read, public-read-write, authenticated-read, aws-exec-read, bucket-owner-read, bucket-owner-full-control]` |  nein |
| endPoint | `String` | S3-Endpunkt. Default: `https://s3.eu-central-1.amazonaws.com/` |  ja |
| metaData | `Map<String,String>` | Metadaten des Objektes resp. der Objekte, z.B. `["lastModified":"2020-08-28"]`. |  ja |
| region | `String` | S3-Region. Default: `eu-central-1` |  ja |
| secretKey | `String` | SecretKey |  nein |
| sourceBucket | `String` | Bucket, aus dem die Objekte kopiert werden. |  nein |
| targetBucket | `String` | Bucket, in den die Objekte kopiert werden. |  nein |

### S3Download

Lädt eine Datei aus einem S3-Bucket herunter.

Beispiel:

``` groovy
tasks.register('downloadFile', S3Download) {
    accessKey = 'abcdefg'
    secretKey = 'hijklmnopqrstuvwxy'
    downloadDir = file("./path/to/dir/")
    bucketName = "ch.so.ada.denkmalschutz"
    key = "foo.pdf"
    endPoint = "https://s3.eu-central-1.amazonaws.com" 
    region = "eu-central-1"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| accessKey | `String` | AccessKey |  nein |
| bucketName | `String` | Name des Buckets, in dem die Datei gespeichert ist. |  nein |
| downloadDir | `File` | Verzeichnis, in das die Datei heruntergeladen werden soll. |  nein |
| endPoint | `String` | S3-Endpunkt. Default: `https://s3.eu-central-1.amazonaws.com/` |  ja |
| key | `String` | Name der Datei. |  nein |
| region | `String` | S3-Region. Default: `eu-central-1` |  ja |
| secretKey | `String` | SecretKey |  nein |

### S3Upload

Lädt ein Dokument (`sourceFile`) oder alle Dokumente in einem
Verzeichnis (`sourceDir`) in einen S3-Bucket (`bucketName`) hoch.

Mit dem passenden Content-Typ kann man das Verhalten des Browsers
steuern. Default ist ‘application/octect-stream’, was dazu führt, dass
die Datei immer heruntergeladen wird. Soll z.B. ein PDF oder ein Bild im
Browser direkt angezeigt werden, muss der korrekte Content-Typ gewählt
werden.

Beispiel:

``` groovy
tasks.register('uploadDirectory', S3Upload) {
    accessKey = 'abcdefg'
    secretKey = 'hijklmnopqrstuvwxy'
    sourceDir = file("./docs")
    bucketName = "ch.so.ada.denkmalschutz"
    endPoint = "https://s3.eu-central-1.amazonaws.com" 
    region = "eu-central-1"
    acl = "public-read"
    contentType = "application/pdf"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| accessKey | `String` | AccessKey |  nein |
| acl | `String` | Access Control Layer `[private, public-read, public-read-write, authenticated-read, aws-exec-read, bucket-owner-read, bucket-owner-full-control]` |  nein |
| bucketName | `String` | Name des Buckets, in dem die Dateien gespeichert werden sollen. |  nein |
| contentType | `String` | Content-Type |  ja |
| endPoint | `String` | S3-Endpunkt. Default: `https://s3.eu-central-1.amazonaws.com/` |  ja |
| metaData | `Map<String,String>` | Metadaten des Objektes resp. der Objekte, z.B. `["lastModified":"2020-08-28"]` |  ja |
| region | `String` | S3-Region. `Default: eu-central-1` |  nein |
| secretKey | `String` | SecretKey |  nein |
| sourceDir | `Object` | Verzeichnis mit den Dateien, die hochgeladen werden sollen. |  ja |
| sourceFile | `Object` | Datei, die hochgeladen werden soll. |  ja |
| sourceFiles | `Object` | `FileCollection` mit den Dateien, die hochgeladen werden sollen, z.B. `fileTree("/path/to/directoy/") { include "*.itf" }` |  ja |

### ShpExport

Daten aus einer bestehenden Datenbanktabelle werden in eine SHP-Datei
exportiert.

Beispiel:

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('shpexport', ShpExport) {
    database = [db_uri, db_user, db_pass]
    schemaName = "shpexport"
    tableName = "exportdata"
    dataFile = "data.shp"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| dataFile | `Object` | Name der SHP-Datei, die erstellt werden soll. |  nein |
| database | `Connector` | Datenbank, aus der exportiert werden soll. |  nein |
| encoding | `String` | Zeichencodierung der SHP-Datei, z.B. `UTF-8`. Default: Systemeinstellung |  ja |
| schemaName | `String` | Name des DB-Schemas, in dem die DB-Tabelle ist. |  ja |
| tableName | `String` | Name der DB-Tabelle, die exportiert werden soll. |  nein |

### ShpImport

Daten aus einer SHP-Datei in eine bestehende Datenbanktabelle
importieren.

Die Tabelle kann weitere Spalten enthalten, die in der SHP-Datei nicht
vorkommen. Sie müssen aber NULLable sein, oder einen Default-Wert
definiert haben.

Die Tabelle muss eine Geometriespalte enthalten. Der Name der
Geometriespalte kann beliebig gewählt werden.

Die Gross-/Kleinschreibung der SHP-Spaltennamen wird für die Zuordnung
zu den DB-Spalten ignoriert.

Beispiel:

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('shpimport', ShpImport) {
    database = [db_uri, db_user, db_pass]
    schemaName = "shpimport"
    tableName = "importdata"
    dataFile = "data.shp"
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| dataFile | `Object` | Name der SHP-Datei, die erstellt werden soll. |  nein |
| database | `Connector` | Datenbank, aus der exportiert werden soll. |  nein |
| encoding | `String` | Zeichencodierung der SHP-Datei, z.B. `UTF-8`. Default: Systemeinstellung |  ja |
| schemaName | `String` | Name des DB-Schemas, in dem die DB-Tabelle ist. |  ja |
| tableName | `String` | Name der DB-Tabelle, die exportiert werden soll. |  nein |

### ShpValidator

Prüft eine SHP-Datei gegenüber einem INTERLIS-Modell. Basiert auf dem
[*ilivalidator*](https://github.com/claeis/ilivalidator).

Im gegebenen Modell wird eine Klasse gesucht, die genau die
Attributenamen wie in der Shp-Datei enthält (wobei die
Gross-/Kleinschreibung ignoriert wird); die Attributtypen werden
ignoriert. Wird keine solche Klasse gefunden, gilt das als
Validierungsfehler.

Die Prüfung von gleichzeitig mehreren Shapefiles führt zu
Fehlermeldungen wie
`OID o3158 of object <Modelname>.<Topicname>.<Klassenname> already exists in ...`.
Beim Öffnen und Lesen eines Shapefiles wird immer der Zähler, der die
interne (im Shapefile nicht vorhandene) `OID` generiert, zurückgesetzt.
Somit kann immer nur ein Shapefile pro Task geprüft werden.

Beispiel:

``` groovy
tasks.register('validate', ShpValidator) {
    models = "ShpModel"
    dataFiles = ["data.shp"]
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| allObjectsAccessible | `Boolean` | Mit der Option nimmt der Validator an, dass er Zugriff auf alle Objekte hat. D.h. es wird z.B. auch die Multiplizität von Beziehungen auf externe Objekte geprüft. Default: false |  ja |
| configFile | `Object` | Konfiguriert die Datenprüfung mit Hilfe einer ini-Datei (um z.B. die Prüfung von einzelnen Constraints auszuschalten). Siehe https://github.com/claeis/ilivalidator/blob/master/docs/ilivalidator.rst#konfiguration |  ja |
| dataFiles | `Object` | Liste der Dateien, die validiert werden sollen. `FileCollection` oder `List`. Eine leere Liste ist kein Fehler. |  nein |
| disableAreaValidation | `Boolean` | Schaltet die AREA-Topologieprüfung aus. Default: false |  ja |
| encoding | `String` | Zeichencodierung der SHP-Datei, z.B. `UTF-8`. Default: Systemeinstellung |  ja |
| failOnError | `Boolean` | Steuert, ob der Task bei einem Validierungsfehler fehlschlägt. Default: true |  ja |
| forceTypeValidation | `Boolean` | Ignoriert die Konfiguration der Typprüfung aus der TOML-Datei, d.h. es kann nur die Multiplizität aufgeweicht werden. Default: false |  ja |
| logFile | `Object` | Schreibt die log-Meldungen der Validierung in eine Text-Datei. |  ja |
| metaConfigFile | `Object` | Konfiguriert den Validator mit Hilfe einer ini-Datei. Siehe https://github.com/claeis/ilivalidator/blob/master/docs/ilivalidator.rst#konfiguration |  ja |
| modeldir | `String` | INTERLIS-Modellrepository. String separiert mit Semikolon (analog ili2db, ilivalidator). |  ja |
| models | `String` | INTERLIS-Modell, gegen das die Dateien geprüft werden sollen (mehrere Modellnamen durch Semikolon trennen). Default: Der Name der CSV-Datei. |  ja |
| multiplicityOff | `Boolean` | Schaltet die Prüfung der Multiplizität generell aus. Default: false |  ja |
| pluginFolder | `Object` | Verzeichnis mit JAR-Dateien, die Zusatzfunktionen enthalten. |  ja |
| proxy | `String` | Proxy-Server für den Zugriff auf Modell-Repositories. |  ja |
| proxyPort | `Integer` | Proxy-Port für den Zugriff auf Modell-Repositories. |  ja |
| skipPolygonBuilding | `Boolean` | Schaltet die Bildung der Polygone aus (nur ITF). Default: false |  ja |
| validationOk | `boolean` | OUTPUT: Ergebnis der Validierung. Nur falls failOnError=false. |  nein |
| xtflogFile | `Object` | Schreibt die log-Meldungen in eine INTERLIS-2-Datei. Die Datei result.xtf entspricht dem Modell IliVErrors. |  ja |

### SqlExecutor

Der SqlExecutor-Task dient dazu, Datenumbauten auszuführen.

Er wird im Allgemeinen dann benutzt, wenn

1.  der Datenumbau komplex ist und deshalb nicht im Db2Db-Task erledigt
    werden kann
2.  oder wenn die Quell-DB keine PostgreSQL-DB ist (weil bei komplexen
    Queries für den Datenumbau möglicherweise fremdsystemspezifische
    SQL-Syntax verwendet werden müsste)
3.  oder wenn Quell- und Zielschema in derselben Datenbank liegen

In den Fällen 1 und 2 werden Stagingtabellen bzw. ein Stagingschema
benötigt, in welche der Db2Db-Task die Daten zuerst 1:1 hineinschreibt.
Der SqlExecutor-Task liest danach die Daten von dort, baut sie um und
schreibt sie dann ins Zielschema. Die Queries für den SqlExecutor-Task
können alle in einem einzelnen .sql-File sein oder (z.B. aus Gründen der
Strukturierung oder Organisation) auf mehrere .sql-Dateien verteilt
sein. Die Reihenfolge der .sql-Dateien ist relevant. Dies bedeutet, dass
die SQL-Befehle des zuerst angegebenen .sql-Datei zuerst ausgeführt
werden müssen, danach dies SQL-Befehle des an zweiter Stelle angegebenen
.sql-Datei, usw.

Der SqlExecutor-Task muss neben Updates ganzer Tabellen (d.h. Löschen
des gesamten Inhalts einer Tabelle und gesamter neuer Stand in die
Tabelle schreiben) auch Updates von Teilen von Tabellen zulassen. D.h.
es muss z.B. möglich sein, innerhalb einer Tabelle nur die Objekte einer
bestimmten Gemeinde zu aktualisieren. Darum ist es möglich innerhalb der
.sql-Datei Paramater zu verwenden und diesen Parametern beim Task einen
konkreten Wert zuzuweisen. Innerhalb der .sql-Datei werden Paramter mit
folgender Syntax verwendet: `${paramName}`.

Unterstützte Datenbanken: PostgreSQL, SQLite, Oracle, Derby und DuckDB.

Beispiele:

``` groovy
def db_uri = 'jdbc:postgresql://localhost:54321/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('executeSomeSql', SqlExecutor) {
    database = [db_uri, db_user, db_pass]
    sqlParameters = [dataset:'Olten']
    sqlFiles = ['demo.sql']
}
```

Damit mit einer einzigen Task-Definition mehrere Datensätze verarbeitet
werden können, kann auch eine Liste von Parametern angegeben werden.

``` groovy
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

tasks.register('executeSomeSql', SqlExecutor) {
    database = [db_uri, db_user, db_pass]
    sqlParameters = [[dataset:'Olten'],[dataset:'Grenchen']]
    sqlFiles = ['demo.sql']
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| database | `ListProperty<String>` | Datenbank, in die importiert werden soll. |  nein |
| sqlFiles | `ListProperty<String>` | Name der SQL-Datei aus der SQL-Statements gelesen und ausgeführt werden. |  nein |
| sqlParameters | `Property<Object>` | Eine Map mit Paaren von Parameter-Name und Parameter-Wert (`Map<String,String>`). Oder eine Liste mit Paaren von Parameter-Name und Parameter-Wert (`List<Map<String,String>>`). |  ja |

### XslTransformer (incubating)

Transformiert eine Datei mittels einer XSL-Transformation ein eine
andere Datei. Ist der `xslFile`-Parameter ein String, wird erwartet,
dass die Datei im GRETL-Verzeichnis im Ressourcenordner
`src/main/resources/xslt`-Verzeichnis gespeichert ist. Falls der
`xslFile`-Parameter ein File-Objekt ist, können lokale Dateien verwendet
werden.

Beispiele:

``` groovy
tasks.register('transform', XslTransformer) {
    xslFile = "eCH0132_to_SO_AGI_SGV_Meldungen_20221109.xsl"
    xmlFile = file("MeldungAnGeometer_G-0111102_20221103_145001.xml")
    outDirectory = file(".")
}
```

``` groovy
tasks.register('transform', XslTransformer) {
    xslFile = file("path/to/eCH0132_to_SO_AGI_SGV_Meldungen_20221109.xsl")
    xmlFile = file("MeldungAnGeometer_G-0111102_20221103_145001.xml")
    outDirectory = file(".")
}
```

``` groovy
tasks.register('transform', XslTransformer) {
    xslFile = "eCH0132_to_SO_AGI_SGV_Meldungen_20221109.xsl"
    xmlFile = fileTree(".").matching {
        include "*.xml"
    }
    outDirectory = file(".")
}
```

| Parameter | Datentyp | Beschreibung | Optional |
|----|----|----|----|
| fileExtension | `String` | Fileextension der Resultatdatei. Default: `xtf` |  ja |
| outDirectory | `File` | Verzeichnis, in das die transformierte Datei gespeichert wird. Der Name der transformierten Datei entspricht standardmässig dem Namen der Input-Datei mit Endung `.xtf`. |  nein |
| xmlFile | `Object` | Datei oder FileTree, die/der transformiert werden soll. |  nein |
| xslFile | `Object` | Name der XSLT-Datei, die im `src/main/resources/xslt`-Verzeichnis liegen muss oder File-Objekt (beliebiger Pfad). |  nein |
