Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
csvFile | `File` | CSV-Datei, die konvertiert werden soll. | nein
encoding | `String` | Zeichencodierung der CSV-Datei, z.B. `UTF-8`. Default: Systemeinstellung | ja
firstLineIsHeader | `Boolean` | Definiert, ob eine Headerzeile geschrieben werden soll, oder nicht. Default: true | ja
modeldir | `String` | INTERLIS-Modellrepository. String separiert mit Semikolon (analog ili2db, ilivalidator). | ja
models | `String` | INTERLIS-Modell für Definition der Datentypen in der Excel-Datei. | ja
outputFile | `File` | Excel-Datei (.xlsx), in die die CSV-Datei umgewandelt resp. importiert werden soll. | nein
valueDelimiter | `Character` | Zeichen, das am Anfang und Ende jeden Wertes geschrieben werden soll. Default `"` | ja
valueSeparator | `Character` | Zeichen, das als Trennzeichen zwischen den Werten verwendet werden soll. Default: `,` | ja
: {tbl-colwidths="[20,20,50,10]"}
