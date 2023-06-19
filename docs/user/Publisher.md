# Publisher-Task

GRETL-Task, welcher für Vektordaten die aktuellen Geodaten-Dateien 
bereitstellt und das Archiv der vorherigen Zeitstände pflegt.

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

## Ablauf

Der Publisher arbeitet die folgenden Hauptschritte ab:

1) Verstecktes Verzeichnis für den Datenstand via FTPS erstellen (.yyyy-MM-dd-UUID/). Kein Abbruch, falls das Verzeichnis vorhanden ist.
2) XTFs in Verzeichnis ablegen.
   1) Für Datenthemen mit Quelle=Datenbank: XTF-Transferdateien exportieren.
      1) Mit ili2pg das xtf erzeugen
      2) Prüfung des xtf gegen das Modell. Abbruch bei fatalen Fehlern
      3) Prüf-Bericht (und evtl. Prüf-Konfiguartion) muss auch mit in die ZIP Datei
      4) ZIP-Datei publizieren
   2) Für Datenthemen mit Quelle=XTF: XTF in Verzeichnis kopieren.
      1) Prüfung des xtf gegen das Modell. Abbruch bei fatalen Fehlern
      2) Prüf-Bericht (und evtl. Prüf-Konfiguartion) muss auch mit in die ZIP Datei
      3) ZIP-Datei publizieren   
3) Aus dem Publikations-xtf die Benutzerformate (Geopackage, Shapefile, Dxf) ableiten und ablegen.
4) Metadaten sammeln und im Unterordner meta/ ablegen.
   1) Publikationsdatum
   2) ili-Dateien
   3) Beipackzettel (HTML via REST-API vom SIMI-Service beziehen) 
5) Neue Ordnernamen setzen.
   1) "aktuell" umbenennen auf Ordnername gemäss Datum in publishdate.json und verschieben in "hist".
   2) Verstecktes Verzeichnis umbenennen auf aktuell.
   3) Benutzerformate in "hist" löschen
6) Publikationsdatum via REST-API in den KGDI-Metadaten nachführen
7) Historische Stände ausdünnen.

## Ordnerstruktur im Ziel-Verzeichnis

### Gängiger Fall: Zwei Modelle, keine Regionen

Publikation in den beiden Datenbereitstellungen ch.so.avt.verkehrszaehlstellen und ch.so.avt.verkehrszaehlstellen.edit

Namenskonvention für die Dateien: \[Datenbereitstellungs-Identifier\].\[Format-Identifier\].zip

> data/
> * ch.so.avt.verkehrszaehlstellen/
>    * aktuell/
>      * ch.so.avt.verkehrszaehlstellen.dxf.zip
>        * Tabelle1.dxf
>        * Tabelle2.dxf
>        * ....
>        * validation.log
>        * validation.ini
>      * ch.so.avt.verkehrszaehlstellen.gpkg.zip
>        * ch.so.avt.verkehrszaehlstellen.gpkg
>        * validation.log
>        * validation.ini
>      * ch.so.avt.verkehrszaehlstellen.shp.zip
>        * Tabelle1.prj
>        * Tabelle1.shp
>        * Tabelle1.shx
>        * Tabelle2.dbf
>        * Tabelle2.prj
>        * Tabelle2.shp
>        * Tabelle2.shx
>        * ....
>        * validation.log
>        * validation.ini
>      * ch.so.avt.verkehrszaehlstellen.xtf.zip
>        * ch.so.avt.verkehrszaehlstellen.xtf
>        * validation.log
>        * validation.ini
>      * meta/
>        * SO_AVT_Verkehrszaehlstellen_Publikation_20190206.ili
>        * publishdate.json      
>        * datenbeschreibung.html
>   * hist/
>     * 2021-04-12/ -- intern identisch aufgebaut wie Ordner aktuell/ aber ohne Benutzerformate
>       * ch.so.avt.verkehrszaehlstellen.xtf.zip
>         * ch.so.avt.verkehrszaehlstellen.xtf
>         * validation.log
>         * validation.ini
>       * meta/
>         * SO_AVT_Verkehrszaehlstellen_Publikation_20190206.ili
>         * publishdate.json      
>         * datenbeschreibung.html
>     * 2021-03-14/
>     * ...
> * ch.so.avt.verkehrszaehlstellen.edit/
>   * aktuell/
>       * ch.so.avt.verkehrszaehlstellen.edit.xtf.zip
>         * ch.so.avt.verkehrszaehlstellen.edit.xtf
>         * validation.log
>         * validation.ini
>       * meta/
>         * SO_AVT_Verkehrszaehlstellen_20190206.ili  
>         * publishdate.json      
>         * datenbeschreibung.html
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

