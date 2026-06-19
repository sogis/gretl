# Anpassungen des Publishers im Rahmen von Next (Themenbezug)

[Konfigurationsanleitung zum Publisher](https://gretl.app/publisher.html)

Entscheide und Fragen:
* Der Publisher ist sehr mächtig, was ihn in der Verwendung nicht einfach macht. Wie kann/soll die Verwendung vereinfacht werden?
* Bezüglich der "Konfigurationskonsistenz" zwischen files.geo.so.ch und data.geo.so.ch ist der Themenintegrator heute komplett auf sich alleine gestellt. Anzustreben sind bessere Validierungsmöglichkeiten der Konfiguration unter Wahrung der heutigen Flexibilität des Publishers.
    * Output Umgebungsabhängig (lokal, test, review, prod) in den unterschiedlichen Ziel-Speicherorten ablegen.
    * Fragment der Datenbezugs-Konfiguration

## Validierung der Redundanzen bezüglich der Publisher-Konfiguration

In der folgenden Tabelle ist abgebildet, welche Informationen sowohl in der Publisher-Konfiguration (Im build.gradle) wie auch in der SIMI-Konfiguration vorkommen:

|Information|Quelle SIMI|Quelle Publisher|Bemerkungen|
|---|---|---|---|
|Identifier der Bereitstellung|Theme.identifier mit ThemePublication.classSuffixOverride|dataIdent|Validierung nicht möglich, da auch "Standalone" Publisher möglich ist|
|Modellname|ThemePublication.publicModelName|Meist aus Datenquelle hergeleitet|Publisher bricht mit Fehlermeldung ab, falls die Modelle nicht übereinstimmen|
|Teilgebiete|SubArea.identifier|Fallabhängig region, regions, dataset|Es gibt aktuell keine Validierung, dass die diebezügliche Konfiguration des Publishers mit der Konfiguration in SIMI übereinstimmt|
|Bereitgestellte Formate|CustomFileType|isUserFormats|Aufgrund des Spezialfalls DXF Geobau in SIMI einzeln abgebildet, im Publisher mit Boolean "isUserFormats".|

## Verbesserungen

### Arbeitsteilung / Verantwortlichkeiten

* Publisher
  * Erstellt oder aktualisiert den Publikationsstand auf S3 zu der entsprechenden Themenbereitstellung, separiert pro Umgebung (Lokal, Test, Review, Prod)
    * Publikationsstand ist eine Liste mit Records. Jeder Record hat die folgenden Eigenschaften:
      * dataset_ident: Kennung der Themenbereitstellung
      * part_ident: Kennung des Teils, welcher publiziert wurde (dataset, ...)
        * Default ist "allparts" für nicht aufgeteilte (kantonsweite) Daten.
      * modelname: Name des Modells, in welchem die bereitgestellten Daten vorliegen
      * published: Zeitstempel der jüngsten Publikation
      * formats: Liste der Formate, welche publiziert wurden
* Netl
  * Joint den Publikationsstand auf die entsprechende Themenintegration
    * Themenintegrationen ohne Publikationsstand werden als "invalid" markiert und in den Datenbezugs-Applikationen nicht angezeigt.
  * Validiert die redundant konfigurierten Informationen und markiert einen Datenbezug als "invalid", falls diese nicht übereinstimmen.

Nachteil: Publikationsdatum wird nur aktualisiert, wenn Netl laufengelassen wird. Dies ist aber vernachlässigbar, da Netl jede Nacht laufen wird.

### Gruppierung und Umbenennung der verwandten Publisher Eigenschaften

#### Quelle Datenbank

Alle mit dem Lesen aus einer Datenbank-Tabelle verbundenen Eigenschaften erhalten den Prefix "db"

|Neu|Alt|Bemerkung|
|---|---|---|
|dbDatabase|database||
|dbSchema|dbSchema||
|dbIliIdent_Type|-|Neu: Typ der ILI-Kennung, welche für den Export verwendet wird (model, topic, basket, dataset)|
|dbIliIdent_Values|-|Neu: Werte der ILI-Kennung (Konvenience-Eigenschaft, damit bei genauen Werten nicht mit RegEx gearbeitet werden muss.)|
|dbIliIdent_RegEx|-|Neu: Reguläre Expression auf die entsprechenden Kennungs-Typen im dbSchema.|
|dbMergeToSingleXtf||Neu: Boolean, ob alle Objekte in ein einziges XTF exportiert werden sollen.|
|-|modelsToPublish|Ersetzt durch dbIliIdent_Type und dbIliIdent_Values|
|-|dataset|Ersetzt durch dbIliIdent_Type und dbIliIdent_Values|
|-|region|Ersetzt durch dbIliIdent_Type und dbIliIdent_RegEx|
|-|regions|Ersetzt durch dbIliIdent_Type und dbIliIdent_Values|


#### Quelle Transferdatei

Alle mit dem Lesen aus einer Transferdatei verbundenen Eigenschaften erhalten den Prefix "xtf"

|Neu|Alt|
|---|---|
|xtfFilePath|sourcePath|
|xtfFilenameRegex|region|
|xtfFilenameList|regions|

#### Ziel Eigenschaften

Alle mit der Ablage im Zielverzeichnis verbundenen Eigenschaften erhalten den Prefix "out".

|Neu|Alt|
|---|---|
|outBasePath|target|
|outDataIdent|dataIdent|
|outWriteUserFormats|isUserFormats|
|outGroomingConf|grooming|
|outValidationConfig|validationConfig| 
|outPublishedRegions|publishedRegions| 

#### Globale Eigenschaften

Alle globalen Eigenschaften erhalten den Präfix "glob" für global.

|Neu|Alt|
|---|---|
|globModeldir|modeldir|
|globProxy|proxy|
|globProxyPort|proxyPort|

#### Deprecated Eigenschaften

Dies werden noch "mitgezogen", gelten aber als deprecated.

|Neu|Alt|
|---|---|
|depExportModels|exportModels|
|depVersion|version|

## Fragen

* Transparenz bei lokaler Entwicklung mittels?
  * Immer zuerst in das build-Verzeichnis schreiben zwecks transparenz
  * Lokaler sft server

### Reduktion der Redundanzen

modelname und formats muss nicht mehr doppelt geführt werden, da dieses vom Publisher in den Publikationsstand geschrieben wird.

Bei nicht vom Publisher publizierten Daten wird manuell der Publikationsstand notiert (wahrscheinlich in eigener Datei). Umfasst dieselben Informationen wie der Publisher-Publikationsstand, aber ohne modelname.

### Vertiefte Validierung

Die vertieften Validierungen sollen dem Themenintegrator in der lokalen Umgebung zur Verfügung stehen (Kurzer Feedbackloop)


