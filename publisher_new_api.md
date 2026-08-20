# Neue API des Publishers (Nach Einführung Themenbezug)

## Motivation

Der Publisher ist sehr mächtig, was ihn in der Verwendung nicht gerade einfach macht. Durch eine Kombination von Namenskonventionen, klaren Fehlermeldlungen und verbesserter Dokumentation soll die Verwendung einfacher gemacht werden.

## Hinweise

* Wo in der Referenzdokumentation die alte Task-Konfigurationsweise verwendet wird, wurde dies übernommen. Meist verwendet die Referenzdokumentation die neue Task-Konfigurationsweise (tasks.register(...))
* Zu Nachführung der Pendenzenliste sind in dieser Doku mit $jek beginnende breadcrumbs enthalten. Diese bitte überlesen.
* Bitte die Besprechungsresultate gleich "inline" ins markdown einpflegen und commiten. Beispielsweise mittels Prefix "$rev" für Review.
* Der Publisher legt im Ordner `meta/` die für die Transferdateien aufgelösten, nicht vordefinierten `.ili`-Modelle sowie bei `outWriteMetadata = true` auch `metainfo.json` ab. Bei `outWriteMetadata = false` werden nur die `.ili`-Modelle geschrieben und keine Publikationsmetadaten in die Datenbank geschrieben.

## Übersicht der Konfiguration der neuen API

### Umgebungsvariablen und Task-Eigenschaften

`PBL_…`-Umgebungsvariablen liefern globale Standardwerte. Eine Task-Eigenschaft überschreibt den
entsprechenden globalen Wert.

|Bereich|Beschreibung|Task-Eigenschaft|Umgebungsvariable|
|---|---|---|---|
|Metadaten|URL der Publikationsmetadaten-Datenbank||`PBL_META_DB_URL`|
|Metadaten|Schema der Publikationsmetadaten-Datenbank||`PBL_META_DB_SCHEMA`|
|Metadaten|Benutzer der Publikationsmetadaten-Datenbank||`PBL_META_DB_USER`|
|Metadaten|Passwort der Publikationsmetadaten-Datenbank||`PBL_META_DB_PASSWORD`|
|Metadaten|Adresse des JSON-Metadaten-Service||`PBL_META_JSON_ADDRESS`|
|Metadaten|Bucket der JSON-Metadaten||`PBL_META_JSON_BUCKET`|
|Metadaten|Dateiname der JSON-Metadaten||`PBL_META_JSON_FILE_NAME`|
|Ausgabe|Standardpfad des Ausgabeordners|`outFolderPath`|`PBL_OUT_FOLDER_PATH`|
|Ausgabe|Benutzer des Standard-Ausgabeordners|`outFolderPath`|`PBL_OUT_FOLDER_USER`|
|Ausgabe|Passwort des Standard-Ausgabeordners|`outFolderPath`|`PBL_OUT_FOLDER_PASSWORD`|
|Ausgabe|Kennung der Publikation|`outDataIdent`||
|Ausgabe|Zu publizierende Formate|`outFormats`||
|Ausgabe|Publikationsmetadaten persistieren (Default: `true`)|`outWriteMetadata`||
|Ausgabe|Grooming-Konfigurationsdatei|`outGroomingConfigFilePath`|`PBL_OUT_GROOMING_CONFIG_FILE_PATH`|
|Ausgabe|Validierungs-Konfigurationsdatei|`outValidationConfigFilePath`||
|DB-Eingabe|Quell-Datenbank-Endpunkt|`dbDatabase`||
|DB-Eingabe|Quell-Datenbank-Schema|`dbSchema`||
|DB-Eingabe|Typ der INTERLIS-Kennung|`dbIliIdent_Type`||
|DB-Eingabe|Werte der INTERLIS-Kennung|`dbIliIdent_Values`||
|DB-Eingabe|Regulärer Ausdruck für die INTERLIS-Kennung|`dbIliIdent_RegEx`||
|DB-Eingabe|Ausgewählte Daten in eine Transferdatei zusammenführen|`dbMergeToSingleXtf`||
|XTF-Eingabe|Quellordner|`xtfFile_FolderPath`||
|XTF-Eingabe|Liste der Quell-Dateinamen|`xtfFilename_List`||
|XTF-Eingabe|Regulärer Ausdruck für Quell-Dateinamen|`xtfFilename_Regex`||
|Eingabe/Ausgabe|Model-Verzeichnis|`ioModelDir`|`PBL_IO_MODEL_DIR`|
|Eingabe/Ausgabe|Publikationsdatum (Default: Zeitpunkt der Ausführung)|`ioPublicationDate`||

