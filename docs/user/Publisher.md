# Publisher-Task

GRETL-Task, welcher für Vektordaten die aktuellsten Geodaten-Dateien 
bereitstellt und das Archiv der vorherigen Zeitstände pflegt.

## ToDos

- XTF -> local XTF
- XTF -> remote XTF
- DB -> XTF
- Regionen
- Validierung
- Benutzer-Formate (GPKG, DXF, SHP)
- KGDI-Service
- Archiv aufräumen
- effektiv publizierte Regionen als Output

## Einbindung in einen typischen GRETL-Publikationsjob

In den heute vorliegenden Publikationsjobs werden häufig die Daten vom relational aufgebauten Edit-Schema 
mittels SQL-Queries "flachgewalzt" und ins Pub-Schema kopiert. Die Schema-Struktur
wird automatisch mittels ili2pg aus Edit- und Pub-Modell generiert.

Für den Datenbezug wird das build.gradle mit zwei Aufrufen des Publisher-Task ergänzt:

    defaultTasks 'pubProduct'

    task pubEdit(type: Publisher){
      dataIdent = "ch.so.avt.verkehrszaehlstellen.edit"
      ...
    }

    task transferVZS(type: Db2Db, dependsOn: pubEdit){
      ...
    }

    task pubProduct(type: Publisher, dependsOn: transferDenkmal){
      dataIdent = "ch.so.avt.verkehrszaehlstellen"
      ...
    }

Bei Problemen mit der Datenqualität der Originaldaten schlägt der Task "pubEdit" fehl. Der Job bricht mit Fehler ab, bevor die Daten irgendwo landen.

## XTF -> XTF

Falls die Daten bereits als XTF-/ITF-Datei vorliegen, muss die Quelldatei angegeben werden.

    task publishFile(type: Publisher){
      dataIdent = "ch.so.agi.vermessung.edit"
      target = [ "sftp://ftp.server.ch/data", "user", "password" ]
      sourcePath = "/path/file.xtf"
    }

Die Daten können alternativ zu SFTP in ein lokales Verzeichnis publiziert werden:

    task publishFile(type: Publisher){
      dataIdent = "ch.so.agi.vermessung.edit"
      target = "/out"  
      sourcePath = "/path/file.xtf"
    }


## Ablauf

Der Publisher arbeitet die folgenden Hauptschritte ab:

1) Verstecktes Verzeichnis für den Datenstand via FTPS erstellen (.yyyy-MM-dd/). Kein Abbruch, falls das Verzeichnis vorhanden ist.
2) XTFs in Verzeichnis ablegen.
   1) Für Datenthemen mit Quelle=Datenbank: XTF-Transferdateien exportieren.
      1) Mit ili2pg das xtf erzeugen.
      2) Prüfung der xtf gegen das Modell. Abbruch bei fatalen Fehlern.
   2) Für Datenthemen mit Quelle=XTF: XTF in Verzeichnis kopieren.
3) Aus dem Publikations-xtf die Benutzerformate (Geopackage, Shapefile, Dxf) ableiten und ablegen.
4) Metadaten sammeln (aktuell nur Publikationsdatum und ili-Dateien) und im Unterordner meta/ ablegen.
5) Neue Ordnernamen setzen.
   1) aktuell umbenennen auf Ordnername gemäss Datum in publishdate.json.
   2) Verstecktes Verzeichnis umbenennen auf aktuell.
6) Publikationsdatum via bereitgestelltes REST-API in den KGDI-Metadaten nachführen
7) Historische Stände ausdünnen.

## Ordnerstruktur im Ziel-Verzeichnis

### Gängiger Fall: Zwei Modelle, keine Regionen

Publikation in den beiden Datenbereitstelungen ch.so.avt.verkehrszaehlstellen und ch.so.avt.verkehrszaehlstellen.edit

Namenskonvention für die Dateien: \[Datenbereitstellungs-Identifier\].\[Format-Identifier\].zip

> data/
> * ch.so.avt.verkehrszaehlstellen/
>    * aktuell/
>      * ch.so.avt.verkehrszaehlstellen.dxf.zip
>      * ch.so.avt.verkehrszaehlstellen.gpkg.zip
>      * ch.so.avt.verkehrszaehlstellen.shp.zip
>      * ch.so.avt.verkehrszaehlstellen.xtf.zip
>      * meta/
>        * SO_AVT_Verkehrszaehlstellen_Publikation_20190206.ili
>        * publishdate.json      
>   * hist/
>     * 2021.04.12/ -- intern identisch aufgebaut wie Ordner aktuell/
>     * 2021.03.14/
>     * ...
> * ch.so.avt.verkehrszaehlstellen.edit/
>   * aktuell/
>       * ch.so.avt.verkehrszaehlstellen.edit.xtf.zip
>       * meta/
>         * SO_AVT_Verkehrszaehlstellen_20190206.ili  
>         * publishdate.json      
>   * hist/
>     * ...

