Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
batchSize | `Integer` | Anzahl der Records, die pro Batch in die Ziel-Datenbank geschrieben werden. Default: 5000) | ja
dataFile | `File` | Name der SHP-Datei, die gelesen werden soll. | nein
database | `Connector` | Datenbank, in die importiert werden soll. | nein
encoding | `String` | Zeichencodierung der SHP-Datei, z.B. `UTF-8`. Default: Systemeinstellung | ja
schemaName | `String` | Name des DB-Schemas, in dem die DB-Tabelle ist. | ja
tableName | `String` | Name der DB-Tabelle, in die importiert werden soll. | nein
: {tbl-colwidths="[20,20,50,10]"}