`outFolderPath` ist eine Endpunkt-Eigenschaft (`[Pfad, Benutzer, Passwort]`). Die drei
`PBL_OUT_FOLDER_*`-Variablen liefern die Komponenten des globalen Standard-Endpunkts.

### Vom Themenintegrator im build.gradle zu konfigurieren:

Gegliedert in die beiden Hauptmodi "Quelle DB" und "Quelle XTF" zwecks besserem Verständnis, welche Parameter im entsprechenden Modus zwingend sind. Der "Arbeitsmodus" des Publishers wird aus den Parametern abgeleitet.

#### Modus "Quelle DB"

|Name|Zwingend?|Beschreibung|Bemerkungen|
|---|---|---|---|
|dbConnection|ja|DB-URI/Benutzername/Passwort der Datenbank, aus welcher ausgelesen wird|Ehemals "database".|
|dbSchema|ja|Schema, aus welchem ausgelesen wird||
|dbIliIdent_Type|ja|Typ der ILI-Kennung (model, topic, basket, dataset), welcher für die Unterauswahl der Daten aus dem Schema verwendet wird||
|dbIliIdent_Values|nein|Liste der zu exportierenden Kennungen (Modelle, datasets, ...) gemäss dbIliIdent_Type|Ersetzt modelsToPublish, dataset, regions.|
|dbIliIdent_RegEx|nein|Regulärer Ausdruck, mit welchem im Quellschema die zu exportierenden Kennungen selektiert werden|Ersetzt region|
|dbMergeToSingleXtf|nein|Boolean welches bestimmt, ob alle Objekte in ein einziges XTF exportiert werden sollen. Default false.|Neu - gab es bislang nicht|

Zusätzliche Regeln:
* Entweder dbIliIdent_Values oder dbIliIdent_RegEx muss gesetzt sein.

#### Modus "Quelle XTF"

|Name|Zwingend?|Beschreibung|Bemerkungen|
|---|---|---|---|
|xtfFile_FolderPath|ja|Pfad zum Quellordner, in welchem das oder die zu publizierenden XTF-Datei(en) enthalten sind|Ersetzt sourcePath|
|xtfFilename_Values|nein|Liste der Dateinamen (ohne Endung), welche aus dem Quellorder zur Publikation selektiert werden sollen.|Ersetzt regions|
|xtfFilename_Regex|nein|Regulärer Ausdruck, über welchen die zu exportierenden Transferdateien des Quellordners selektiert werden|Ersetzt region|

Zusätzliche Regeln:
* Entweder xtfFilename_List oder xtfFilename_Regex muss gesetzt sein.

#### Quellenunabhängige Parameter

|Name|Zwingend?|Beschreibung|Bemerkungen|
|---|---|---|-|
|outDataIdent|ja|Identifier der Themenbereitstellung, für welche Daten publiziert werden.||
|outFolderPath|nein|Optionaler Zielordner bzw. Endpoint; überschreibt `pupFolderPath`.|Bei `outWriteMetadata = false` zwingend.|
|outWriteMetadata|nein|Schreibt Metadaten in die Publikationsdatenbank und `meta/metainfo.json`.|Default: `true`. Bei `false` werden keine globalen Publisher-Einstellungen gelesen.|
|outGroomingConfigFilePath|nein|Voll qualifizierter Pfad zur Grooming-Konfiguration.||
|outValidationConfigFilePath|nein|Voll qualifizierter Pfad zur Validierungs-Konfiguration.||
|customModelDir|nein|Von den globalen Einstellungen abweichendes Modeldir.||
|outFormats|ja|Liste der zu exportierenden Dateiformate: `xtf`, `itf`, `gpkg`, `shp`, `dxf`, `dxf_geobau`.||

### Env-Variablen (Muss einmalig für die lokale Umgebung in gretljobs.properties konfiguriert werden)

