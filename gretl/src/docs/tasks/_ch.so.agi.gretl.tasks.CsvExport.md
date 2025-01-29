Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
attributes | `String[]` | Spalten der DB-Tabelle, die exportiert werden sollen. Definiert die Reihenfolge der Spalten in der CSV-Datei. Default: alle Spalten | ja
dataFile | `Object` | Name der CSV-Datei, die erstellt werden soll. | nein
database | `Connector` | Datenbank aus der exportiert werden soll. | nein
encoding | `String` | Zeichencodierung der CSV-Datei, z.B. "UTF-8". Default: Systemeinstellung | ja
firstLineIsHeader | `Boolean` | Definiert, ob eine Headerzeile geschrieben werden soll, oder nicht. Default: true | ja
schemaName | `String` | Name des DB-Schemas, in dem die DB-Tabelle ist. | ja
tableName | `String` | Name der DB-Tabelle, die exportiert werden soll. | nein
valueDelimiter | `Character` | Zeichen, das am Anfang und Ende jeden Wertes geschrieben werden soll. Default `"` | ja
valueSeparator | `Character` | Zeichen, das als Trennzeichen zwischen den Werten verwendet werden soll. Default: `,` | ja
: {tbl-colwidths="[20,20,50,10]"}
