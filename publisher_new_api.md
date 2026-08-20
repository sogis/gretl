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
|Metadaten|Connection-URL der Datenbank, in welche die Publikationsmetadaten geschrieben werden.||`PBL_META_DB_URL`|
|Metadaten|Schema der Publikationsmetadaten-Datenbank.||`PBL_META_DB_SCHEMA`|
|Metadaten|Benutzername für die Publikationsmetadaten-Datenbank.||`PBL_META_DB_USER`|
|Metadaten|Passwort für die Publikationsmetadaten-Datenbank.||`PBL_META_DB_PASSWORD`|
|Metadaten|Basis-URL der JSON-Metadatenquelle.||`PBL_META_JSON_ADDRESS`|
|Metadaten|Bucket der JSON-Metadatenquelle.||`PBL_META_JSON_BUCKET`|
|Metadaten|Dateiname der JSON-Metadatenquelle.||`PBL_META_JSON_FILE_NAME`|
|Ausgabe|Basispfad des Publikationsordners bzw. Ziel-Endpunkt; wird durch `outFolderPath` überschrieben.|`outFolderPath`|`PBL_OUT_FOLDER_PATH`|
|Ausgabe|Benutzername für den Publikationsordner bzw. Ziel-Endpunkt.|`outFolderPath`|`PBL_OUT_FOLDER_USER`|
|Ausgabe|Passwort für den Publikationsordner bzw. Ziel-Endpunkt.|`outFolderPath`|`PBL_OUT_FOLDER_PASSWORD`|
|Ausgabe|Identifier der Themenbereitstellung, für welche Daten publiziert werden.|`outDataIdent`||
|Ausgabe|Liste der zu exportierenden Dateiformate: `xtf`, `itf`, `gpkg`, `shp`, `dxf`, `dxf_geobau`.|`outFormats`||
|Ausgabe|Schreibt Metadaten in die Publikationsdatenbank und nach `meta/metainfo.json`. Default: `true`; bei `false` werden keine globalen Publisher-Einstellungen gelesen.|`outWriteMetadata`||
|Ausgabe|Voll qualifizierter Pfad zur Grooming-Konfiguration.|`outGroomingConfigFilePath`|`PBL_OUT_GROOMING_CONFIG_FILE_PATH`|
|Ausgabe|Voll qualifizierter Pfad zur Validierungs-Konfiguration.|`outValidationConfigFilePath`||
|DB-Eingabe|DB-URI, Benutzername und Passwort der Datenbank, aus welcher ausgelesen wird.|`dbDatabase`||
|DB-Eingabe|Schema, aus welchem ausgelesen wird.|`dbSchema`||
|DB-Eingabe|Typ der ILI-Kennung (`model`, `topic`, `basket`, `dataset`) zur Unterauswahl der Daten aus dem Schema.|`dbIliIdent_Type`||
|DB-Eingabe|Liste der gemäss `dbIliIdent_Type` zu exportierenden Kennungen, z. B. Modelle oder Datasets.|`dbIliIdent_Values`||
|DB-Eingabe|Regulärer Ausdruck zur Auswahl der zu exportierenden Kennungen im Quellschema.|`dbIliIdent_RegEx`||
|DB-Eingabe|Bestimmt, ob alle Objekte in eine einzige XTF-Datei exportiert werden. Default: `false`.|`dbMergeToSingleXtf`||
|XTF-Eingabe|Pfad zum Quellordner mit den zu publizierenden XTF-Dateien.|`xtfFile_FolderPath`||
|XTF-Eingabe|Liste der Dateinamen ohne Endung, die aus dem Quellordner publiziert werden sollen.|`xtfFilename_List`||
|XTF-Eingabe|Regulärer Ausdruck zur Auswahl der zu publizierenden Transferdateien im Quellordner.|`xtfFilename_Regex`||
|Eingabe/Ausgabe|Vom Publisher zu verwendendes Model-Verzeichnis.|`ioModelDir`|`PBL_IO_MODEL_DIR`|
|Eingabe/Ausgabe|Publikationsdatum. Default: Zeitpunkt der Ausführung.|`ioPublicationDate`||

`outFolderPath` ist eine Endpunkt-Eigenschaft (`[Pfad, Benutzer, Passwort]`). Die drei
`PBL_OUT_FOLDER_*`-Variablen liefern die Komponenten des globalen Standard-Endpunkts.

## Beispiele