|Name|Zwingend?|Beschreibung|Bemerkungen|
|---|---|---|---|
|PUPDATE_DB_URL|ja|Connection-URL der Datenbank, in welche die Publikationsdatums-Metainformationen geschrieben werden.||
|PUPDATE_DB_SCHEMA|ja|Schema der Publikations-Metainformationen.||
|PUPDATE_DB_USER|ja|Publikationsdatums-Metainformationen: Benutzername.||
|PUPDATE_DB_PASS|ja|Publikationsdatums-Metainformationen: Passwort.||
|PUPFOLDER_PATH|ja|Basispfad des (sftp) Publikationsordners, in den exportiert wird.||
|PUPFOLDER_USER|ja|Benutzername, mit welchem in den Publikationsordner geschrieben wird.||
|PUPFOLDER_PASS|ja|Passwort, mit welchem in den Publikationsordner geschrieben wird.||
JSONMETA_ADDRESS|jsonmetaAddress|ja|Basis-URL der JSON-Metadatenquelle.||
JSONMETA_BUCKET|jsonmetaBucket|ja|Bucket der JSON-Metadatenquelle.||
JSONMETA_FILENAME|jsonmetaFileName|ja|Dateiname der JSON-Metadatenquelle.||
|MODELDIR|nein|Von Publisher zu verwendendes modeldir. Default: Ili2pg Default||
|GROOMING_CONFIG_FILE_PATH|ja|Pfad zur Default Grooming Konfig.||

## Beispiele

