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
    * Publikationsstand ist eine Lite mit Records. Jeder Record hat die folgenden Eigenschaften:
      * part: Kennung des Teils, welcher publiziert wurde (dataset, ...)
      * published: Zeitstempel der jüngsten Publikation
      * modelname: Name des Publikationsmodells
      * formats: Liste der Formate, welche publiziert wurden
* Netl
  * Joint den Publikationsstand auf die entsprechende Themenintegration
    * Themenintegrationen ohne Publikationsstand werden als "invalid" markiert und in den Datenbezugs-Applikationen nicht angezeigt.
  * Validiert die redundant konfigurierten Informationen und markiert einen Datenbezug als "invalid", falls diese nicht übereinstimmen.

Nachteil: Publikationsdatum wird nur aktualisiert, wenn Netl laufengelassen wird. Dies ist aber vernachlässigbar, da Netl jede Nacht laufen wird.

### Gruppierung und Umbenennung der verwandten Publisher Eigenschaften

Todo

### Reduktion der Redundanzen

modelname und formats muss nicht mehr doppelt geführt werden, da dieses vom Publisher in den Publikationsstand geschrieben wird.

Bei nicht vom Publisher publizierten Daten wird manuell der Publikationsstand notiert (wahrscheinlich in eigener Datei). Umfasst dieselben Informationen wie der Publisher-Publikationsstand, aber ohne modelname.

### Vertiefte Validierung

Die vertieften Validierungen sollen dem Themenintegrator in der lokalen Umgebung zur Verfügung stehen (Kurzer Feedbackloop)


