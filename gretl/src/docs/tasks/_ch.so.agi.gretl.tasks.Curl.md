Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
data | `String` | String, der via POST hochgeladen werden soll. Entspricht `curl [URL] --data`. | ja
dataBinary | `File` | Datei, die hochgeladen werden soll. Entspricht `curl [URL] --data-binary`. | ja
expectedBody | `String` | Erwarteter Text, der vom Server als Body zurückgelieferd wird. | ja
expectedStatusCode | `Integer` | Erwarteter Status Code, der vom Server zurückgeliefert wird. | nein
formData | `Map<String,Object>` | Form data parameters. Entspricht `curl [URL] -F key1=value1 -F file1=@my_file.xtf`. | ja
headers | `MapProperty<String,String>` | Request-Header. Entspricht `curl [URL] -H ... -H ....`. | ja
method | `MethodType` | HTTP-Request-Methode. Unterstützt werden `GET` und `POST`. | ja
outputFile | `File` | Datei, in die der Output gespeichert wird. Entspricht `curl [URL] -o`. | ja
password | `String` | Passwort. Wird zusammen mit `user` in einen Authorization-Header umgewandelt. Entspricht `curl [URL] -u user:password`. | ja
serverUrl | `String` | Die URL des Servers inklusive Pfad und Queryparameter. | nein
user | `String` | Benutzername. Wird zusammen mit `password` in einen Authorization-Header umgewandelt. Entspricht `curl [URL] -u user:password`. | ja
: {tbl-colwidths="[20,20,50,10]"}