Folgend Kopien der Beispiele der [Referenzdoku](https://gretl.app/publisher.html), in bestehender und neuer Konfiguration zum Vergleich.

### XTF -> XTF

#### Schreiben auf Test- oder Prod SFTP

bisher:

    task publishFile(type: Publisher){
        dataIdent = "ch.so.agi.vermessung.edit"
        target = [ "sftp://ftp.server.ch/data", "user", "password" ]
        sourcePath = file("/path/file.xtf")
    }

neu:

    task publishFile(type: Publisher){
        dataIdent = "ch.so.agi.vermessung.edit"
        xtfFile_FolderPath = file("/path")
        xtfFilename_List = ["file"]
    }

Bemerkung: sftp-Konfig ist neu als env definiert und fällt darum im Task weg

#### Schreiben in lokales Verzeichnis

bisher:

    task publishFile(type: Publisher){
        dataIdent = "ch.so.agi.vermessung.edit"
        target = [file("/out")]  
        sourcePath = file("/path/file.xtf")
    }

neu:

    task publishFile(type: Publisher){
        dataIdent = "ch.so.agi.vermessung.edit"
        xtfFile_FolderPath = file("/path")
        xtfFilename_List = ["file"]
        outFolderPath = file("$buildDir/publisher_local")
        outWriteMetadata = false
    }

### DB -> XTF

#### Dataset(s)

bisher:

    tasks.register('publishFromDb', Publisher) {
        dataIdent = "ch.so.agi.vermessung"
        target = [ "sftp://ftp.server.ch/data", "user", "password" ]
        database = ["uri","user","password"]
        dbSchema "av"
        dataset = "myDataset"
    }

neu:

    tasks.register('publishFromDb', Publisher) {
        dataIdent = "ch.so.agi.vermessung"
        dbDatabase = ["uri","user","password"]
        dbSchema "av"
        dbIliIdent_Type = "dataset"
        dbIliIdent_Values = ["myDataset"]
    }

#### Model(s)

bisher:

    tasks.register('publishFromDb', Publisher) {
        dataIdent = "ch.so.agi.vermessung"
        target = [ "sftp://ftp.server.ch/data", "user", "password" ]
        database = ["uri","user","password"]
        dbSchema "av"
        modelsToPublish = "DM01AVCH24LV95D"
    }

neu:

    tasks.register('publishFromDb', Publisher) {
        dataIdent = "ch.so.agi.vermessung"
        dbDatabase = ["uri","user","password"]
        dbSchema "av"
        dbIliIdent_Type = "model"
        dbIliIdent_Values = ["DM01AVCH24LV95D"]
    }

### Regionen (Sprich: Regular expressions)

#### XTF

bisher:

    tasks.register('publishFile', Publisher) {
        dataIdent = "ch.so.agi.vermessung.edit"
        target = [ "sftp://ftp.server.ch/data", "user", "password" ]
        sourcePath = file("/transferfiles/file.xtf")
        region = "[0-9][0-9][0-9][0-9]"  // regex; ersetzt den filename im sourcePath
    }

neu:

    tasks.register('publishFile', Publisher) {
        dataIdent = "ch.so.agi.vermessung.edit"
        xtfFile_FolderPath = file("/path")
        xtfFilename_Regex = "[0-9][0-9][0-9][0-9]"
    }

#### DB

bisher:

    tasks.register('publishFromDb', Publisher) {
        dataIdent = "ch.so.agi.vermessung.edit"
        target = [ "sftp://ftp.server.ch/data", "user", "password" ]
        database = ["uri","user","password"]
        dbSchema "av"
        region = "[0-9][0-9][0-9][0-9]"
    }

neu:

    tasks.register('publishFromDb', Publisher) {
        dataIdent = "ch.so.agi.vermessung.edit"
        dbDatabase = ["uri","user","password"]
        dbSchema "av"
        dbIliIdent_Type = "dataset"
        dbIliIdent_RegEx = "[0-9][0-9][0-9][0-9]"
    }

### Verkettung von Publishern

bisher:

    tasks.register('publishFile0' Publisher) {
        dataIdent = "ch.so.agi.vermessung.edit"
        target = [project.buildDir]
        sourcePath = file("../../../../src/test/resources/data/publisher/files/av_test.itf")
        modeldir= file("../../../../src/test/resources/data/publisher/ili")
        region="[0-9][0-9][0-9][0-9]"
    }

    tasks.register('publishFile1', Publisher) {
        dataIdent = "ch.so.agi.vermessung.pub"
        target = [project.buildDir]
        sourcePath = file("../../../../src/test/resources/data/publisher/files/av_test.itf")
        modeldir= file("../../../../src/test/resources/data/publisher/ili")
        regions=publishFile0.publishedRegions
    }

neu:

    tasks.register('publishFile0' Publisher) {
        dataIdent = "ch.so.agi.vermessung.edit"
        xtfFile_FolderPath = file("../../../../src/test/resources/data/publisher/files/")
        xtfFilename_RegEx = "[0-9][0-9][0-9][0-9]"
        outFolderPath = file("$buildDir")
        outWriteMetadata = false
    }

    tasks.register('publishFile1', Publisher) {
        dataIdent = "ch.so.agi.vermessung.pub"
        xtfFile_FolderPath = file("../../../../src/test/resources/data/publisher/files/")
        xtfFilename_List = publishFile0.publishedRegions
        outFolderPath = file("$buildDir")
        outWriteMetadata = false
    }

### Validierung

bisher:

    tasks.register('publishFile', Publisher) {
        ...
        validationConfig =  "validationConfig.ini"
    }

neu:

    tasks.register('publishFile', Publisher) {
        ...
        validationConfigFilePath =  file("$projectDir/validationConfig.ini")
    }

### Benutzer-Formate

bisher:

    tasks.register('publishUserFormats', Publisher) {
        ...
        userFormats = true
    }

neu:

    tasks.register('publishUserFormats', Publisher) {
        ...
        outFormats = ["xtf","gpkg","shp","dxf"]
    }

### KGDI-Service

Fällt weg.

### Archiv aufräumen

bisher:

    tasks.register('publishFile', Publisher) {
        ...
        grooming = "grooming.json"
    }

neu:

    tasks.register('publishFile', Publisher) {
        ...
    }

oder, falls der Default verändert werden muss:

    tasks.register('publishFile', Publisher) {
        ...
        groomingConfigFilePath = file("$projectDir/grooming.json")
    }

## Input-Validierung

Um trotz der langen Parameterliste der Publishers ein vom Themenintegrator nicht beabsichtigtes Verhalten zu vermeiden, werden die korrekten Parameter-Konfigurationen strikte validiert. Bei nicht eindeutigen Task-Konfigurationen bricht der Publisher mit entsprechender Fehlermeldung ab.

### Beispiele von nicht eindeutigen Konfigurationen:

Sowohl Angaben zu Db- und Dateiquelle gemacht:

    tasks.register('publish', Publisher) {
        ...
        dbConnection = ...
        xtfFile_FolderPath = ... 
        ...
    }

Sowohl Liste als auch Regex konfiguriert:

    tasks.register('publish', Publisher) {
        ...
        xtfFilename_List = ...
        xtfFilename_Regex = ... 
        ...
    }

## Log-Output des neuen Publishers

Geplant:
* Bei "normalem" Log-Level: Eine Logzeile, in welcher die Teilschritte aufgelistet werden, welche der Publisher aufgrund der Task-Konfiguration abarbeitet. 
  * Beispiel: `Publishing ch.so.agi.vermessung.pub through steps: ReadFromFile, Validate, DeriveUserFormats, Pack, UpdateRemote, WritePubDate`
* Bei "info" Log-Level: Eine weitere Logzeile pro Teilschritt, mit Detailinformationen zum Teilschritt. 
  * Beispiel: `Reading files fuu.xtf, bar.xtf from myFolder`
