# GRETL-Benutzer-Handbuch

Das Datenmanagement-Tool _GRETL_ ist ein Werkzeug, das für Datenimports, Datenumbauten
(Modellumbau) und Datenexports eingesetzt wird. _GRETL_ führt Jobs aus, wobei ein Job aus
mehreren atomaren Tasks besteht. Damit ein Job als vollständig ausgeführt gilt, muss jeder zum
Job gehörende Task vollständig ausgeführt worden sein. Schlägt ein Task fehl, gilt auch der Job
als fehlgeschlagen.

Ein Job besteht aus einem oder mehreren Tasks, die gemäss einem gerichteten Graphen (Directed Acyclic Graph; DAG)
miteinander verknüpft sind.

Ein Job kann aus z.B. aus einer linearen Kette von Tasks bestehen:

    Task 1 – Task 2 – Task 3 – Task n

Beispiel: Datenimport aus INTERLIS-Datei – Datenumbau – Datenexport nach Shapefile.

Ein Job kann sich nach einem Task aber auch auf zwei oder mehr verschiedene weitere Tasks
verzweigen:

          - Task 2 – Task 3 – Task n
    Task 1 –
          – Task 4 – Task 5 – Task m

Beispiel: Datenimport aus INTERLIS-Datei – Datenumbau in Zielschema 1 und ein zweiter
Datenumbau in Zielschema 2.

Es ist auch möglich, dass zuerst zwei oder mehr Tasks unabhängig voneinander
ausgeführt werden müssen, bevor ein einzelner weiterer Task ausgeführt wird.

    Task 1 –
           – Task 3 – Task 4 – Task n
    Task 2 –

Die Tasks eines Jobs werden per Konfigurationsfile konfiguriert.

## Kleines Beispiel

Erstellen sie in einem neuen Verzeichnis ``gretldemo`` eine neue Datei ``build.gradle``:

```
import ch.so.agi.gretl.tasks.*
import ch.so.agi.gretl.api.*

apply plugin: 'ch.so.agi.gretl'

buildscript {
    repositories {
        maven { url "http://jars.interlis.ch" }
        maven { url "http://jars.umleditor.org" }
        maven { url "https://repo.osgeo.org/repository/release/" }
        maven { url "https://plugins.gradle.org/m2/" }
        mavenCentral()
    }
    dependencies {
        classpath group: 'ch.so.agi', name: 'gretl',  version: '2.2.+'
    }
}

defaultTasks 'validate'


task validate(type: IliValidator){
    dataFiles = ["BeispielA.xtf"]
}
```

Die Datei ``build.gradle`` ist die Job-Konfiguration. Dieser kleine Beispiel-Job besteht nur aus einem einzigen Task: ``validate``.

Erstellen Sie nun noch die Datei ``BeispielA.xtf`` (damit danach der Job erfolgreich ausgeführt werden kann).

```
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

Um den Job auszuführen, wechseln Sie ins Verzeichnis mit der Job-Konfiguration, und geben da das Kommando ``gradle`` ohne 
Argument ein:

    cd gretldemo
    gradle

Sie sollten etwa folgende Ausgabe erhalten:

```
Starting a Gradle Daemon, 1 incompatible and 1 stopped Daemons could not be reused, use --status for details
Download http://jars.umleditor.org/ch/so/agi/gretl/maven-metadata.xml
Download http://jars.umleditor.org/ch/so/agi/gretl/1.0.4-SNAPSHOT/maven-metadata.xml
Download http://jars.umleditor.org/ch/so/agi/gretl/1.0.4-SNAPSHOT/gretl-1.0.4-20180104.152357-34.jar

BUILD SUCCESSFUL in 21s
```

``BUILD SUCCESSFUL`` zeigt an, dass der Job (die Validierung der Datei ``BeispielA.xtf``) erfolgreich ausgeführt wurde.

Um dieselbe Job-Konfiguration für verschiedene Datensätze verwenden zu können, muss es parametrisierbar sein. Die Jobs/Tasks können so generisch konfiguriert werden, dass dieselbe Konfiguration z.B. für 
Daten aus verschiedenen Gemeinden benutzt
werden kann. Parameter für die Job Konfiguration können z.B. mittels gradle-Properties 
([Gradle properties and system properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_properties_and_system_properties)) 
dem Job mitgegeben werden, also z.B. 

    cd gretldemo
    gradle -Pdataset=Olten

## Systemanforderungen
Um die aktuelle Version von gretl auszuführen, muss 

 - die Java-Laufzeitumgebung (JRE), Version 1.8 oder neuer, und 
 - gradle, Version 5.1 oder neuer, auf Ihrem System installiert sein.
 
Die Java-Laufzeitumgebung (JRE) kann auf der Website http://www.java.com/ gratis bezogen werden.

Die gradle-Software kann auf der Website http://www.gradle.org/ gratis bezogen werden.

Um _GRETL_ laufen zu lassen, benötigen sie typischerweise eine Internetverbindung (Ein Installation, die keine Internetverbindung benötigt ist auch möglich, aber aufwendig).

## Installation
_GRETL_ selbst muss nicht explizit installiert werden, sondern wird dynamisch durch das Internet bezogen.

## Ausführen
Um gretl auszuführen, geben Sie auf der Kommandozeile folgendes Kommando ein (wobei ``jobfolder`` der absolute Pfad 
zu ihrem Verzeichnis mit der Job Konfiguration ist.)

    gradle --project-dir jobfolder
    
Alternativ können Sie auch ins Verzeichnis mit der Job Konfiguration wechseln, und da das Kommando ``gradle`` ohne 
Argument verwenden:

    cd jobfolder
    gradle


## Tasks

### Av2ch

Transformiert eine INTERLIS1-Transferdatei im kantonalen AV-DM01-Modell in das Bundesmodell. Unterstützt werden die Sprachen _Deutsch_ und _Italienisch_ und der Bezugrahmen _LV95_. Getestet mit Daten aus den Kantonen Solothurn, Glarus und Tessin. Weitere Informationen sind in der Basisbibliothek zu finden: [https://github.com/sogis/av2ch](https://github.com/sogis/av2ch).

Das Bundes-ITF hat denselben Namen wie das Kantons-ITF.

Aufgrund der sehr vielen Logging-Messages einer verwendeten Bibliothek, wird der `System.err`-Ouput nach `dev/null` [https://github.com/sogis/av2ch/blob/master/src/main/java/ch/so/agi/av/Av2ch.java#L75](gemappt).

```
task transform(type: Av2ch) {
    inputFile = file("254900.itf")
    outputDirectory = file("output")
}
```

Parameter | Beschreibung
----------|-------------------
inputFile   | Name der zu transformierenden ITF-Datei.
outputDirectory  | Name des Verzeichnisses in das die zu erstellende Datei geschrieben wird.
modeldir | INTERLIS-Modellablage. String separiert mit Semikolon (analog ili2db, ilivalidator).
zip | Die zu erstellende Datei wird gezippt (Default: false).

### Av2geobau

Av2geobau konvertiert eine Interlis-Transferdatei (itf) in eine DXF-Geobau Datei. 
Av2geobau funktioniert ohne Datenbank.

Die ITF-Datei muss dem Modell DM01AVCH24LV95D entsprechen. Die Daten werden nicht validiert.

Die Datenstruktur der DXF-Datei ist im Prinzip sehr einfach: Die verschiedenen Informationen aus dem Datenmodell DM01 werden in verschiedene DXF-Layer abgebildet, z.B. die begehbaren LFP1 werden in den Layer "01111" abgebildet. Oder die Gebäude in den Layer "01211".

Der Datenumbau ist nicht konfigurierbar.

```
task av2geobau(type: Av2geobau){
    itfFiles = "ch_254900.itf"
    dxfDirectory = "./out/"
}
```

Es können auch mehrere Dateien angegeben werden.

```
task av2geobau(type: Av2geobau){
    itfFiles = fileTree(".").matching {
        include"*.itf"
    }
    dxfDirectory = "./out/"
}
```

Parameter    | Beschreibung
-------------|-------------------
itfFiles     | ITF-Datei, die nach DXF transformiert werden soll. Es können auch mehrere Dateien angegeben werden.
dxfDirectory | Verzeichnis, in das die DXF-Dateien gespeichert werden.
modeldir     | Dateipfade, die Modell-Dateien (ili-Dateien) enthalten. Mehrere Pfade können durch Semikolon ‚;‘ getrennt werden. Es sind auch URLs von Modell-Repositories möglich. Default: ``%ITF_DIR;http://models.interlis.ch/``. ``%ITF_DIR`` ist ein Platzhalter für das Verzeichnis mit der ITF-Datei.
logFile      | Schreibt die log-Meldungen der Konvertierung in eine Text-Datei.
proxy        | Proxy Server für den Zugriff auf Modell Repositories
proxyPort    | Proxy Port für den Zugriff auf Modell Repositories
zip          | Die zu erstellende Datei wird gezippt und es werden zusätzliche Dateien (Musterplan, Layerbeschreibung, Hinweise) hinzugefügt (Default: false).

### Csv2Excel (incubating)
Konvertiert eine CSV-Datei in eine Excel-Datei (*.xlsx). Datentypen werden anhand eines INTERLIS-Modelles eruiert. Fehlt das Modell, wird alles als Text gespeichert. Die Daten werden vollständig im Speicher vorgehalten. Falls grosse Dateien geschrieben werden müssen, kann das zu Problemen führen. Dann müsste die Apache POI SXSSF Implementierung (Streaming) verwendet werden.

Beispiel:
```
task convertData(type: Csv2Excel) {
    csvFile = file("./20230124_sap_Gebaeude.csv")
    firstLineIsHeader = true
    valueDelimiter = null
    valueSeparator = ";"
    encoding = "ISO-8859-1";
    models = "SO_HBA_Gebaeude_20230111";
    outputDir = file(".");
}
```

Parameter | Beschreibung
----------|-------------------
csvFile | CSV-Datei, die konvertiert werden soll.
firstLineIsHeader | Definiert, ob eine Headerzeile geschrieben werden soll, oder nicht. Default: true
valueDelimiter | Zeichen, das am Anfang und Ende jeden Wertes geschrieben werden soll. Default ``"``
valueSeparator | Zeichen, das als Trennzeichen zwischen den Werten verwendet werden soll. Default: ``,``
encoding | Zeichencodierung der CSV-Datei, z.B. ``"UTF-8"``. Default: Systemeinstellung
models | INTERLIS-Modell für Definition der Datentypen in der Excel-Datei.
modeldir | Dateipfade, die Modell-Dateien (ili-Dateien) enthalten. Mehrere Pfade können durch Semikolon ";" getrennt werden. Es sind auch URLs von Modell-Repositories möglich.
outputDir | Verzeichnis, in das die Excel-Datei gespeichert wird. Default: Verzeichnis, in dem die CSV-Datei vorliegt.

### Csv2Parquet (incubating)
Konvertiert eine CSV-Datei in eine Parquet-Datei. Datentypen werden anhand eines INTERLIS-Modelles eruiert. Fehlt das Modell, wird alles als Text gespeichert.

Beispiel:
```
task convertData(type: Csv2Parquet) {
    csvFile = file("./20230124_sap_Gebaeude.csv")
    firstLineIsHeader = true
    valueDelimiter = null
    valueSeparator = ";"
    encoding = "ISO-8859-1";
    models = "SO_HBA_Gebaeude_20230111";
    outputDir = file(".");
}
```

Parameter | Beschreibung
----------|-------------------
csvFile | CSV-Datei, die konvertiert werden soll.
firstLineIsHeader | Definiert, ob eine Headerzeile geschrieben werden soll, oder nicht. Default: true
valueDelimiter | Zeichen, das am Anfang und Ende jeden Wertes geschrieben werden soll. Default ``"``
valueSeparator | Zeichen, das als Trennzeichen zwischen den Werten verwendet werden soll. Default: ``,``
encoding | Zeichencodierung der CSV-Datei, z.B. ``"UTF-8"``. Default: Systemeinstellung
models | INTERLIS-Modell für Definition der Datentypen in der Parquet-Datei.
modeldir | Dateipfade, die Modell-Dateien (ili-Dateien) enthalten. Mehrere Pfade können durch Semikolon ";" getrennt werden. Es sind auch URLs von Modell-Repositories möglich.
outputDir | Verzeichnis, in das die Parquet-Datei gespeichert wird. Default: Verzeichnis, in dem die CSV-Datei vorliegt.

### CsvExport
Daten aus einer bestehenden Datenbanktabelle werden in eine CSV-Datei exportiert.

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task csvexport(type: CsvExport){
    database = [db_uri, db_user, db_pass]
    schemaName = "csvexport"
    tableName = "exportdata"
    firstLineIsHeader=true
    attributes = [ "t_id","Aint"]
    dataFile = "data.csv"
}
```