## XTF -> XTF

Falls die Daten bereits als XTF-/ITF-Datei vorliegen, muss die Quelldatei angegeben werden.

    task publishFile(type: Publisher){
      dataIdent = "ch.so.agi.vermessung.edit"
      target = [ "sftp://ftp.server.ch/data", "user", "password" ]
      sourcePath = file("/path/file.xtf")
    }

Die Daten können alternativ zu SFTP in ein lokales Verzeichnis publiziert werden:

    task publishFile(type: Publisher){
      dataIdent = "ch.so.agi.vermessung.edit"
      target = [file("/out")]  
      sourcePath = file("/path/file.xtf")
    }

## DB -> XTF

Falls die Daten in einer ili2db konformen PostgreSQL Datenbank vorliegen, muss die Datenbank angegeben werden und 
welche Daten (Parameter dataset, modelsToPublish, regions, region) 
aus der Datenbank exportiert werden sollen.

    task publishFromDb(type: Publisher){
      dataIdent = "ch.so.agi.vermessung"
      target = [ "sftp://ftp.server.ch/data", "user", "password" ]
      database = ["uri","user","password"]
      dbSchema "av"
      dataset = "dataset"
    }

Nur bei einfachen Modellen (falls das DB Schema ohne createBasketCol erstellt worden ist), kann der Export alternativ 
mit dem Parameter modelsToPublish mit Angabe des INTERLIS-Modellnamens erfolgen:

    task publishFromDb(type: Publisher){
      dataIdent = "ch.so.agi.vermessung"
      target = [ "sftp://ftp.server.ch/data", "user", "password" ]
      database = ["uri","user","password"]
      dbSchema "av"
      modelsToPublish = "DM01AVCH24LV95D"
    }

## Regionen

