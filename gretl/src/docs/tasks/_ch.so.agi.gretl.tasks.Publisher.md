Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
dataIdent | `String` | Identifikator der Daten z.B. "ch.so.agi.vermessung.edit". | nein
database | `Connector` | Datenbank mit Quelldaten z.B. `["uri","user","password"]`. Alternative zu sourcePath. | ja
dataset | `String` | ili2db-Datasetname der Quelldaten "dataset" (Das ili2db-Schema muss also immer mit `--createBasketCol` erstellt werden) | ja
dbSchema | `String` | Schema in der Datenbank z.B. `"av"` | ja
exportModels | `String` | Das Export-Modell, indem die Daten exportiert werden. Der Parameter wird nur bei der Ausdünnung benötigt. Als Export-Modelle sind Basis-Modelle zulässig. | ja
grooming | `Object` | Konfiguration für die Ausdünnung z.B. `"grooming.json"`. Ohne Angabe wird nicht aufgeräumt. | ja
isUserFormats | `Boolean` | Benutzerformat (Geopackage, Shapefile, Dxf) erstellen. Default: false | ja
kgdiService | `Endpoint` | Endpunkt des SIMI-Services für die Rückmeldung des Publikationsdatums und die Erstellung des Beipackzettels, z.B. `["http://api.kgdi.ch/metadata","user","pwd"]`. Publisher ergänzt die URL fallabhängig mit `/pubsignal` respektive `/doc`. | ja
kgdiTokenService | `Endpoint` | Endpunkt des Authentifizierung-Services, z.B. `["http://api.kgdi.ch/metadata","user","pwd"]`. Publisher ergänzt die URL mit `/v2/oauth/token`. | ja
modeldir | `String` | Dateipfade, die Modell-Dateien (ili-Dateien) enthalten. Mehrere Pfade können durch Semikolon `;` getrennt werden. Es sind auch URLs von Modell-Repositories möglich. Default: `%ITF_DIR;http://models.interlis.ch/`. `%ITF_DIR` ist ein Platzhalter für das Verzeichnis mit der ITF-Datei. | ja
modelsToPublish | `String` | INTERLIS-Modellnamen der Quelldaten in der DB (Nur für "einfache" Modelle, deren ili2db-Schema ohne `--createBasketCol` erstellt werden kann). | ja
proxy | `String` | Proxy-Server für den Zugriff auf Modell-Repositories. | ja
proxyPort | `Integer` | Proxy-Port für den Zugriff auf Modell-Repositories. | ja
publishedRegions | `ListProperty<String>` | Liste der effektiv publizierten Regionen. | nein
region | `String` | Muster (Regular Expression) der Dateinamen oder Datasetnamen, falls die Publikation Regionen-weise erfolgt z.B. `"[0-9][0-9][0-9][0-9]"`. Alternative zum Parameter regions,<br/><br/>Bei Quelle "Datei" ist die Angabe einer "stellvertretenden" Transferdatei mittels "sourcePath" zwingend. Bsp.: Bei sourcePath `file("/transferfiles/dummy.xtf")` werden alle im Ordner "transferfiles" enthaltenen Transferdateien mit dem Muster verglichen und bei "match" selektiert und verarbeitet. | ja
regions | `ListProperty<String>` | Liste der zu publizierenden Regionen (Dateinamen oder Datasetnamen), falls die Publikation Regionen-weise erfolgen soll. Alternative zum Parameter `region`. | ja
sourcePath | `Object` | Quelldatei z.B. `file("/path/file.xtf")` | ja
target | `Endpoint` | Zielverzeichnis z.B. `[ "sftp://ftp.server.ch/data", "user", "password" ]` oder einfach ein Pfad `[file("/out")]` | nein
validationConfig | `Object` | Konfiguration für die Validierung (eine ilivalidator-config-Datei) z.B. "validationConfig.ini". | ja
version | `Date` | null | ja
: {tbl-colwidths="[20,20,50,10]"}