Parameter | Beschreibung
----------|-------------------
database | Datenbank aus der exportiert werden soll.
dataFile  | Name der CSV-Datei, die erstellt werden soll.
tableName | Name der DB-Tabelle, die exportiert werden soll
schemaName | Name des DB-Schemas, in dem die DB-Tabelle ist.
firstLineIsHeader | Definiert, ob eine Headerzeile geschrieben werden soll, oder nicht. Default: true
valueDelimiter | Zeichen, das am Anfang und Ende jeden Wertes geschrieben werden soll. Default ``"``
valueSeparator | Zeichen, das als Trennzeichen zwischen den Werten verwendet werden soll. Default: ``,``
attributes | Spalten der DB-Tabelle, die exportiert werden sollen. Definiert die Reihenfolge der Spalten in der CSV-Datei. Default: alle Spalten
encoding | Zeichencodierung der CSV-Datei, z.B. ``"UTF-8"``. Default: Systemeinstellung

Geometriespalten können nicht exportiert werden.

### CsvImport
Daten aus einer CSV-Datei werden in eine bestehende Datenbanktabelle importiert.

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task csvimport(type: CsvImport){
    database = [db_uri, db_user, db_pass]
    schemaName = "csvimport"
    tableName = "importdata"
    firstLineIsHeader=true
    dataFile = "data1.csv"
}
```

Parameter | Beschreibung
----------|-------------------
database | Datenbank in die importiert werden soll
dataFile  | Name der CSV-Datei, die gelesen werden soll
tableName | Name der DB-Tabelle, in die importiert werden soll
schemaName | Name des DB-Schemas, in dem die DB-Tabelle ist.
firstLineIsHeader | Definiert, ob die CSV-Datei einer Headerzeile hat, oder nicht. Default: true
valueDelimiter | Zeichen, das am Anfang und Ende jeden Wertes vorhanden ist. Default ``"``
valueSeparator | Zeichen, das als Trennzeichen zwischen den Werten interpretiert werden soll. Default: ``,``
encoding | Zeichencodierung der CSV-Datei, z.B. ``"UTF-8"``. Default: Systemeinstellung
batchSize | Anzahl der Records, die pro Batch in die Ziel-Datenbank geschrieben werden (Standard: 5000). 

Die Tabelle kann weitere Spalten enthalten, die in der CSV-Datei nicht vorkommen. Sie müssen
aber NULLable sein, oder einen Default-Wert definiert haben.

Geometriepalten können nicht importiert werden.

Die Gross-/Kleinschreibung der CSV-Spaltennamen wird für die Zuordnung zu den DB-Spalten ignoriert.

### CsvValidator

Prüft eine CSV-Datei gegenüber einem INTERLIS-Modell. Basiert auf dem [_ilivalidator_](https://github.com/claeis/ilivalidator). Das Datenmodell darf die OID nicht als UUID modellieren (`OID AS INTERLIS.UUIDOID`). 

Beispiel:
```
task validate(type: CsvValidator){
    models = "CsvModel"
    firstLineIsHeader=true
    dataFiles = ["data1.csv"]
}
```

Parameter | Beschreibung
----------|-------------------
dataFiles | Liste der CSV-Dateien, die validiert werden sollen. Eine leere Liste ist kein Fehler.
models | INTERLIS-Modell, gegen das die Dateien geprüft werden sollen (mehrere Modellnamen durch Semikolon trennen). Default: Der Name der CSV-Datei.
modeldir | Dateipfade, die Modell-Dateien (ili-Dateien) enthalten. Mehrere Pfade können durch Semikolon ‚;‘ getrennt werden. Es sind auch URLs von Modell-Repositories möglich. Default: ``%XTF_DIR;http://models.interlis.ch/``. ``%XTF_DIR`` ist ein Platzhalter für das Verzeichnis mit der CSV-Datei.
configFile | Konfiguriert die Datenprüfung mit Hilfe einer TOML-Datei (um z.B. die Prüfung von einzelnen Constraints auszuschalten). siehe https://github.com/claeis/ilivalidator/blob/master/docs/ilivalidator.rst#konfiguration
forceTypeValidation | Ignoriert die Konfiguration der Typprüfung aus der TOML-Datei, d.h. es kann nur die Multiplizität aufgeweicht werden. Default: false
disableAreaValidation | Schaltet die AREA-Topologieprüfung aus. Default: false
multiplicityOff | Schaltet die Prüfung der Multiplizität generell aus. Default: false
allObjectsAccessible | Mit der Option nimmt der Validator an, dass er Zugriff auf alle Objekte hat. D.h. es wird z.B. auch die Multiplizität von Beziehungen auf externe Objekte geprüft. Default: false
skipPolygonBuilding | Schaltet die Bildung der Polygone aus (nur ITF). Default: false
logFile | Schreibt die log-Meldungen der Validierung in eine Text-Datei.
xtflogFile | Schreibt die log-Meldungen in eine INTERLIS 2-Datei. Die Datei result.xtf entspricht dem Modell [IliVErrors](http://models.interlis.ch/models/tools/IliVErrors.ili).
pluginFolder | Verzeichnis mit JAR-Dateien, die Zusatzfunktionen enthalten. 
proxy | Proxy Server für den Zugriff auf Modell Repositories
proxyPort | Proxy Port für den Zugriff auf Modell Repositories
failOnError |  Steuert, ob der Task bei einem Validierungsfehler fehlschlägt. Default: true
validationOk | OUTPUT: Ergebnis der Validierung. Nur falls failOnError=false
firstLineIsHeader | Definiert, ob die CSV-Datei einer Headerzeile hat, oder nicht. Default: true
valueDelimiter | Zeichen, das am Anfang und Ende jeden Wertes vorhanden ist. Default ``"``
valueSeparator | Zeichen, das als Trennzeichen zwischen den Werten interpretiert werden soll. Default: ``,``
encoding | Zeichencodierung der CSV-Datei, z.B. ``"UTF-8"``. Default: Systemeinstellung

Falls die CSV-Datei eine Header-Zeile enthält (mit den Spaltennamen), wird im gegebenen Modell eine Klasse gesucht, 
welche die Header-Spaltennamen enthält (Gross-/Kleinschreibung sowie optionale "Spalten" der Modell-Klasse werden ignoriert). 
Wird keine solche Klasse gefunden, gilt das als Validierungsfehler.

Falls die CSV-Datei keine Header-Zeile enthält (mit den Spaltennamen), wird im gegebenen Modell eine Klasse gesucht, 
die die selbe Anzahl Attribute hat. Wird keine solche Klasse gefunden, 
gilt das als Validierungsfehler.

Die Prüfung von gleichzeitig mehreren CSV-Dateien führt zu Fehlermeldungen wie `OID o3158 of object <Modelname>.<Topicname>.<Klassenname> already exists in ...`. Beim Öffnen und Lesen einer CSV-Datei wird immer der Zähler, der die interne (in der CSV-Datei nicht vorhandene) `OID` generiert, zurückgesetzt. Somit kann immer nur eine CSV-Datei pro Task geprüft werden.

### Curl

Simuliert mit einem HttpClient einige Curl-Befehle.

Beispiele:

```
import ch.so.agi.gretl.tasks.Curl.MethodType;

task uploadData(type: Curl) {
    serverUrl = "https://geodienste.ch/data_agg/interlis/import"
    method = MethodType.POST
    formData = ["topic": "npl_waldgrenzen", "lv95_file": file("./test.xtf.zip"), "publish": "true", "replace_all": "true"]
    user = "fooUser"
    password = "barPwd"
    expectedStatusCode = 200
    expectedBody = "\"success\":true"
}
```

```
import ch.so.agi.gretl.tasks.Curl.MethodType;

task uploadData(type: Curl) {
    serverUrl = "https://testweb.so.ch/typo3/api/digiplan"
    method = MethodType.POST
    headers = ["Content-Type": "application/xml", "Content-Encoding": "gzip"]
    dataBinary = file("./planregister.xml.gz")
    user = "fooUser"
    password = "barPwd"
    expectedStatusCode = 202
}
```

```
import ch.so.agi.gretl.tasks.Curl.MethodType;

