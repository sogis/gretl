Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
controlKeepAliveTimeout | `Long` | Timeout bis ein NOOP über den Kontroll-Kanal versendet wird. Default: 300s (=5 Minuten) | ja
fileSeparator | `String` | Default: `/`. (Falls systemType Windows ist, ist der Default `\`. | ja
fileType | `String` | `ASCII` oder `BINARY`. Default: `ASCII`. | ja
localDir | `File` | Lokales Verzeichnis, in dem die Dateien gespeichert werden. | nein
passiveMode | `Boolean` | Aktiv- oder Passiv-Verbindungsmodus. Default: Passiv (true) | ja
password | `String` | Passwort für den Zugriff auf dem Server. | nein
remoteDir | `String` | Verzeichnis auf dem Server. | nein
remoteFile | `Object` | Dateiname oder Liste der Dateinamen auf dem Server (kann auch ein Muster sein (* oder ?)). Ohne diesen Parameter werden alle Dateien aus dem Remoteverzeichnis heruntergeladen. | ja
server | `String` | Name des Servers (ohne ftp://). | nein
systemType | `String` | `UNIX` oder `WINDOWS`. Default: `UNIX`. | ja
user | `String` | Benutzername auf dem Server. | nein
: {tbl-colwidths="[20,20,50,10]"}
