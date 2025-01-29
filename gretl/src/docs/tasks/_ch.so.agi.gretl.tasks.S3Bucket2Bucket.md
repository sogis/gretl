Parameter | Datentyp | Beschreibung | Optional
----------|----------|-------------|-------------
accessKey | `String` | AccessKey | nein
acl | `String` | Access Control Layer `[private, public-read, public-read-write, authenticated-read, aws-exec-read, bucket-owner-read, bucket-owner-full-control]` | nein
endPoint | `String` | S3-Endpunkt. Default: `https://s3.eu-central-1.amazonaws.com/` | ja
metaData | `Map<String,String>` | Metadaten des Objektes resp. der Objekte, z.B. `["lastModified":"2020-08-28"]`. | ja
region | `String` | S3-Region. Default: `eu-central-1` | ja
secretKey | `String` | SecretKey | nein
sourceBucket | `String` | Bucket, aus dem die Objekte kopiert werden. | nein
targetBucket | `String` | Bucket, in den die Objekte kopiert werden. | nein
: {tbl-colwidths="[20,20,50,10]"}
