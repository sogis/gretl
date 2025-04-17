Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
allObjectsAccessible | `Boolean` | Mit der Option nimmt der Validator an, dass er Zugriff auf alle Objekte hat. D.h. es wird z.B. auch die Multiplizität von Beziehungen auf externe Objekte geprüft. Default: false | ja
configFile | `Object` | Konfiguriert die Datenprüfung mit Hilfe einer ini-Datei (um z.B. die Prüfung von einzelnen Constraints auszuschalten). Siehe https://github.com/claeis/ilivalidator/blob/master/docs/ilivalidator.rst#konfiguration | ja
dataFiles | `Object` | Liste der Dateien, die validiert werden sollen. `FileCollection` oder `List`. Eine leere Liste ist kein Fehler. | nein
disableAreaValidation | `Boolean` | Schaltet die AREA-Topologieprüfung aus. Default: false | ja
encoding | `String` | Zeichencodierung der CSV-Datei, z.B. `UTF-8`. Default: Systemeinstellung | ja
failOnError | `Boolean` | Steuert, ob der Task bei einem Validierungsfehler fehlschlägt. Default: true | ja
firstLineIsHeader | `Boolean` | Definiert, ob die CSV-Datei einer Headerzeile hat, oder nicht. Default: `true` | ja
forceTypeValidation | `Boolean` | Ignoriert die Konfiguration der Typprüfung aus der TOML-Datei, d.h. es kann nur die Multiplizität aufgeweicht werden. Default: false | ja
logFile | `Object` | Schreibt die log-Meldungen der Validierung in eine Text-Datei. | ja
metaConfigFile | `Object` | Konfiguriert den Validator mit Hilfe einer ini-Datei. Siehe https://github.com/claeis/ilivalidator/blob/master/docs/ilivalidator.rst#konfiguration | ja
modeldir | `String` | INTERLIS-Modellrepository. `String`, separiert mit Semikolon (analog ili2db, ilivalidator). | ja
models | `String` | INTERLIS-Modell, gegen das die Dateien geprüft werden sollen (mehrere Modellnamen durch Semikolon trennen). Default: Der Name der CSV-Datei. | ja
multiplicityOff | `Boolean` | Schaltet die Prüfung der Multiplizität generell aus. Default: false | ja
pluginFolder | `Object` | Verzeichnis mit JAR-Dateien, die Zusatzfunktionen enthalten. | ja
proxy | `String` | Proxy-Server für den Zugriff auf Modell-Repositories. | ja
proxyPort | `Integer` | Proxy-Port für den Zugriff auf Modell-Repositories. | ja
skipPolygonBuilding | `Boolean` | Schaltet die Bildung der Polygone aus (nur ITF). Default: false | ja
validationOk | `boolean` | OUTPUT: Ergebnis der Validierung. Nur falls failOnError=false. | nein
valueDelimiter | `Character` | Zeichen, das am Anfang und Ende jeden Wertes vorhanden ist. Default `"` | ja
valueSeparator | `Character` | Zeichen, das als Trennzeichen zwischen den Werten interpretiert werden soll. Default: `,` | ja
xtflogFile | `Object` | Schreibt die log-Meldungen in eine INTERLIS-2-Datei. Die Datei result.xtf entspricht dem Modell IliVErrors. | ja
: {tbl-colwidths="[20,20,50,10]"}
