Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
dxfDirectory | `Object` | Verzeichnis, in das die DXF-Dateien gespeichert werden. | nein
isZip | `Boolean` | Die zu erstellende Datei wird gezippt und es werden zusätzliche Dateien (Musterplan, Layerbeschreibung, Hinweise) hinzugefügt. Default: false | ja
itfFiles | `Object` | ITF-Datei, die nach DXF transformiert werden soll. Es können auch mehrere Dateien angegeben werden. File- oder FileCollection-Objekt. | nein
logFile | `Object` | Schreibt die log-Meldungen der Konvertierung in eine Text-Datei. | ja
modeldir | `String` | INTERLIS-Modellrepository. String separiert mit Semikolon (analog ili2db, ilivalidator). | ja
proxy | `String` | Proxy-Server für den Zugriff auf Modell-Repositories. | ja
proxyPort | `Integer` | Proxy-Port für den Zugriff auf Modell-Repositories. | ja
: {tbl-colwidths="[20,20,50,10]"}
