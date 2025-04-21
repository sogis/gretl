Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
accessKey | `String` | AccessKey | nein
acl | `String` | Access Control Layer `[private, public-read, public-read-write, authenticated-read, aws-exec-read, bucket-owner-read, bucket-owner-full-control]` | nein
bucketName | `String` | Name des Buckets, in dem die Dateien gespeichert werden sollen. | nein
contentType | `String` | Content-Type | ja
endPoint | `String` | S3-Endpunkt. Default: `https://s3.eu-central-1.amazonaws.com/` | ja
metaData | `Map<String,String>` | Metadaten des Objektes resp. der Objekte, z.B. `["lastModified":"2020-08-28"]` | ja
region | `String` | S3-Region. `Default: eu-central-1` | nein
secretKey | `String` | SecretKey | nein
sourceDir | `File` | Verzeichnis mit den Dateien, die hochgeladen werden sollen. | ja
sourceFile | `File` | Datei, die hochgeladen werden soll. | ja
sourceFiles | `FileCollection` | `FileCollection` mit den Dateien, die hochgeladen werden sollen, z.B. `fileTree("/path/to/directoy/") { include "*.itf" }` | ja
: {tbl-colwidths="[20,20,50,10]"}
