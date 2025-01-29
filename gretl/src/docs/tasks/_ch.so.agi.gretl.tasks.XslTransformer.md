Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
fileExtension | `String` | Fileextension der Resultatdatei. Default: `xtf` | ja
outDirectory | `File` | Verzeichnis, in das die transformierte Datei gespeichert wird. Der Name der transformierten Datei entspricht standardmässig dem Namen der Input-Datei mit Endung `.xtf`. | nein
xmlFile | `Object` | Datei oder FileTree, die/der transformiert werden soll. | nein
xslFile | `Object` | Name der XSLT-Datei, die im `src/main/resources/xslt`-Verzeichnis liegen muss oder File-Objekt (beliebiger Pfad). | nein
: {tbl-colwidths="[20,20,50,10]"}
