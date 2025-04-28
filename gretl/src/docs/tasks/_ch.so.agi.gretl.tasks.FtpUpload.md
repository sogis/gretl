Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
controlKeepAliveTimeout | `Long` | Timeout bis ein NOOP über den Kontroll-Kanal versendet wird. Default: 300s (=5 Minuten) | ja
fileSeparator | `String` | Default: `/`. (Falls systemType Windows ist, ist der Default `\`. | ja
fileType | `String` | `ASCII` oder `BINARY`. Default: `ASCII`. | ja
localFile | `File` | Lokale Datei, die auf den FTP-Server hochgeladen werden soll. | nein
passiveMode | `Boolean` | Aktiv- oder Passiv-Verbindungsmodus. Default: Passiv (true) | ja
password | `String` | Passwort für den Zugriff auf dem Server. | nein
remoteDir | `String` | Verzeichnis auf dem FTP-Server, in dem die lokale Datei gespeichert werden soll. | nein
server | `String` | Name des Servers (ohne ftp://). | nein
systemType | `String` | `UNIX` oder `WINDOWS`. Default: `UNIX`. | ja
user | `String` | Benutzername auf dem Server. | nein
: {tbl-colwidths="[20,20,50,10]"}