Die folgenden Beispiele verwenden ausschliesslich die neue API. Alle verwendeten Task-Eigenschaften
sind in [Umgebungsvariablen und Task-Eigenschaften](#umgebungsvariablen-und-task-eigenschaften)
aufgeführt.

### XTF -> XTF

#### Schreiben auf Test- oder Prod-SFTP

    tasks.register('publishFile', Publisher) {
        outDataIdent = "ch.so.agi.vermessung.edit"
        xtfFile_FolderPath = file("/path")
        xtfFilename_List = ["file"]
    }

Der Ziel-Endpunkt wird über die `PBL_OUT_FOLDER_*`-Umgebungsvariablen konfiguriert und muss
deshalb nicht im Task angegeben werden.

#### Schreiben in lokales Verzeichnis

    tasks.register('publishFile', Publisher) {
        outDataIdent = "ch.so.agi.vermessung.edit"
        xtfFile_FolderPath = file("/path")
        xtfFilename_List = ["file"]
        outFolderPath = [file("$buildDir/publisher_local")]
        outWriteMetadata = false
    }

### DB -> XTF

#### Dataset(s)

    tasks.register('publishFromDb', Publisher) {
        outDataIdent = "ch.so.agi.vermessung"
        dbDatabase = ["uri","user","password"]
        dbSchema = "av"
        dbIliIdent_Type = "dataset"
        dbIliIdent_Values = ["myDataset"]
    }

#### Model(s)

    tasks.register('publishFromDb', Publisher) {
        outDataIdent = "ch.so.agi.vermessung"
        dbDatabase = ["uri","user","password"]
        dbSchema = "av"
        dbIliIdent_Type = "model"
        dbIliIdent_Values = ["DM01AVCH24LV95D"]
    }

### Regular expressions

#### XTF

    tasks.register('publishFile', Publisher) {
        outDataIdent = "ch.so.agi.vermessung.edit"
        outFormats = ["xtf","gpkg","shp","dxf"]
        xtfFile_FolderPath = file("/path")
        xtfFilename_Regex = "[0-9][0-9][0-9][0-9]"
    }

Exportiert alle Dateien im Ordner deren Name auf die Expression "[0-9][0-9][0-9][0-9]" matcht.   
Beispiel: 2504.xtf (.xtf wird nicht mit der Expression verglichen).

#### DB

    tasks.register('publishFromDb', Publisher) {
        outDataIdent = "ch.so.agi.vermessung.edit"
        outFormats = ["xtf","gpkg","shp","dxf"]
        dbDatabase = ["uri","user","password"]
        dbSchema = "av"
        dbIliIdent_Type = "dataset"
        dbIliIdent_RegEx = "[0-9][0-9][0-9][0-9]"
    }

Exportiert alle Datasets deren Name auf die Expression "[0-9][0-9][0-9][0-9]" matcht.

### Validierung

    tasks.register('publishFile', Publisher) {
        outDataIdent = "ch.so.agi.vermessung.edit"
        outFormats = ["xtf","gpkg","shp","dxf"]
        xtfFile_FolderPath = file("/path")
        xtfFilename_List = ["file"]
        outValidationConfigFilePath = file("$projectDir/validationConfig.ini")
    }

### Formate

    tasks.register('publishUserFormats', Publisher) {
        outDataIdent = "ch.so.agi.vermessung.edit"
        outFormats = ["shp"]
        xtfFile_FolderPath = file("/path")
        xtfFilename_List = ["file"]
    }

Es werden exakt die konfigurierten Formate exportiert. Im Beispiel also ausschliesslich Shapefile (ohne xtf).

### Archiv aufräumen

Der globale Default wird verwendet, sofern keine Grooming-Konfiguration im Task gesetzt ist.
Für eine abweichende Konfiguration:

    tasks.register('publishFile', Publisher) {
        outDataIdent = "ch.so.agi.vermessung.edit"
        xtfFile_FolderPath = file("/path")
        xtfFilename_List = ["file"]
        outGroomingConfigFilePath = file("$projectDir/grooming.json")
    }

## Input-Validierung

Um trotz der langen Parameterliste der Publishers ein vom Themenintegrator nicht beabsichtigtes Verhalten zu vermeiden, werden die korrekten Parameter-Konfigurationen strikte validiert. Bei nicht eindeutigen Task-Konfigurationen bricht der Publisher mit entsprechender Fehlermeldung ab.

### Beispiele von nicht eindeutigen Konfigurationen:

Sowohl Angaben zu Db- und Dateiquelle gemacht:

    tasks.register('publish', Publisher) {
        ...
        dbDatabase = ...
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
