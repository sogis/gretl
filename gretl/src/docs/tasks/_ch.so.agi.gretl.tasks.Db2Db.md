Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
batchSize | `Property<Integer>` | Anzahl der Records, die pro Batch in die Ziel-Datenbank geschrieben werden (Default: 5000). Für sehr grosse Tabellen muss ein kleinerer Wert gewählt werden. | ja
fetchSize | `Property<Integer>` | Anzahl der Records, die auf einmal vom Datenbank-Cursor von der Quell-Datenbank zurückgeliefert werden (Standard: 5000). Für sehr grosse Tabellen muss ein kleinerer Wert gewählt werden. | ja
sourceDb | `ListProperty<String>` | Datenbank, aus der gelesen werden soll. | nein
sqlParameters | `Property<Object>` | Eine Map mit Paaren von Parameter-Name und Parameter-Wert (`Map<String,String>`). Oder eine Liste mit Paaren von Parameter-Name und Parameter-Wert (`List<Map<String,String>>`). | ja
targetDb | `ListProperty<String>` | Datenbank, in die geschrieben werden soll. | nein
transferSets | `ListProperty<TransferSet>` | Eine Liste von `TransferSet`s. | nein
: {tbl-colwidths="[20,20,50,10]"}
