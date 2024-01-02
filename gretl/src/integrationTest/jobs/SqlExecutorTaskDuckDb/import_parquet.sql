LOAD spatial;

CREATE TABLE bewilligte_ews AS SELECT * FROM read_parquet(${pwd}||'/data/ch.so.afu.bewilligte_erdwaermeanlagen.parquet');

SELECT count(*) FROM bewilligte_ews;

CREATE TABLE abbaustelle AS SELECT * FROM ST_Read(${pwd}||'/data/ch.so.afu.abbaustellen.gpkg', layer='abbaustelle');

SELECT count(*) FROM abbaustelle;
