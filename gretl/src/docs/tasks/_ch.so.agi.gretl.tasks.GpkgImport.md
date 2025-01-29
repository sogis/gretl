Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
batchSize | `Integer` | Anzahl der Records, die pro Batch in die Ziel-Datenbank geschrieben werden. Default: 5000 | ja
dataFile | `Object` | Name der GeoPackage-Datei, die gelesen werden soll. | nein
database | `Connector` | Datenbank, in die importiert werden soll. | nein
dstTableName | `String` | Name der DB-Tabelle, in die importiert werden soll. | nein
fetchSize | `Integer` | Anzahl der Records, die pro Fetch aus der Quell-Datenbank gelesen werden. Default: 5000 | ja
schemaName | `String` | Name des DB-Schemas, in dem die DB-Tabelle ist. | ja
srcTableName | `String` | Name der GeoPackage-Tabelle, die importiert werden soll. | nein
: {tbl-colwidths="[20,20,50,10]"}
