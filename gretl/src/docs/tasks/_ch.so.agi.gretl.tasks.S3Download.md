Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
accessKey | `String` | AccessKey | nein
bucketName | `String` | Name des Buckets, in dem die Datei gespeichert ist. | nein
downloadDir | `File` | Verzeichnis, in das die Datei heruntergeladen werden soll. | nein
endPoint | `String` | S3-Endpunkt. Default: `https://s3.eu-central-1.amazonaws.com/` | ja
key | `String` | Name der Datei. Wird kein Key definiert, wird der Inhalt des gesamten Buckets heruntergeladen. | ja
region | `String` | S3-Region. Default: `eu-central-1` | ja
secretKey | `String` | SecretKey | nein
: {tbl-colwidths="[20,20,50,10]"}
