Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
database | `ListProperty<String>` | Datenbank, in die importiert werden soll. | nein
sqlFiles | `ListProperty<String>` | Name der SQL-Datei aus der SQL-Statements gelesen und ausgeführt werden. | nein
sqlParameters | `Property<Object>` | Eine Map mit Paaren von Parameter-Name und Parameter-Wert (`Map<String,String>`). Oder eine Liste mit Paaren von Parameter-Name und Parameter-Wert (`List<Map<String,String>>`). | ja
: {tbl-colwidths="[20,20,50,10]"}
