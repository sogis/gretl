Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
batchSize | `Integer` | Anzahl der Records, die pro Batch in die Ziel-Datenbank (GeoPackage) geschrieben werden. Default: 5000 | ja
dataFile | `Object` | Name der GeoPackage-Datei, die erstellt werden soll. | nein
database | `Connector` | Datenbank aus der exportiert werden soll. | nein
dstTableName | `Object` | Name der Tabelle(n) in der GeoPackage-Datei. `String` oder `List`. | nein
fetchSize | `Integer` | Anzahl der Records, die pro Fetch aus der Quell-Datenbank gelesen werden. Default: 5000 | ja
schemaName | `String` | Name des DB-Schemas, in dem die DB-Tabelle ist. | ja
srcTableName | `Object` | Name der DB-Tabelle(n), die exportiert werden soll(en). `String` oder `List`. | nein
: {tbl-colwidths="[20,20,50,10]"}