task downloadData(type: Curl) {
    serverUrl = "https://raw.githubusercontent.com/sogis/gretl/master/README.md"
    method = MethodType.GET
    outputFile = file("./README.md")
    expectedStatusCode = 200
}
```

Parameter | Beschreibung
----------|-------------------
serverUrl | Die URL des Servers inklusive Pfad und Queryparameter.
method |  HTTP-Request-Methode. Unterstützt werden `GET` und `POST`.
expectedStatusCode | Erwarteter Status Code, der vom Server zurückgeliefert wird.
expectedBody | Erwarteter Text, der vom Server als Body zurückgelieferd wird. (optional)
formData | Form data parameters. Entspricht `curl [URL] -F key1=value1 -F file1=@my_file.xtf`. (optional)
dataBinary | Datei, die hochgeladen werden soll. Entspricht `curl [URL] --data-binary`. (optional)
headers | Request-Header. Entspricht `curl [URL] -H ... -H ...`. (optional)
user | Benutzername. Wird zusammen mit `password` in einen Authorization-Header umgewandelt. Entspricht `curl [URL] -u user:password`. (optional)
password | Passwort. Wird zusammen mit `user` in einen Authorization-Header umgewandelt. Entspricht `curl [URL] -u user:password`. (optional)
outputFile | Datei, in die der Output gespeichert wird. Entspricht `curl [URL] -o`. (optional)

### DatabaseDocumentExport (Experimental)

Speichert Dokumente, deren URL in einer Spalte einer Datenbanktabelle gespeichert sind, in einem lokalen Verzeichnis. Zukünftig und bei Bedarf kann der Task so erweitert werden, dass auch BLOBs aus der Datenbank gespeichert werden können.

Redirect von HTTP nach HTTPS funktionieren nicht. Dies [korrekterweise](https://stackoverflow.com/questions/1884230/httpurlconnection-doesnt-follow-redirect-from-http-to-https) (?) wegen der verwendeten Java-Bibliothek.

Wegen der vom Kanton Solothurn eingesetzten self-signed Zertifikate muss ein unschöner Handstand gemacht werden. Leider kann dieser Usecase schlecht getestet werden, da die Links nur in der privaten Zone verfügbar sind und die zudem noch häufig ändern können. Manuell getestet wurde es jedoch.

Als Dateiname wird der letzte Teil des URL-Pfades verwendet, z.B. `https://artplus.verw.rootso.org/MpWeb-apSolothurnDenkmal/download/2W8v0qRZQBC0ahDnZGut3Q?mode=gis` wird mit den Prefix und Extension zu `ada_2W8v0qRZQBC0ahDnZGut3Q.pdf`.

Es wird `DISTINCT ON (<documentColumn>)` und ein Filter `WHERE <documentColumn> IS NOT NULL` verwendet.

```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task exportDocuments(type: DatabaseDocumentExport){
    database = [db_uri, db_user, db_pass]
    qualifiedTableName = "ada_denkmalschutz.fachapplikation_rechtsvorschrift_link"
    documentColumn = "multimedia_link"
    targetDir = file(".")
    fileNamePrefix = "ada_"
    fileNameExtension = "pdf"
}
```

Parameter | Beschreibung
----------|-------------------
database | Datenbank aus der die Dokumente exportiert werden sollen.
qualifiedTableName  | Qualifizierter Tabellenname
documentColumn | DB-Tabellenspalte mit dem Dokument resp. der URL zum Dokument.
targetDir | Verzeichnis in das die Dokumente exportiert werden sollen.
fileNamePrefix | Prefix für Dateinamen (optional)
fileNameExtension | Dateinamen-Extension (optional)

### Db2Db

Dies ist prinzipiell ein 1:1-Datenkopie, d.h. es findet kein Datenumbau statt, die Quell- und die Ziel-
Tabelle hat jeweils identische Attribute. Es werden auf Seite Quelle in der Regel also simple
SELECT-Queries ausgeführt und die Resultate dieser Queries in Tabellen der Ziel-DB eingefügt.
Unter bestimmten Bedingungen (insbesondere wenn es
sich um einen wenig komplexen Datenumbau handelt), kann dieser Task aber auch zum
Datenumbau benutzt werden.

Die Queries können auf mehrere .sql-Dateien verteilt werden, d.h. der Task muss die Queries mehrerer .sql-Dateien
zu einer Transaktion kombinieren können. Jede .sql-Datei gibt genau eine Resultset (RAM-Tabelle)
zurück. Das Resultset wird in die konfigurierte Zieltabelle geschrieben. Die
Beziehungen sind: Eine bis mehrere Quelltabellen ergeben ein Resultset; das Resultset entspricht
bezüglich den Attributen genau der Zieltabelle und wird 1:1 in diese geschrieben. Der Db2Db-
Task verarbeitet innerhalb einer Transaktion 1-n Resultsets und wird entsprechend auch mit 1-n
SQL-Dateien konfiguriert.

Die Reihenfolge der .sql-Dateien ist relevant. Dies bedeutet, dass die SQL-Befehle der zuerst
angegebenen .sql-Datei zuerst ausgeführt werden müssen, danach die SQL-Befehle der an
zweiter Stelle angegebenen .sql-Datei, usw.

Es ist auch möglich, in den .sql-Dateien mehr als nur ein SELECT-Query zu formulieren, z.B.
ein vorgängiges DELETE.

Alle SELECT-Statements werden in einer Transaktion ausgeführt werden, damit ein konsistenter
Datenstand gelesen wird. Alle INSERT-Statements werden in einer Transaktion ausgeführt
werden, damit bei einem Fehler der bisherige Datenstand bestehen bleibt und also kein
unvollständiger Import zurückgelassen wird.

Damit dieselbe .sql-Datei für verschiedene Datensätze benutzt werden kann, ist es möglich 
innerhalb der .sql-Datei Parameter zu verwenden und diesen Parametern beim Task einen
konkreten Wert zuzuweisen. Innerhalb der .sql-Datei werden Paramter mit folgender Syntax
verwendet: ``${paramName}``.

```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task transferSomeData(type: Db2Db) {
    sourceDb = [db_uri, db_user, db_pass]
    targetDb = ['jdbc:sqlite:gretldemo.sqlite',null,null]
    sqlParameters = [dataset:'Olten']
    transferSets = [
        new TransferSet('some.sql', 'albums_dest', true)
    ];
}
```
Damit mit einer einzigen Task-Definition mehrere Datensätze verarbeitet werden können, kann auch 
eine Liste von Parametern angegeben werden.

```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task transferSomeData(type: Db2Db) {
    sourceDb = [db_uri, db_user, db_pass]
    targetDb = ['jdbc:sqlite:gretldemo.sqlite',null,null]
    sqlParameters = [[dataset:'Olten'],[dataset:'Grenchen']]
    transferSets = [
        new TransferSet('some.sql', 'albums_dest', true)
    ];
}
```


Parameter | Beschreibung
----------|-------------------
sourceDb | Datenbank aus der gelesen werden soll
targetDb | Datenbank in die geschrieben werden soll
transferSets  | Eine Liste von ``TransferSet``s.
sqlParameters | Eine Map mit Paaren von Parameter-Name und Parameter-Wert (``Map<String,String>``). Oder eine Liste mit Paaren von Parameter-Name und Parameter-Wert (``List<Map<String,String>>``).
batchSize | Anzahl der Records, die pro Batch in die Ziel-Datenbank geschrieben werden (Standard: 5000). Für sehr grosse Tabellen muss ein kleinerer Wert gewählt werden.
fetchSize | Anzahl der Records, die auf einmal vom Datenbank-Cursor von der Quell-Datenbank zurückgeliefert werden (Standard: 5000). Für sehr grosse Tabellen muss ein kleinerer Wert gewählt werden.

Eine ``TransferSet`` ist 

- eine SQL-Datei (mit SQL-Anweisungen zum Lesen der Daten aus der sourceDb), 
- dem Namen der Ziel-Tabelle in der targetDb, und 
- der Angabe ob in der Ziel-Tabelle vor dem INSERT zuerst alle Records gelöscht werden sollen.
- einem optionalen vierten Parameter, der verwendet werden kann um den zu erzeugenden SQL-Insert-String
  zu beeinflussen, u.a. um einen WKT-Geometrie-String in eine PostGIS-Geometrie umzuwandeln

