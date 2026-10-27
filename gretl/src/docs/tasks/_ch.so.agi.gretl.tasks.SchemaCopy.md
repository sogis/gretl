Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
database | `Connector` | Datenbank mit dem Quell- und den Ziel-Schemas. | nein
sourceSchema | `String` | Name des Quell-Schemas. | nein
tables | `List<String>` | Namen der ungefiltert zu kopierenden Tabellen. | ja
tablesWithFilter | `List<String>` | Tabellenname und Whereclause für gefiltertes Befüllen der Zieltabelle. 
 Liste nach folgendem Muster paarweise befüllen: 
 Tabelle1 Name, Tabelle1 Filter, Tabelle2 Name, Tabelle2 Filter, ...
 Die Zeilenfilter dürfen {@code WHERE} nicht enthalten. | ja
targetSchema | `String` | Name eines einzelnen Ziel-Schemas. Bei mehreren Zielschemas {@link #getTargetSchemas()} nutzen. | ja
targetSchemas | `List<String>` | Namen der Ziel-Schemas. Bei einem Zielschema ist es einfacher {@link #getTargetSchema()} zu nutzen. | ja
: {tbl-colwidths="[20,20,50,10]"}