Falls die Daten bereits als XTF-/ITF-Dateien vorliegen, muss zusätzlich zu einer möglichen Quelldatei (sourcePath) 
das Dateinamens-Muster (ohne Nameserweiterung (.xtf oder .itf) der Regionen (region) angegeben werden.

    task publishFile(type: Publisher){
      dataIdent = "ch.so.agi.vermessung.edit"
      target = [ "sftp://ftp.server.ch/data", "user", "password" ]
      sourcePath = file("/transferfiles/file.xtf")
      region = "[0-9][0-9][0-9][0-9]"  // regex; ersetzt den filename im sourcePath
    }

Der sourcePath ist wie bei der Verarbeitung eines XTF (ohne Regionen) ein Datei- und nicht ein Ordner-Pfad.
Mittels Parameter "region" werden aus allem im Ordner "transferfiles" enthaltenen Transfer-Dateien die zu
verarbeitenden selektiert.

Falls die Daten in einer ili2db konformen PostgreSQL Datenbank vorliegen, muss 
das Muster der Datensatz-Namen (dataset) angegeben werden (= ein Datensatz pro Region (region)).
    
    task publishFromDb(type: Publisher){
      dataIdent = "ch.so.agi.vermessung.edit"
      target = [ "sftp://ftp.server.ch/data", "user", "password" ]
      database = ["uri","user","password"]
      dbSchema "av"
      region = "[0-9][0-9][0-9][0-9]"  // regex; ersetzt das dataset
    }

Verarbeitet werden alle Datensätze, deren Dateiname (Quelle Transferdatei) oder dataset Name
(Quelle DB) auf das in Parameter "region" definierte Muster (regular Expression) zutreffen.

### Beispiele für die Verwendung von Regionen

Es können eindeutige Namen oder auch regular expressions verwendet werden.

Export mit region aus lokaler DB (Nutzungsplanung mit 3 Datasets: 2580, 2581, 2582)

    task publishFromDb2(type: Publisher){
      dataIdent = "ch.so.arp.nutzungsplanung.publishFromDb2"
      database = [dbUriPub, dbUserPub, dbPwdPub]
      dbSchema = "arp_nutzungsplanung_pub_v1"
      target = [project.buildDir]
    
      region = "[2][5][8][0]"         //exportiert Dataset 2580
      region = "2580"                 //exportiert Dataset 2580
      region = 2580                   //exportiert Dataset 2580
      region = "[0-9][0-9][0-9][0-9]" //exportiert alle 3 Datasets
      region = ".*"                   //exportiert alle 3 Datasets
      userFormats = true
      kgdiService = ["http://localhost:8080/app/rest","admin","admin"]
    }


4 xtf-Files: a2581.xtf, c2582.xtf, b2583.xtf, d2584.xtf, lokal im Job-Verzeichnis

    task publishFile2(type: Publisher){
      dataIdent = "publishFile2"
      sourcePath = file("a2581.xtf")                                        Angabe zum Ablageort eines der zu publizierenden Files
      target = [project.buildDir]
      
      region = "[a-d][0-9][0-9][0-9][0-9]"  //alle 4 Files werden publiziert
      region = "[2][5][8][4]"               //d2584.xtf wird publiziert
      kgdiService = ["http://localhost:8080/app/rest","admin","admin"]
    }

## Verkettung von Publishern
    
Damit nachfolgende Tasks die Liste der tatsächlich publizierten Regionen auswerten können, 
kann der Parameter publishedRegions des Tasks Publisher verwendet werden.

    task publishFile(type: Publisher){
      dataIdent = "ch.so.agi.vermessung.edit"
      target = [ "sftp://ftp.server.ch/data", "user", "password" ]
      sourcePath = file("/path/file.xtf")
      region = "[0-9][0-9][0-9][0-9]"
    }
    task printPublishedRegions(dependsOn: publishFile){
      doLast(){
        println publishFile.publishedRegions
      }
    }

Der Publisher lässt sich somit auch über die zu publizierenden Regionen verketten.

    task publishFile0(type: Publisher){
      dataIdent = "ch.so.agi.vermessung.edit"
      target = [project.buildDir]
      sourcePath = file("../../../../src/test/resources/data/publisher/files/av_test.itf")
      modeldir= file("../../../../src/test/resources/data/publisher/ili")
      region="[0-9][0-9][0-9][0-9]"
    }
    task publishFile1(type: Publisher){
      dataIdent = "ch.so.agi.vermessung.pub"
      target = [project.buildDir]
      sourcePath = file("../../../../src/test/resources/data/publisher/files/av_test.itf")
      modeldir= file("../../../../src/test/resources/data/publisher/ili")
      regions=publishFile0.publishedRegions
    }

Enthält das aktuelle Verzeichnis schon Daten (von Regionen), werden diese (wie sonst auch historisiert),
und danach mit den neuen Regionen ergänzt. Der Parameter publishedRegions enthält nur die neu 
publizierten Regionen (und nicht alle publizierten Regionen). Auch an den KGDI-Service werden nur die neu 
publizierten Regionen notifiziert (und nicht alle publizierten Regionen).
Die Dateien im meta Unterverzeichnis werden neu erstellt.

## Validierung

Die Validierung kann mit einer ilivalidator Konfigurationsdatei konfiguriert werden.

    task publishFile(type: Publisher){
      ...
      validationConfig =  "validationConfig.ini"
    }

## Benutzer-Formate (GPKG, DXF, SHP)

Optional können Benutzerformate (Geopackage, Shapefile, Dxf) erstellt werden. Die Daten müssen in einer
entsprechend flachen Struktur vorliegen.

    task publishFromDb(type: Publisher){
      dataIdent = "ch.so.agi.vermessung"
      target = [ "sftp://ftp.server.ch/data", "user", "password" ]
      database = ["uri","user","password"]
      dbSchema "av"
      dataset = "dataset"
      userFormats = true
    }

    task publishFromFile(type: Publisher){
      dataIdent = "ch.so.agi.vermessung.edit"
      target = [file("/out")]  
      sourcePath = file("/path/file.xtf")
      userFormats = true
    }
    
Falls das Datenmodell der Quelldaten ``DM01AVCH24LV95D`` ist, wird das DXF automatisch in der Geobau-Struktur erstellt 
und kein Geopackage und kein Shapefile erstellt. 

Die Benutzerformate werden beim Verschieben in den Archiv-Ordner entfernt.

## KGDI-Service

Der Service wird benutzt, um:

- den Beipackzettel (im Unterordner meta) zu erstellen/beziehen
- das Publikationsdatum in den Metadaten nachzuführen

    task publishFile(type: Publisher){
      ...
      kgdiTokenService = ["http://api.kgdi.ch/metadata","superuser","superpwd"]
      kgdiService = ["http://api.kgdi.ch/metadata","user","pwd"]
    }
    
### Beipackzettel beziehen

    HTTP GET: endpoint+"/doc?dataident="+dataIdent+"&published="+versionTag
### Publikationsdatum nachführen

    HTTP PUT: endpoint+"/pubsignal",request

Der Request-Body "Ohne Regionen":
```
{
	"dataIdent": "ch.so.afu.gewaesserschutz",
	"published": "2021-12-23T14:54:49.050062",
	"publishedBaskets": [{
		"model": "SO_AGI_MOpublic_20201009",
		"topic": "Bodenbedeckung",
		"basket": "oltenBID"
	}, {
		"model": "DM01",
		"topic": "Liegenschaften",
		"basket": "wangenBID"
	}]
}
```

Der Request-Body "Mit Regionen":
```
{
	"dataIdent": "ch.so.afu.gewaesserschutz",
	"published": "2021-12-23T14:54:49.050062",
	"publishedRegions": [{
		"region": "olten",
		"publishedBaskets": [{
			"model": "SO_AGI_MOpublic_20201009",
			"topic": "Bodenbedeckung",
			"basket": "oltenBID"
		}]
	}, {
		"region": "wangen",
		"publishedBaskets": [{
			"model": "SO_AGI_MOpublic_20201009",
			"topic": "Bodenbedeckung",
			"basket": "wangenBID"
		}]
	}]
}
```    
    
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

Die ``to`` Angabe muss mit der ``from`` Angabe der nächsthöheren Stufe identisch sein (also z.B daily.to=weekly.from). 
Einzelne Stufen können weggelassen werden. Bei der niedrigsten Stufe (i.d.R. ``daily``) muss ``from=0`` sein. Bei der 
höchsten Stufe (i.d.R. ``yearly``) kann ``to`` definiert oder ``null`` sein. Falls ``to`` definiert ist, wird 
der älteste Stand beim Erreichen des ``to`` Alters gelöscht. 
Falls bei der höchsten Stufe ``to=null``, wird der älteste Stand nicht gelöscht.

## Parameter

Parameter | Beschreibung
----------|-------------------
dataIdent | Identifikator der Daten z.B. "ch.so.agi.vermessung.edit"
target    | Zielverzeichnis z.B. [ "sftp://ftp.server.ch/data", "user", "password" ] oder einfach ein Pfad [file("/out")]
sourcePath | Quelldatei z.B. file("/path/file.xtf")
database  | Datenbank mit Quelldaten z.B. ["uri","user","password"]. Alternative zu sourcePath
dbSchema  | Schema in der Datenbank z.B. "av"
dataset   | ili2db-Datasetname der Quelldaten "dataset" (Das ili2db-Schema muss also immer mit --createBasketCol erstellt werden)
modelsToPublish   | Interlis-Modellnamen der Quelldaten in der DB (Nur für "einfache" Modelle, deren ili2db-Schema ohne --createBasketCol erstellt werden kann)
region    | Muster (Regular Expression) der Dateinamen oder Datasetnamen, falls die Publikation Regionen-weise erfolgt z.B. "[0-9][0-9][0-9][0-9]". Alternative zum Parameter regions,<br/><br/>Bei Quelle "Datei" ist die Angabe einer "stellvertretenden" Transferdatei mittels "sourcePath" zwingend. Bsp.: Bei sourcePath "file("/transferfiles/dummy.xtf")" werden alle im Ordner "transferfiles" enthaltenen Transferdateien mit dem Muster verglichen und bei "match" selektiert und verarbeitet.	  
regions   | Liste der zu publizierenden Regionen (Dateinamen oder Datasetnamen), falls die Publikation Regionen-weise erfolgen soll. Alternative zum Parameter region	  
publishedRegions | Liste der effektiv publizierten Regionen	  
validationConfig |  Konfiguration für die Validierung (eine ilivalidator-config-Datei) z.B. "validationConfig.ini"
userFormats | Benutzerformat (Geopackage, Shapefile, Dxf) erstellen. Default ist false
kgdiTokenService | Endpunkt des Authentifizierung-Services, z.B. ["http://api.kgdi.ch/metadata","user","pwd"]. Publisher ergänzt die URL mit /v2/oauth/token.
kgdiService | Endpunkt des SIMI-Services für die Rückmeldung des Publikationsdatums und die Erstellung des Beipackzettels, z.B. ["http://api.kgdi.ch/metadata","user","pwd"]. Publisher ergänzt die URL fallabhängig mit /pubsignal respektive /doc.
grooming | Konfiguration für die Ausdünnung z.B. "grooming.json". Ohne Angabe wird nicht aufgeräumt.
exportModels | Das Export-Modell, indem die Daten exportiert werden. Der Parameter wird nur bei der Ausdünnung benötigt. Als Export-Modelle sind Basis-Modelle zulässig. 
modeldir     | Dateipfade, die Modell-Dateien (ili-Dateien) enthalten. Mehrere Pfade können durch Semikolon ‚;‘ getrennt werden. Es sind auch URLs von Modell-Repositories möglich. Default: ``%ITF_DIR;http://models.interlis.ch/``. ``%ITF_DIR`` ist ein Platzhalter für das Verzeichnis mit der ITF-Datei.
proxy        | Proxy Server für den Zugriff auf Modell Repositories
proxyPort    | Proxy Port für den Zugriff auf Modell Repositories
