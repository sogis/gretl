Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
accessKey | `String` | AccessKey | nein
bucketName | `String` | Name des Buckets, in die Datei oder sämtliche Dateie gelöscht werden sollen. | nein
endPoint | `String` | S3-Endpunkt. Default: `https://s3.eu-central-1.amazonaws.com/` | ja
key | `String` | Name der Datei, die gelöscht werden soll. Wird kein Key definiert, wird der Inhalt des gesamten Buckets gelöscht. | ja
region | `String` | S3-Region. Default: `eu-central-1` | ja
secretKey | `String` | SecretKey | nein
: {tbl-colwidths="[20,20,50,10]"}
