GRANT ALL PRIVILEGES ON DATABASE gretl TO ddluser;
CREATE ROLE dmluser WITH LOGIN PASSWORD 'dmluser';
CREATE ROLE readeruser WITH LOGIN PASSWORD 'readeruser';
ALTER DATABASE gretl SET postgis.gdal_enabled_drivers TO 'GTiff PNG JPEG';