### Abbildung von Regionen-Einteilungen

Die Regionen werden als Präfix der Dateien abgebildet. Die Ordnerstruktur bleibt gleich. Aufbau Dateiname:   
\[Regionen-Identifier\].\[Datenbereitstellungs-Identifier\].\[Format-Identifier\].zip

Beispiel AV (Regionen-Identifier ist die BFS-NR):

> data/
> * ch.so.agi.av.mopublic/
>   * aktuell/
>     * 2501.ch.so.agi.av.mopublic.dxf.zip
>     * 2501.ch.so.agi.av.mopublic.gpkg.zip
>     * 2501.ch.so.agi.av.mopublic.shp.zip
>     * 2501.ch.so.agi.av.mopublic.xtf.zip
>     * 2502.ch.so.agi.av.mopublic.dxf.zip
>     * 2502.ch.so.agi.av.mopublic.gpkg.zip
>     * 2502.ch.so.agi.av.mopublic.shp.zip
>     * 2502.ch.so.agi.av.mopublic.xtf.zip
>     * ...
>     * meta/
>       * ...    
>   * hist/
>     * ...



## DB -> XTF

Falls die Daten in einer ili2db konformen PostgreSQL Datenbank vorliegen, muss die Datenbank angegeben werden.

    task publishFromDb(type: Publisher){
      dataIdent = "ch.so.agi.vermessung"
      target = [ "sftp://ftp.server.ch/data", "user", "password" ]
      database = ["uri","user","password"]
      dbSchema "av"
      dataset = "dataset"
    }

## Regionen

    task publishFile(type: Publisher){
      dataIdent = "ch.so.agi.vermessung.edit"
      target = [ "sftp://ftp.server.ch/data", "user", "password" ]
      sourcePath = "/path/file.xtf"
      region = "[0-9][0-9][0-9][0-9]"  // muster; ersetzt den filename im sourcePath	  
    }

    task publishFromDb(type: Publisher){
      dataIdent = "ch.so.agi.vermessung.edit"
      target = [ "sftp://ftp.server.ch/data", "user", "password" ]
      database = ["uri","user","password"]
      dbSchema "av"
      region = "[0-9][0-9][0-9][0-9]"  // muster; ersetzt das dataset
    }
    
## Validierung

Die Validierung kann mit einer ilivalidator Konfigurationsdatei konfiguriert werden.

    task publishFile(type: Publisher){
      ...
      validationConfig =  "validationConfig.ini"
    }

## Benutzer-Formate (GPKG, DXF, SHP)

Kann nur ab DB erstellt werden.

    task publishDb(type: Publisher){
      dataIdent = "ch.so.agi.vermessung"
      target = [ "sftp://ftp.server.ch/data", "user", "password" ]
      database = ["uri","user","password"]
      dbSchema "av"
      userFormats = true
    }
    
Falls das Datenmodell der Quelldaten ``DM01AVCH24LV95D`` ist, wird das DXF automatisch in der Geobau-Struktur erstellt. 

## KGDI-Service

    task publishFile(type: Publisher){
      ...
      kgdiService = ["http://api.kgdi.ch/metadata","user","pwd"]
    }
    
## Archiv aufräumen

    task publishFile(type: Publisher){
      ...
      grooming = "grooming.json"
    }

In der Datei grooming.json wird konfiguriert, wie ausgedünnt wird.

    {
      "grooming": {
        "daily": {
           "from": 0,
           "to": 1
        },
        "weekly": {
           "from": 1,
           "to": 4
        },
        "monthly": {
           "from": 4,
           "to": 52
        },
        "yearly": {
           "from": 52,
           "to": null
        }
      }
    }


## Parameter

Parameter | Beschreibung
----------|-------------------
inputFile   | Name der zu transformierenden ITF-Datei.
outputDirectory  | Name des Verzeichnisses in das die zu erstellende Datei geschrieben wird.
zip | Die zu erstellende Datei wird gezippt (Default: false).
