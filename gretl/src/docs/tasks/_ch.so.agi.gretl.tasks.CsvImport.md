Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
batchSize | `Integer` | Anzahl der Records, die pro Batch in die Ziel-Datenbank geschrieben werden. Default: `5000` | ja
dataFile | `File` | CSV-Datei, die importiert werden soll. | nein
database | `Connector` | Datenbank in die importiert werden soll. | nein
encoding | `String` | Zeichencodierung der CSV-Datei, z.B. `UTF-8`. Default: Systemeinstellung. | ja
isFirstLineIsHeader | `Boolean` | Definiert, ob die CSV-Datei einer Headerzeile hat, oder nicht. Default: `true` | ja
schemaName | `String` | Name des DB-Schemas, in dem die DB-Tabelle ist. | ja
tableName | `String` | Name der DB-Tabelle, in die importiert werden soll. | nein
valueDelimiter | `Character` | Zeichen, das am Anfang und Ende jeden Wertes vorhanden ist. Default `"` | ja
valueSeparator | `Character` | Zeichen, das als Trennzeichen zwischen den Werten interpretiert werden soll. Default: `,` | ja
: {tbl-colwidths="[20,20,50,10]"}