Beispiel, Umwandlung Rechtswert/Hochwertspalten in eine PostGIS-Geometrie
(siehe auch Gretl-Job [afu_onlinerisk_transfer](https://github.com/sogis/gretljobs/tree/main/afu_onlinerisk_transfer) der eine Punktgeometriespalten aus einer Nicht-Postgis-DB übernimmt):

```
new TransferSet('untersuchungseinheit.sql', 'afu_qrcat_v1.onlinerisk_untersuchungseinheit', true, (String[])["geom:wkt:2056"])
```

"geom" ist der Geometrie-Spalten-Name der verwendet wird.

Dazugehöriger Auszug aus SQL-Datei zur Erzeugung des WKT-Strings mit Hilfe von concatenation:

```
'Point(' || ue.koordinate_x::text || ' ' || ue.koordinate_y::text || ')' AS geom
```

Unterstützte Datenbanken: PostgreSQL, SQLite und Oracle. Der Oracle-JDBC-Treiber muss jedoch selber installiert werden (Ausgenommen vom Docker-Image).

### FtpDelete

Löscht Daten auf einem FTP-Server.

Beispiel, löscht alle Daten in einem Verzeichnis:

```
task ftpdelete(type: FtpDelete) {
    server= "ftp.server.org"
    user= "Hans"
    password= "dummy"
    remoteDir = "\\dm01avso24lv95\\itf"
    remoteFile = fileTree(pathToTempFolder) { include '*.zip' } 
    //remoteFile = "*.zip"
}
```

Um bestimmte Dateien zu löschen:

```
task ftpdownload(type: FtpDownload){
    server= "ftp.server.org"
    user= "Hans"
    password= "dummy"
    remoteDir = "\\dm01avso24lv95\\itf"
    remoteFile = "*.zip"
}
```

Um heruntergeladene Daten zu löschen:

```
task ftpdownload(type: FtpDownload){
    server= "ftp.server.org"
    user= "Hans"
    password= "dummy"
    remoteDir = "\\dm01avso24lv95\\itf"
    remoteFile = fileTree(pathToDownloadFolder) { include '*.zip' } 
}
```


Parameter               | Beschreibung
------------------------|-------------------
server                  | Name des Servers (ohne ftp://) 
user                    | Benutzername auf dem Server
password                | Passwort für den Zugriff auf dem Server
remoteDir               | Verzeichnis auf dem Server
remoteFile              | Dateiname oder Liste der Dateinamen auf dem Server (kann auch ein Muster sein (* oder ?)). Ohne diesen Parameter werden alle Dateien aus dem Remoteverzeichnis gelöscht.
systemType              | UNIX oder WINDOWS. Default ist UNIX.
fileSeparator           | Default ist '/'. (Falls systemType Windows ist, ist der Default '\\'.
passiveMode             | Aktiv oder Passiv Verbindungsmodus. Default ist Passiv (true)
controlKeepAliveTimeout | Timeout bis ein NOOP über den Kontroll-Kanal versendet wird. Default ist 300s (=5 Minuten)

### FtpDownload

Lädt alle Dateien aus dem definierten Verzeichnis des Servers in ein lokales 
Verzeichnis herunter.

Beispiel:

```
task ftpdownload(type: FtpDownload){
    server= "ftp.server.org"
    user= "Hans"
    password= "dummy"
    localDir= "downloads"
    remoteDir= ""
}
```

Um eine bestimmte Datei herunterzuladen:

```
task ftpdownload(type: FtpDownload){
    server="ftp.infogrips.ch"
    user= "Hans"
    password= "dummy"
    systemType="WINDOWS"
    localDir= "downloads"
    remoteDir="\\dm01avso24lv95\\itf"
    remoteFile="240100.zip"
}
```


Parameter               | Beschreibung
------------------------|-------------------
server                  | Name des Servers (ohne ftp://) 
user                    | Benutzername auf dem Server
password                | Passwort für den Zugriff auf dem Server
localDir                | Lokales Verzeichnis indem die Dateien gespeichert werden
remoteDir               | Verzeichnis auf dem Server
remoteFile              | Dateiname oder Liste der Dateinamen auf dem Server (kann auch ein Muster sein (* oder ?)). Ohne diesen Parameter werden alle Dateien aus dem Remoteverzeichnis heruntergeladen.
systemType              | UNIX oder WINDOWS. Default ist UNIX.
fileType                | ASCII oder BINARY. Default ist ASCII.
fileSeparator           | Default ist '/'. (Falls systemType Windows ist, ist der Default '\\'.
passiveMode             | Aktiv oder Passiv Verbindungsmodus. Default ist Passiv (true)
controlKeepAliveTimeout | Timeout bis ein NOOP über den Kontroll-Kanal versendet wird. Default ist 300s (=5 Minuten)

### FtpList

Liefert eine Liste der Dateien aus dem definierten Verzeichnis des Servers.

Beispiel:

```
task ftplist(type: FtpList){
    server= "ftp.server.org"
    user= "Hans"
    password= "dummy"
    remoteDir= ""
    doLast {
        println files
    }
}
```


Parameter               | Beschreibung
------------------------|-------------------
server                  | Name des Servers (ohne ftp://) 
user                    | Benutzername auf dem Server
password                | Passwort für den Zugriff auf dem Server
remoteDir               | Verzeichnis auf dem Server
files                   | Liste der Dateinamen auf dem Server
systemType              | UNIX oder WINDOWS. Default ist UNIX.
fileSeparator           | Default ist '/'. (Falls systemType Windows ist, ist der Default '\\'.
passiveMode             | Aktiv oder Passiv Verbindungsmodus. Default ist Passiv (true)
controlKeepAliveTimeout | Timeout bis ein NOOP über den Kontroll-Kanal versendet wird. Default ist 300s (=5 Minuten)

### Gpkg2Dxf 

Exportiert alle Tabellen einer GeoPackage-Datei in DXF-Dateien. Als Input wird eine von _ili2gpkg_ erzeugte GeoPackage-Datei benötigt.

Es werden alle INTERLIS-Klassen exportier (`SELECT tablename FROM T_ILI2DB_TABLE_PROP WHERE setting = 'CLASS'`). Der eigentliche SELECT-Befehl ist komplizierter weil für das Layern der einzelnen DXF-Objekte das INTERLIS-Metaattribut `!!@dxflayer="true"` ausgelesen wird. Gibt es kein solches Metaattribut wird alles in den gleichen DXF-Layer (`default`) geschrieben.
 
**Encoding**: Die DXF-Dateien sind `ISO-8859-1` encodiert.

**Achtung**: Task sollte verbessert werden (siehe E-Mail Claude im Rahmen des Publisher-Projektes).

```
task gpkg2dxf(type: Gpkg2Dxf) {
    dataFile = file("data.gpkg")
    outputDir = file("./out/")
}
```

Parameter | Beschreibung
----------|-------------------
dataFile | GeoPackage-Datei, die nach DXF transformiert werden soll.
outputDir | Verzeichnis, in das die DXF-Dateien gespeichert werden.

### GpkgExport

Daten aus einer bestehenden Datenbanktabelle werden in eine GeoPackage-Datei exportiert.

Beispiele:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task gpkgexport(type: GpkgExport){
    database = [db_uri, db_user, db_pass]
    schemaName = "gpkgexport"
    srcTableName = "exportdata"
    dataFile = "data.gpkg"
    dstTableName = "exportdata"
}
```

```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task gpkgexport(type: GpkgExport) {
    database = [db_uri, db_user, db_pass]
    schemaName = "gpkgexport"
    srcTableName = ["exportTable1", "exportTable2"]
    dataFile = "data.gpkg"
    dstTableName = ["exportTable1", "exportTable2"]
}
```

Parameter | Beschreibung
----------|-------------------
database | Datenbank aus der exportiert werden soll
dataFile  | Name der GeoPackage-Datei, die erstellt werden soll
srcTableName | Name der DB-Tabelle(n), die exportiert werden soll(en). String oder List.
schemaName | Name des DB-Schemas, in dem die DB-Tabelle ist.
dstTableName | Name der Tabelle(n) in der GeoPackage-Datei. String oder List.
batchSize | Anzahl der Records, die pro Batch in die Ziel-Datenbank (GeoPackage) geschrieben werden (Standard: 5000). 
fetchSize | Anzahl der Records, die pro Fetch aus der Quell-Datenbank gelesen werden (Standard: 5000). 

### GpkgImport

Daten aus einer GeoPackage-Datei in eine bestehende Datenbanktabelle importieren.

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task gpkgimport(type: GpkgImport){
    database = [db_uri, db_user, db_pass]
    schemaName = "gpkgimport"
    srcTableName = "Point"
    dstTableName = "importdata"
    dataFile = "point.gpkg"
}
```

Parameter | Beschreibung
----------|-------------------
database | Datenbank in die importiert werden soll
dataFile  | Name der GeoPackage-Datei, die gelesen werden soll
srcTableName | Name der GeoPackage-Tabelle, die importiert werden soll
schemaName | Name des DB-Schemas, in dem die DB-Tabelle ist.
dstTableName | Name der DB-Tabelle, in die importiert werden soll 
~~encoding~~ | ~~Zeichencodierung der SHP-Datei, z.B. ``"UTF-8"``. Default: Systemeinstellung~~
batchSize | Anzahl der Records, die pro Batch in die Ziel-Datenbank geschrieben werden (Standard: 5000). 
fetchSize | Anzahl der Records, die pro Fetch aus der Quell-Datenbank gelesen werden (Standard: 5000). 

Die Tabelle kann weitere Spalten enthalten, die in der GeoPackage-Datei nicht vorkommen. Sie müssen
aber NULLable sein, oder einen Default-Wert definiert haben.

Die Gross-/Kleinschreibung der GeoPckage-Spaltennamen wird für die Zuordnung zu den DB-Spalten ignoriert.

### Gpkg2Shp 

Exportiert alle Tabellen einer GeoPackage-Datei in Shapefiles. Als Input wird eine von _ili2gpkg_ erzeugte GeoPackage-Datei benötigt. 

Es werden alle INTERLIS-Klassen exportiert (`SELECT tablename FROM T_ILI2DB_TABLE_PROP WHERE setting = 'CLASS'`). Je nach, bei der Erstellung der GeoPackage-Datei, verwendeten Parametern, muss die Query angepasst werden. Es muss jedoch darauf geachtet werden, dass es nur eine Query gibt (für alle Datensätze). Für den vorgesehenen Anwendungsfall (sehr einfache, flache Modelle) dürfte das kein Problem darstellen.

**Encoding**: Die Shapefiles sind neu `UTF-8` encodiert. Standard ist `ISO-8859-1`, scheint aber v.a. in QGIS nicht standardmässig zu funktionieren (keine Umlaute). 

**Achtung**: Task sollte verbessert werden (siehe E-Mail Claude im Rahmen des Publisher-Projektes).

```
task gpkg2shp(type: Gpkg2Shp) {
    dataFile = file("data.gpkg")
    outputDir = file("./out/")
}
```

Parameter | Beschreibung
----------|-------------------
dataFile | GeoPackage-Datei, die nach Shapefile transformiert werden soll.
outputDir | Verzeichnis, in das die Shapefile gespeichert werden.

### GpkgValidator
Prüft eine GeoPackage-Datei gegenüber einem INTERLIS-Modell. Basiert auf dem [_ilivalidator_](https://github.com/claeis/ilivalidator).

Beispiel:
```
task validate(type: GpkgValidator){
    models = "GpkgModel"
    dataFiles = ["attributes.gpkg"]
    tableName = "Attributes"
}
```

Parameter | Beschreibung
----------|-------------------
dataFiles | Liste der GeoPackage-Dateien, die validiert werden sollen. Eine leere Liste ist kein Fehler.
tableName | Name der Tabelle in den GeoPackage-Dateien.
models | INTERLIS-Modell, gegen das die die Dateien geprüft werden sollen (mehrere Modellnamen durch Semikolon trennen). Default: Der Name der CSV-Datei.
modeldir | Dateipfade, die Modell-Dateien (ili-Dateien) enthalten. Mehrere Pfade können durch Semikolon ‚;‘ getrennt werden. Es sind auch URLs von Modell-Repositories möglich. Default: ``%XTF_DIR;http://models.interlis.ch/``. ``%XTF_DIR`` ist ein Platzhalter für das Verzeichnis mit der SHP-Datei.
configFile | Konfiguriert die Datenprüfung mit Hilfe einer TOML-Datei (um z.B. die Prüfung von einzelnen Constraints auszuschalten). siehe https://github.com/claeis/ilivalidator/blob/master/docs/ilivalidator.rst#konfiguration
forceTypeValidation | Ignoriert die Konfiguration der Typprüfung aus der TOML-Datei, d.h. es kann nur die Multiplizität aufgeweicht werden. Default: false
disableAreaValidation | Schaltet die AREA Topologieprüfung aus. Default: false
multiplicityOff | Schaltet die Prüfung der Multiplizität generell aus. Default: false
allObjectsAccessible | Mit der Option nimmt der Validator an, dass er Zugriff auf alle Objekte hat. D.h. es wird z.B. auch die Multiplizität von Beziehungen auf externe Objekte geprüft. Default: false
logFile | Schreibt die log-Meldungen der Validierung in eine Text-Datei.
xtflogFile | Schreibt die log-Meldungen in eine INTERLIS 2-Datei. Die Datei result.xtf entspricht dem Modell IliVErrors.
pluginFolder | Verzeichnis mit JAR-Dateien, die Zusatzfunktionen enthalten. 
proxy | Proxy Server für den Zugriff auf Modell Repositories
proxyPort | Proxy Port für den Zugriff auf Modell Repositories
failOnError |  Steuert, ob der Task bei einem Validierungsfehler fehlschlägt. Default: true
validationOk | OUTPUT: Ergebnis der Validierung. Nur falls failOnError=false

### Gzip
Gzipped eine einzelne Datei. Es gibt einen eingebauten Tar-Task, der - nomen est omen - aber immer zuerst eine Tar-Datei erstellt. 

Parameter | Beschreibung
----------|-------------------
dataFile | Datei, die gezipped werden soll.
gzipFile | Output-Datei

Beispiel:
```
task compressFile(type: Gzip) {
    dataFile = file("./planregister.xml");
    gzipFile = file("./planregister.xml.gz");
}
```

### Ili2gpkgImport

Importiert Daten aus einer INTERLIS-Transferdatei in eine GeoPackage-Datei.

Die Tabellen werden implizit auch angelegt, falls sie noch nicht vorhanden sind. Falls die Tabellen in der Datenbank 
schon vorhanden sind, können sie zusätzliche Spalten enthalten (z.B. bfsnr, datum etc.), welche beim Import leer bleiben.

Falls beim Import ein Datensatz-Identifikator (dataset) definiert wird, darf dieser Datensatz-Identifikator in der 
Datenbank noch nicht vorhanden sein. 

Um die bestehenden (früher importierten) Daten zu ersetzen, kann der Task Ili2gpkgReplace (**not yet implemented**) verwendet werden.

Die Option `--doSchemaImport` wird automatisch gesetzt.

Beispiel:
```
task importData(type: Ili2gpkgImport) {
    models = "SO_AGI_AV_GB_Administrative_Einteilungen_20180613"
    dataFile = file("data.xtf");
    dbfile = file("data.gpkg")    
}
```

Parameter | Beschreibung
----------|-------------------
dbfile | GeoPackage-Datei in die importiert werden soll
dataFile  | Name der XTF-/ITF-Datei, die gelesen werden soll. Es können auch mehrere Dateien sein.
proxy  | Entspricht der ili2gpkg Option --proxy
proxyPort  | Entspricht der ili2gpkg Option --proxyPort
modeldir  | Entspricht der ili2gpkg Option --modeldir
models  | Entspricht der ili2gpkg Option --models
dataset  | Entspricht der ili2gpkg Option --dataset
baskets  | Entspricht der ili2gpkg Option --baskets
topics  | Entspricht der ili2gpkg Option --topics
preScript  | Entspricht der ili2gpkg Option --preScript
postScript  | Entspricht der ili2gpkg Option --postScript
deleteData  | Entspricht der ili2gpkg Option --deleteData
logFile  | Entspricht der ili2gpkg Option --logFile
validConfigFile  | Entspricht der ili2gpkg Option --validConfigFile
disableValidation  | Entspricht der ili2gpkg Option --disableValidation
disableAreaValidation  | Entspricht der ili2gpkg Option --disableAreaValidation
forceTypeValidation  | Entspricht der ili2gpkg Option --forceTypeValidation
strokeArcs  | Entspricht der ili2gpkg Option --strokeArcs
skipPolygonBuilding  | Entspricht der ili2gpkg Option --skipPolygonBuilding
skipGeometryErrors  | Entspricht der ili2gpkg Option --skipGeometryErrors
iligml20  | Entspricht der ili2gpkg Option --iligml20
coalesceJson  | Entspricht der ili2gpkg Option --coalesceJson
nameByTopic  | Entspricht der ili2gpkg Option --nameByTopic
defaultSrsCode  | Entspricht der ili2gpkg Option --defaultSrsCode
createEnumTabs  | Entspricht der ili2gpkg Option --createEnumTabs
createMetaInfo  | Entspricht der ili2gpkg Option --createMetaInfo

Für die Beschreibung der einzenen ili2gpkg Optionen: https://github.com/claeis/ili2db/blob/master/docs/ili2db.rst#aufruf-syntax

### Ili2pgExport

Exportiert Daten aus der PostgreSQL-Datenbank in eine INTERLIS-Transferdatei.

Mit dem Parameter ``models``, ``topics``, ``baskets`` oder ``dataset`` wird definiert, welche Daten exportiert werden.

Ob die Daten im INTERLIS 1-, INTERLIS 2- oder GML-Format geschrieben werden, ergibt sich aus der Dateinamenserweiterung der Ausgabedatei. Für eine INTERLIS 1-Transferdatei muss die Erweiterung .itf verwendet werden. Für eine GML-Transferdatei muss die Erweiterung .gml verwendet werden.

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task exportData(type: Ili2pgExport){
    database = [db_uri, db_user, db_pass]
    dataFile = "lv03_254900-out.itf"
    dataset = "254900"
    logFile = "ili2pg.log"
}
```

Damit mit einer einzigen Task-Definition mehrere Datensätze verarbeitet werden können, kann auch 
eine Liste von Dateinamen und Datensätzen angegeben werden.

```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task exportData(type: Ili2pgExport){
    database = [db_uri, db_user, db_pass]
    dataFile = ["lv03_254900-out.itf","lv03_255000-out.itf"]
    dataset = ["254900","255000"]
    logFile = "ili2pg.log"
}
```


Parameter | Beschreibung
----------|-------------------
database | Datenbank aus der exportiert werden soll
dataFile  | Name der XTF-/ITF-Datei, die erstellt werden soll
dbschema  | Entspricht der ili2pg Option --dbschema
proxy  | Entspricht der ili2pg Option --proxy
proxyPort  | Entspricht der ili2pg Option --proxyPort
modeldir  | Entspricht der ili2pg Option --modeldir
models  | Entspricht der ili2pg Option --models
dataset  | Entspricht der ili2pg Option --dataset
baskets  | Entspricht der ili2pg Option --baskets
topics  | Entspricht der ili2pg Option --topics
preScript  | Entspricht der ili2pg Option --preScript
postScript  | Entspricht der ili2pg Option --postScript
deleteData  | Entspricht der ili2pg Option --deleteData
logFile  | Entspricht der ili2pg Option --logFile
validConfigFile  | Entspricht der ili2pg Option --validConfigFile
disableValidation  | Entspricht der ili2pg Option --disableValidation
disableAreaValidation  | Entspricht der ili2pg Option --disableAreaValidation
forceTypeValidation  | Entspricht der ili2pg Option --forceTypeValidation
strokeArcs  | Entspricht der ili2pg Option --strokeArcs
skipPolygonBuilding  | Entspricht der ili2pg Option --skipPolygonBuilding
skipGeometryErrors  | Entspricht der ili2pg Option --skipGeometryErrors
export3  | Entspricht der ili2pg Option --export3
iligml20  | Entspricht der ili2pg Option --iligml20
disableRounding  | Entspricht der ili2pg Option --disableRounding
failOnException | Task wirft Exception, falls ili2db-Prozess fehlerhaft ist (= Ili2dbException). Default: true.

Für die Beschreibung der einzenen ili2pg Optionen: https://github.com/claeis/ili2db/blob/master/docs/ili2db.rst#aufruf-syntax

### Ili2pgImport

Importiert Daten aus einer INTERLIS-Transferdatei in die PostgreSQL-Datenbank.

Die Tabellen werden implizit auch angelegt, falls sie noch nicht vorhanden sind. Falls die Tabellen in der Datenbank 
schon vorhanden sind, können sie zusätzliche Spalten enthalten (z.B. bfsnr, datum etc.), welche beim Import leer bleiben.

Falls beim Import ein Datensatz-Identifikator (dataset) definiert wird, darf dieser Datensatz-Identifikator in der 
Datenbank noch nicht vorhanden sein. 

Falls man mehrere Dateien importieren will, diese jedoch erst zur Laufzeit eruiert werden können, muss der Parameter `dataFile` eine Gradle `FileCollection` resp. eine implementierende Klasse (z.B. `FileTree`) sein. Gleiches gilt für den `dataset`-Parameter. Als einzelner Wert für das Dataset wird in diesem Fall der Name der Datei _ohne_ Extension und _ohne_ Pfad verwendet. Leider kann nicht bereits in der Task-Definition aus dem Filetree eine Liste gemacht werden, z.B. `fileTree(pathToUnzipFolder) { include '*.itf' }.files.name`. Diese Liste ist leer.

Um die bestehenden (früher importierten) Daten zu ersetzen, kann der Task Ili2pgReplace verwendet werden.

Beispiel 1:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task importData(type: Ili2pgImport){
    database = [db_uri, db_user, db_pass]
    dataFile = "lv03_254900.itf"
    logFile = "ili2pg.log"
}
```

Beispiel 2:

Import der AV-Daten. In der `t_datasetname`-Spalte soll die BFS-Nummer stehen. Die BFS-Nummer entspricht den ersten vier Zeichen des Filenamens. 

```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task importData(type: Ili2pgImport){
    database = [db_uri, db_user, db_pass]
    dataFile = fileTree(pathToUnzipFolder) { include '*.itf' }
    dataset = dataFile
    datasetSubstring = 0..4
    logFile = "ili2pg.log"
}
```

Parameter | Beschreibung
----------|-------------------
database | Datenbank in die importiert werden soll
dataFile  | Name der XTF-/ITF-Datei, die gelesen werden soll. Es können auch mehrere Dateien sein.
dbschema  | Entspricht der ili2pg Option --dbschema
proxy  | Entspricht der ili2pg Option --proxy
proxyPort  | Entspricht der ili2pg Option --proxyPort
modeldir  | Entspricht der ili2pg Option --modeldir
models  | Entspricht der ili2pg Option --models
dataset  | Entspricht der ili2pg Option --dataset
datasetSubstring | Range für das Extrahieren eines Substrings von Datasets
baskets  | Entspricht der ili2pg Option --baskets
topics  | Entspricht der ili2pg Option --topics
preScript  | Entspricht der ili2pg Option --preScript
postScript  | Entspricht der ili2pg Option --postScript
deleteData  | Entspricht der ili2pg Option --deleteData
logFile  | Entspricht der ili2pg Option --logFile
importTid | Entspricht der ili2pg Option --importTid
importBid | Entspricht der lii2pg Option --importBid
validConfigFile  | Entspricht der ili2pg Option --validConfigFile
disableValidation  | Entspricht der ili2pg Option --disableValidation
disableAreaValidation  | Entspricht der ili2pg Option --disableAreaValidation
forceTypeValidation  | Entspricht der ili2pg Option --forceTypeValidation
strokeArcs  | Entspricht der ili2pg Option --strokeArcs
skipPolygonBuilding  | Entspricht der ili2pg Option --skipPolygonBuilding
skipGeometryErrors  | Entspricht der ili2pg Option --skipGeometryErrors
iligml20  | Entspricht der ili2pg Option --iligml20
disableRounding  | Entspricht der ili2pg Option --disableRounding
failOnException | Task wirft Exception, falls ili2db-Prozess fehlerhaft ist (= Ili2dbException). Default: true.

Für die Beschreibung der einzenen ili2pg Optionen: https://github.com/claeis/ili2db/blob/master/docs/ili2db.rst#aufruf-syntax

### Ili2pgImportSchema

Erstellt die Tabellenstruktur in der PostgreSQL-Datenbank anhand eines INTERLIS-Modells.

Der Parameter ``iliFile`` oder ``models``  muss gesetzt werden.

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task importSchema(type: Ili2pgImportSchema){
    database = [db_uri, db_user, db_pass]
    models = "DM01AVSO24"
    dbschema = "gretldemo"
    logFile = "ili2pg.log"
}
```

Parameter | Beschreibung
----------|-------------------
database | Datenbank in die importiert werden soll
iliFile  | Name der ili-Datei die gelesen werden soll
models  | Name des ili-Modells, das gelesen werden soll
dbschema  | Entspricht der ili2pg Option --dbschema
proxy  | Entspricht der ili2pg Option --proxy
proxyPort  | Entspricht der ili2pg Option --proxyPort
modeldir  | Entspricht der ili2pg Option --modeldir
dataset  | Entspricht der ili2pg Option --dataset
baskets  | Entspricht der ili2pg Option --baskets
topics  | Entspricht der ili2pg Option --topics
preScript  | Entspricht der ili2pg Option --preScript
postScript  | Entspricht der ili2pg Option --postScript
logFile  | Entspricht der ili2pg Option --logFile
strokeArcs  | Entspricht der ili2pg Option --strokeArcs
oneGeomPerTable | Entspricht der ili2pg Option --oneGeomPerTable
setupPgExt | Entspricht der ili2pg Option --setupPgExt
dropscript | Entspricht der ili2pg Option --dropscript
createscript | Entspricht der ili2pg Option --createscript
defaultSrsAuth | Entspricht der ili2pg Option --defaultSrsAuth
defaultSrsCode | Entspricht der ili2pg Option --defaultSrsCode
createSingleEnumTab | Entspricht der ili2pg Option --createSingleEnumTab
createEnumTabs | Entspricht der ili2pg Option --createEnumTabs
createEnumTxtCol | Entspricht der ili2pg Option --createEnumTxtCol
createEnumColAsItfCode | Entspricht der ili2pg Option --createEnumColAsItfCode
beautifyEnumDispName | Entspricht der ili2pg Option --beautifyEnumDispName
noSmartMapping | Entspricht der ili2pg Option --noSmartMapping
smart1Inheritance | Entspricht der ili2pg Option --smart1Inheritance
smart2Inheritance | Entspricht der ili2pg Option --smart2Inheritance
coalesceCatalogueRef | Entspricht der ili2pg Option --coalesceCatalogueRef
coalesceMultiSurface | Entspricht der ili2pg Option --coalesceMultiSurface
coalesceMultiLine | Entspricht der ili2pg Option --coalesceMultiLine
expandMultilingual | Entspricht der ili2pg Option --expandMultilingual
coalesceJson | Entspricht der ili2pg Option --coalesceJson
createFk | Entspricht der ili2pg Option --createFk
createFkIdx | Entspricht der ili2pg Option --createFkIdx
createUnique | Entspricht der ili2pg Option --createUnique
createNumChecks | Entspricht der ili2pg Option --createNumChecks
createStdCols | Entspricht der ili2pg Option --createStdCols
t_id_Name | Entspricht der ili2pg Option --t_id_Name
idSeqMin | Entspricht der ili2pg Option --idSeqMin
idSeqMax | Entspricht der ili2pg Option --idSeqMax
createTypeDiscriminator | Entspricht der ili2pg Option --createTypeDiscriminator
createGeomIdx | Entspricht der ili2pg Option --createGeomIdx
disableNameOptimization | Entspricht der ili2pg Option --disableNameOptimization
nameByTopic | Entspricht der ili2pg Option --nameByTopic
maxNameLength | Entspricht der ili2pg Option --maxNameLength
sqlEnableNull | Entspricht der ili2pg Option --sqlEnableNull
sqlColsAsText | Entspricht der ili2pg Option --sqlColsAsText
sqlExtRefCols | Entspricht der ili2pg Option --sqlExtRefCols
keepAreaRef | Entspricht der ili2pg Option --keepAreaRef
createTidCol | Entspricht der ili2pg Option --createTidCol
createBasketCol | Entspricht der ili2pg Option --createBasketCol
createDatasetCol | Entspricht der ili2pg Option --createDatasetCol
ver4_translation | Entspricht der ili2pg Option --ver4_translation
translation | Entspricht der ili2pg Option --translation
createMetaInfo | Entspricht der ili2pg Option --createMetaInfo
failOnException | Task wirft Exception, falls ili2db-Prozess fehlerhaft ist (= Ili2dbException). Default: true.

Für die Beschreibung der einzenen ili2pg Optionen: https://github.com/claeis/ili2db/blob/master/docs/ili2db.rst#aufruf-syntax

### Ili2pgReplace

Ersetzt die Daten in der PostgreSQL-Datenbank anhand eines Datensatz-Identifikators (dataset) mit den 
Daten aus einer INTERLIS-Transferdatei. Diese Funktion bedingt, dass das Datenbankschema mit der 
Option createBasketCol erstellt wurde (via Task Ili2pgImportSchema).

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task replaceData(type: Ili2pgReplace){
    database = [db_uri, db_user, db_pass]
    dataFile = "lv03_254900.itf"
    dataset = "254900"
    logFile = "ili2pg.log"
}
```
Die Parameter sind analog wie bei Ili2pgImport.

### Ili2pgDelete

Löscht einen Datensatz in der PostgreSQL-Datenbank anhand eines Datensatz-Identifikators. Diese Funktion bedingt, dass das Datenbankschema mit der Option createBasketCol erstellt wurde (via Task Ili2pgImportSchema). Der Parameter `failOnException` muss `false` sein, ansonsten bricht der Job ab.

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task deleteDataset(type: Ili2pgDelete){
    database = [db_uri, db_user, db_pass]
    models = "DM01AVSO24LV95"
    dbschema = "dm01"
    dataset = "kammersrohr"
}
```

Es können auch mehrere Datensätze pro Task gelöscht werden:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task deleteDataset(type: Ili2pgDelete){
    database = [db_uri, db_user, db_pass]
    dbschema = "dm01"
    dataset = ["Olten","Grenchen"]
}
```


### Ili2pgUpdate

Aktualisiert die Daten in der PostgreSQL-Datenbank anhand einer INTERLIS-Transferdatei, d.h. 
neue Objekte werden eingefügt, bestehende Objekte werden aktualisiert und in der Transferdatei nicht mehr 
vorhandene Objekte werden gelöscht. 

Diese Funktion bedingt, dass das Datenbankschema mit der Option `createBasketCol` erstellt wurde (via Task Ili2pgImportSchema), 
und dass die Klassen und Topics eine stabile OID haben.

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task updateData(type: Ili2pgUpdate){
    database = [db_uri, db_user, db_pass]
    dataFile = "lv03_254900.itf"
    dataset = "254900"
    logFile = "ili2pg.log"
}
```

Die Parameter sind analog wie bei Ili2pgImport.

### Ili2pgValidate

Prüft die Daten ohne diese in eine Datei zu exportieren. Der Task ist erfolgreich, wenn keine Fehler gefunden werden und ist nicht erfolgreich, wenn Fehler gefunden werden. Mit der Option `failOnException=false` ist der Task erfolgreich, auch wenn Fehler gefunden werden.

Mit dem Parameter `--models`, `--topics`, `--baskets` oder `--dataset` wird definiert, welche Daten geprüft werden. Parameter `--dataset` akzeptiert auch eine Liste von Datasets.

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
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

### IliValidator

Prüft eine INTERLIS-Datei (.itf oder .xtf) gegenüber einem INTERLIS-Modell (.ili). Basiert auf dem [_ilivalidator_](https://github.com/claeis/ilivalidator).

Beispiel:
```
task validate(type: IliValidator){
    dataFiles = ["Beispiel2a.xtf"]
    logFile = "ilivalidator.log"
}
```

Parameter | Beschreibung
----------|-------------------
dataFiles | Liste der XTF- oder ITF-Dateien, die validiert werden sollen. Eine leere Liste ist kein Fehler.
models | INTERLIS-Modell, gegen das die die Dateien geprüft werden sollen (mehrere Modellnamen durch Semikolon trennen). Default: Wird anhand der dataFiles ermittelt.
modeldir | Dateipfade, die Modell-Dateien (ili-Dateien) enthalten. Mehrere Pfade können durch Semikolon ‚;‘ getrennt werden. Es sind auch URLs von Modell-Repositories möglich. Default: ``%XTF_DIR;http://models.interlis.ch/``. ``%XTF_DIR`` ist ein Platzhalter für das Verzeichnis mit der CSV-Datei.
configFile | Konfiguriert die Datenprüfung mit Hilfe einer TOML-Datei (um z.B. die Prüfung von einzelnen Constraints auszuschalten). siehe https://github.com/claeis/ilivalidator/blob/master/docs/ilivalidator.rst#konfiguration
forceTypeValidation | Ignoriert die Konfiguration der Typprüfung aus der TOML-Datei, d.h. es kann nur die Multiplizität aufgeweicht werden. Default: false
disableAreaValidation | Schaltet die AREA Topologieprüfung aus. Default: false
multiplicityOff | Schaltet die Prüfung der Multiplizität generell aus. Default: false
allObjectsAccessible | Mit der Option nimmt der Validator an, dass er Zugriff auf alle Objekte hat. D.h. es wird z.B. auch die Multiplizität von Beziehungen auf externe Objekte geprüft. Default: false
skipPolygonBuilding | Schaltet die Bildung der Polygone aus (nur ITF). Default: false
logFile | Schreibt die log-Meldungen der Validierung in eine Text-Datei.
xtflogFile | Schreibt die log-Meldungen in eine INTERLIS 2-Datei. Die Datei result.xtf entspricht dem Modell IliVErrors.
~~pluginFolder~~ | ~~Verzeichnis mit JAR-Dateien, die Zusatzfunktionen enthalten.~~ 
proxy | Proxy Server für den Zugriff auf Modell Repositories
proxyPort | Proxy Port für den Zugriff auf Modell Repositories
failOnError |  Steuert, ob der Task bei einem Validierungsfehler fehlschlägt. Default: true
validationOk | OUTPUT: Ergebnis der Validierung. Nur falls failOnError=false

Zusatzfunktionen (Custom Functions): Die `pluginFolder`-Option ist zum jetzigen Zeitpunkt ohne Wirkung. Die Zusatzfunktionen werden als normale Abhängigkeit definiert und in der ilivalidator-Task-Implementierung registriert. Das Laden der Klassen zur Laufzeit in _iox-ili_ hat nicht funktioniert (`NoClassDefFoundError`...). Der Plugin-Mechanismus von _ilivalidator_ wird momentan ohnehin geändert ("Ahead-Of-Time-tauglich" gemacht).

### JsonImport

Daten aus einer Json-Datei in eine Datenbanktabelle importieren. Die gesamte Json-Datei (muss UTF-8 encoded sein) wird als Text in eine Spalte importiert. Ist das Json-Objekt in der Datei ein Top-Level-Array wird für jedes Element des Arrays ein Record in der Datenbanktabelle erzeugt.

Beispiel:
```
task importJson(type: JsonImport){
    database = [db_uri, db_user, db_pass]
    jsonFile = "data.json"
    qualifiedTableName = "jsonimport.jsonarray"
    columnName = "json_text_col"
}
```

Parameter | Beschreibung
----------|-------------------
database | Datenbank in die importiert werden soll
jsonFile | Json-Datei, die importiert werden soll
qualifiedTableName | Qualifizierter Tabellennamen ("schema.tabelle") in die importiert werden soll
columnName | Spaltenname der Tabelle in die importiert werden soll
deleteAllRows | Inhalt der Tabelle vorgängig löschen?

### MetaPublisher (incubating)

Der MetaPublisher dient zum Publizieren von Metadaten/-dateien. Er erstellt eine INTERLIS-Transferdatei pro Themenpublikation für die Datensuche, falls nötig eine GeoJSON-Datei und die Geocat-XML-Datei. Die GeoJSON-Datei ist entweder statisch (für Raster o.ä.) oder dynamisch (z.B. amtliche Vermessung). Bei einer dynamischen GeoJSON-Datei wird das Datum nachgeführt.

Pro Themenpublikation (also pro Publisher-Task) kann resp. muss es auch einen MetaPublisher-Task geben. Informationen zum Thema sind in einer Toml-Datei gespeichert. Diese dient auch zum Überschreiben von Klassen- und Attributbeschreibungen, die aus der Modelldatei gelesen werden. Die GeoJSON-Datei muss im gleichen Verzeichnis wie die Toml-Datei vorliegen.

Momentan werden die erzeugen Meta-Dateien im jeweiligen Ordner der Themenpublikation im _meta_-Verzeichnis gespeicher und zusätzlich (damit es für die Datensuche leichter fällt) in einem übergeordneten _config_-Verzeichnis.

Beispiele:

```
tasks.register('publishMetaFile', MetaPublisher){
    metaConfigFile = file("meta.toml")
    target = ["sftp://foo.bar.ch", "user", "pwd"]      
    geocatTarget = [Paths.get(project.buildDir.getAbsolutePath(), "geocat").toFile()]
}
```

```
tasks.register('publishMetaFiles', MetaPublisher) {
    dependsOn 'publishFiles'
    metaConfigFile = file("meta-dm01_so.toml")
    target = [project.buildDir]  
    regions = publishFiles.publishedRegions
}
```

Im zweiten Beispiel werden die Regionen aus dem vorausgegangenen Publisher-Task als Input verwendet. 

Parameter | Beschreibung
----------|-------------------
metaConfigFile | Toml-Datei mit Metainformationen zur Themenpublikation
target | Zielverzeichnis der Metadateien (sftp oder Filesystem)
regions | Liste (resp. ListProperty) mit den durch den PublisherTask publizierten Regionen.
geocatTarget | Zielverzeichnis für Geocat-Output (sftp oder Filesystem)

### OgdMetaPublisher (incubating)

Publiziert die Metadaten eines OGD-Datensatzes. Im Gegensatz zum "MetaPublisher" ist es weniger als Publisher (mit viel Konvention), sondern eher ein Generator, der aus dem Toml-File und der INTERLIS-Modelldatei die Metainfo-Datei erstellt. Beim Resultat handelt es sich um eine INTERLIS-Transferdatei (mit eigenem OgdMeta-Modell).

Mit dem "MetaPublisher" teilt er sich einiges an Copy/Paste-Code. Man könnte gewissen Aspekte wohl zusammenlegen (z.B. Infos aus INTERLIS-Modell lesen).

Die INTERLIS-Modelldatei muss entweder in einem (bekannten) Repo sein oder im Verzeichnis der Toml-Datei vorliegen.

Beispiel:

```
task publishMeta(type: OgdMetaPublisher) {
    configFile = file("./ch.so.hba.kantonale_gebaeude.toml")
    outputDir = file(".")
}
```

Parameter | Beschreibung
----------|-------------------
configFile | Toml-Datei mit Metainformationen zum Datensatz.
outputDir | Verzeichnis, in das die Metainfo-Datei gespeichert wird.

### PostgisRasterExport

Exportiert eine PostGIS-Raster-Spalte in eine Raster-Datei mittels SQL-Query. Die SQL-Query darf nur einen Record zurückliefern, d.h. es muss unter Umständen `ST_Union()` verwendet werden. Es angenommen, dass die erste _bytea_-Spalte des Resultsets die Rasterdaten enthält. Weitere _bytea_-Spalten werden ignoriert. Das Outputformat und die Formatoptionen müssen in der SQL-Datei (in der Select-Query) angegeben werden, z.B.:

```
SELECT
    1::int AS foo, ST_AsGDALRaster((ST_AsRaster(ST_Buffer(ST_Point(2607880,1228287),10),150, 150)), 'AAIGrid', ARRAY[''], 2056) AS raster
;
```

Beispiel:
```
task exportTiff(type: PostgisRasterExport) {
    database = [db_uri, db_user, db_pass]
    sqlFile = "raster.sql"
    dataFile = "export.tif"
}
```

Parameter | Beschreibung
----------|-------------------
database | Datenbank aus der exportiert werden soll.
sqlFile  | Name der SQL-Datei aus das SQL-Statement gelesen und ausgeführt wird.
sqlParameters | Eine Map mit Paaren von Parameter-Name und Parameter-Wert.
dataFile | Name der Rasterdatei, die erstellt werden soll.

### Publisher

Stellt für Vektordaten die aktuellsten Geodaten-Dateien bereit und pflegt das Archiv der vorherigen Zeitstände.

[Details](Publisher.md)

### S3Download

Lädt eine Datei aus einem S3-Bucket herunter.

```
task downloadFile(type: S3Download) {
    accessKey = abcdefg
    secretKey = hijklmnopqrstuvwxy
    downloadDir = file("./path/to/dir/")
    bucketName = "ch.so.ada.denkmalschutz"
    key = "foo.pdf"
    endPoint = "https://s3.eu-central-1.amazonaws.com" 
    region = "eu-central-1"
}
```

Parameter | Beschreibung
----------|-------------------
accessKey | AccessKey
secretKey | SecretKey
downloadDir  | Verzeichnis in das die Datei heruntergeladen werden soll.
bucketName  | Name des Buckets, in dem die Datei gespeichert ist.
key | Name der Datei
endPoint | S3-Endpunkt (default: `https://s3.eu-central-1.amazonaws.com/`)
region | S3-Region (default: `eu-central-1`). 

### S3Upload

Lädt ein Dokument (`sourceFile`) oder alle Dokumente in einem Verzeichnis (`sourceDir`) in einen S3-Bucket (`bucketName`) hoch. 

Mit dem passenden Content-Typ kann man das Verhalten des Browsers steuern. Default ist 'application/octect-stream', was dazu führt, dass die Datei immer heruntergeladen wird. Soll z.B. ein PDF oder ein Bild im Browser direkt angezeigt werden, muss der korrekte Content-Typ gewählt werden.

```
task uploadDirectory(type: S3Upload) {
    accessKey = abcdefg
    secretKey = hijklmnopqrstuvwxy
    sourceDir = file("./docs")
    bucketName = "ch.so.ada.denkmalschutz"
    endPoint = "https://s3.eu-central-1.amazonaws.com" 
    region = "eu-central-1"
    acl = "public-read"
    contentType = "application/pdf"
}
```

Parameter | Beschreibung
----------|-------------------
accessKey | AccessKey
secretKey | SecretKey
sourceDir  | Verzeichnis mit den Dateien, die hochgeladen werden sollen.
sourceFile  | Datei, die hochgeladen werden soll.
sourceFiles  | FileCollection mit den Dateien, die hochgeladen werden sollen, z.B. `fileTree("/path/to/directoy/") { include "*.itf" }`
bucketName  | Name des Buckets, in dem die Dateien gespeichert werden sollen.
endPoint | S3-Endpunkt (default: `https://s3.eu-central-1.amazonaws.com/`)
region | S3-Region (default: `eu-central-1`). 
acl | Access Control Layer `[private, public-read, public-read-write, authenticated-read, aws-exec-read, bucket-owner-read, bucket-owner-full-control]`
contentType | Content-Type
metaData  | Metadaten des Objektes resp. der Objekte, z.B. `["lastModified":"2020-08-28"]`.

### S3Bucket2Bucket

Kopiert Objekte von einem Bucket in einen anderen. Die Buckets müssen in der gleichen Region sein. Die Permissions werden nicht mitkopiert und müssen explizit gesetzt werden. 

```
task copyFiles(type: S3Bucket2Bucket, dependsOn:'directoryupload') {
    accessKey = s3AccessKey
    secretKey = s3SecretKey
    sourceBucket = s3SourceBucket
    targetBucket = s3TargetBucket
    acl = "public-read"
}
```

Parameter | Beschreibung
----------|-------------------
accessKey | AccessKey
secretKey | SecretKey
sourceBucket  | Bucket aus dem die Objekte kopiert werden.
targetBucket  | Bucket in den die Objekte kopiert werden.
acl | Access Control Layer `[private, public-read, public-read-write, authenticated-read, aws-exec-read, bucket-owner-read, bucket-owner-full-control]`
metaData  | Metadaten des Objektes resp. der Objekte, z.B. `["lastModified":"2020-08-28"]`.

### ShpExport
Daten aus einer bestehenden Datenbanktabelle werden in eine Shp-Datei exportiert.

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task shpexport(type: ShpExport){
    database = [db_uri, db_user, db_pass]
    schemaName = "shpexport"
    tableName = "exportdata"
    dataFile = "data.shp"
}
```

Parameter | Beschreibung
----------|-------------------
database | Datenbank aus der exportiert werden soll
dataFile  | Name der SHP Datei, die erstellt werden soll
tableName | Name der DB-Tabelle, die exportiert werden soll
schemaName | Name des DB-Schemas, in dem die DB-Tabelle ist.
firstLineIsHeader | Definiert, ob eine Headerzeile geschrieben werden soll, oder nicht. Default: true
encoding | Zeichencodierung der SHP-Datei, z.B. ``"UTF-8"``. Default: Systemeinstellung

Die Tabelle darf eine Geometriespalte enthalten.

### ShpImport

Daten aus einer Shp-Datei in eine bestehende Datenbanktabelle importieren.

Beispiel:
```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task shpimport(type: ShpImport){
    database = [db_uri, db_user, db_pass]
    schemaName = "shpimport"
    tableName = "importdata"
    dataFile = "data.shp"
}
```

Parameter | Beschreibung
----------|-------------------
database | Datenbank in die importiert werden soll
dataFile  | Name der SHP-Datei, die gelesen werden soll
tableName | Name der DB-Tabelle, in die importiert werden soll
schemaName | Name des DB-Schemas, in dem die DB-Tabelle ist.
encoding | Zeichencodierung der SHP-Datei, z.B. ``"UTF-8"``. Default: Systemeinstellung
batchSize | Anzahl der Records, die pro Batch in die Ziel-Datenbank geschrieben werden (Standard: 5000). 

Die Tabelle kann weitere Spalten enthalten, die in der Shp-Datei nicht vorkommen. Sie müssen
aber NULLable sein, oder einen Default-Wert definiert haben.

Die Tabelle muss eine Geometriespalte enthalten. Der Name der Geometriespalte kann beliebig gewählt werden.

Die Gross-/Kleinschreibung der Shp-Spaltennamen wird für die Zuordnung zu den DB-Spalten ignoriert.

### ShpValidator

Prüft eine SHP-Datei gegenüber einem INTERLIS-Modell. Basiert auf dem [_ilivalidator_](https://github.com/claeis/ilivalidator).

Beispiel:
```
task validate(type: ShpValidator){
    models = "ShpModel"
    dataFiles = ["data.shp"]
}
```

Parameter | Beschreibung
----------|-------------------
dataFiles | Liste der SHP-Dateien, die validiert werden sollen. Eine leere Liste ist kein Fehler.
models | INTERLIS-Modell, gegen das die Dateien geprüft werden sollen (mehrere Modellnamen durch Semikolon trennen). Default: Der Name der SHP-Datei.
modeldir | Dateipfade, die Modell-Dateien (ili-Dateien) enthalten. Mehrere Pfade können durch Semikolon ‚;‘ getrennt werden. Es sind auch URLs von Modell-Repositories möglich. Default: ``%XTF_DIR;http://models.interlis.ch/``. ``%XTF_DIR`` ist ein Platzhalter für das Verzeichnis mit der SHP-Datei.
configFile | Konfiguriert die Datenprüfung mit Hilfe einer TOML-Datei (um z.B. die Prüfung von einzelnen Constraints auszuschalten). siehe https://github.com/claeis/ilivalidator/blob/master/docs/ilivalidator.rst#konfiguration
forceTypeValidation | Ignoriert die Konfiguration der Typprüfung aus der TOML-Datei, d.h. es kann nur die Multiplizität aufgeweicht werden. Default: false
disableAreaValidation | Schaltet die AREA Topologieprüfung aus. Default: false
multiplicityOff | Schaltet die Prüfung der Multiplizität generell aus. Default: false
allObjectsAccessible | Mit der Option nimmt der Validator an, dass er Zugriff auf alle Objekte hat. D.h. es wird z.B. auch die Multiplizität von Beziehungen auf externe Objekte geprüft. Default: false
logFile | Schreibt die log-Meldungen der Validierung in eine Text-Datei.
xtflogFile | Schreibt die log-Meldungen in eine INTERLIS 2-Datei. Die Datei result.xtf entspricht dem Modell IliVErrors.
pluginFolder | Verzeichnis mit JAR-Dateien, die Zusatzfunktionen enthalten. 
proxy | Proxy Server für den Zugriff auf Modell Repositories
proxyPort | Proxy Port für den Zugriff auf Modell Repositories
failOnError |  Steuert, ob der Task bei einem Validierungsfehler fehlschlägt. Default: true
validationOk | OUTPUT: Ergebnis der Validierung. Nur falls failOnError=false
encoding | Zeichencodierung der SHP-Datei, z.B. ``"UTF-8"``. Default: Systemeinstellung

Im gegebenen Modell wird eine Klasse gesucht, 
die genau die Attributenamen wie in der Shp-Datei enthält (wobei die Gross-/Kleinschreibung ignoriert wird); 
die Attributtypen werden ignoriert. Wird keine solche Klasse gefunden, gilt das als Validierungsfehler.

Die Prüfung von gleichzeitig mehreren Shapefiles führt zu Fehlermeldungen wie `OID o3158 of object <Modelname>.<Topicname>.<Klassenname> already exists in ...`. Beim Öffnen und Lesen eines Shapefiles wird immer der Zähler, der die interne (im Shapefile nicht vorhandene) `OID` generiert, zurückgesetzt. Somit kann immer nur ein Shapefile pro Task geprüft werden.

### SqlExecutor

Der SqlExecutor-Task dient dazu, Datenumbauten auszuführen. 

Er wird im Allgemeinen dann benutzt, wenn

1. der Datenumbau komplex ist und deshalb nicht im Db2Db-Task erledigt werden kann
2. oder wenn die Quell-DB keine PostgreSQL-DB ist (weil bei komplexen Queries für den
Datenumbau möglicherweise fremdsystemspezifische SQL-Syntax verwendet werden müsste)
3. oder wenn Quell- und Zielschema in derselben Datenbank liegen

In den Fällen 1 und 2 werden Stagingtabellen bzw. ein Stagingschema benötigt, in welche der
Db2Db-Task die Daten zuerst 1:1 hineinschreibt. Der SqlExecutor-Task liest danach die Daten von
dort, baut sie um und schreibt sie dann ins Zielschema. Die Queries für den SqlExecutor-Task können alle in einem einzelnen .sql-File sein oder (z.B. aus Gründen der Strukturierung oder Organisation) auf mehrere .sql-Dateien verteilt sein.
Die Reihenfolge der .sql-Dateien ist relevant. Dies bedeutet, dass die SQL-Befehle des zuerst
angegebenen .sql-Datei zuerst ausgeführt werden müssen, danach dies SQL-Befehle des an
zweiter Stelle angegebenen .sql-Datei, usw.

Der SqlExecutor-Task muss neben Updates ganzer Tabellen (d.h. Löschen des gesamten Inhalts
einer Tabelle und gesamter neuer Stand in die Tabelle schreiben) auch Updates von Teilen von
Tabellen zulassen. D.h. es muss z.B. möglich sein, innerhalb einer Tabelle nur die Objekte einer
bestimmten Gemeinde zu aktualisieren. Darum ist es möglich 
innerhalb der .sql-Datei Paramater zu verwenden und diesen Parametern beim Task einen
konkreten Wert zuzuweisen. Innerhalb der .sql-Datei werden Paramter mit folgender Syntax
verwendet: ``${paramName}``.

```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task executeSomeSql(type: SqlExecutor){
    database = [db_uri, db_user, db_pass]
    sqlParameters = [dataset:'Olten']
    sqlFiles = ['demo.sql']
}
```

Damit mit einer einzigen Task-Definition mehrere Datensätze verarbeitet werden können, kann auch 
eine Liste von Parametern angegeben werden.

```
def db_uri = 'jdbc:postgresql://localhost/gretldemo'
def db_user = "dmluser"
def db_pass = "dmluser"

task executeSomeSql(type: SqlExecutor){
    database = [db_uri, db_user, db_pass]
    sqlParameters = [[dataset:'Olten'],[dataset:'Grenchen']]
    sqlFiles = ['demo.sql']
}
```


Parameter | Beschreibung
----------|-------------------
database | Datenbank in die importiert werden soll
sqlFiles  | Name der SQL-Datei aus der SQL-Statements gelesen und ausgeführt werden
sqlParameters | Eine Map mit Paaren von Parameter-Name und Parameter-Wert (``Map<String,String>``). Oder eine Liste mit Paaren von Parameter-Name und Parameter-Wert (``List<Map<String,String>>``).

Unterstützte Datenbanken: PostgreSQL, SQLite und Oracle. Der Oracle-JDBC-Treiber muss jedoch selber installiert werden (Ausgenommen vom Docker-Image).

### XslTransformer (incubating)

**TODO:** Wie sieht es mit der Endung aus? Immer XTF (wohl auch nicht)

Transformiert eine Datei mittels einer XSL-Transformation ein eine andere Datei. Ist der `xslFile`-Parameter ein String, wird erwartet, dass die Datei im GRETL-Verzeichnis im Ressourcenordner `src/main/resources/xslt`-Verzeichnis gespeichert ist. Falls der `xslFile`-Parameter ein File-Objekt ist, können lokale Dateien verwendet werden.

```
task transform(type: XslTransformer) {
    xslFile = "eCH0132_to_SO_AGI_SGV_Meldungen_20221109.xsl"
    xmlFile = file("MeldungAnGeometer_G-0111102_20221103_145001.xml")
    outDirectory = file(".")
}
```

```
task transform(type: XslTransformer) {
    xslFile = file("path/to/eCH0132_to_SO_AGI_SGV_Meldungen_20221109.xsl")
    xmlFile = file("MeldungAnGeometer_G-0111102_20221103_145001.xml")
    outDirectory = file(".")
}
```

```
task transform(type: XslTransformer) {
    xslFile = "eCH0132_to_SO_AGI_SGV_Meldungen_20221109.xsl"
    xmlFile = fileTree(".").matching {
        include "*.xml"
    }
    outDirectory = file(".")
}
```

Parameter | Beschreibung
----------|-------------------
xslFile | Name der XSLT-Datei, die im `src/main/resources/xslt`-Verzeichnis liegen muss oder File-Objekt (beliebier Pfad).
xmlFile | Datei oder FileTree, die/der transformiert werden soll.
outDirectory | Verzeichnis, in das die transformierte Datei gespeichert wird. Der Name der transformierten Datei entspricht dem Namen der Inpuzt-Datei mit Endung `.xtf`.
