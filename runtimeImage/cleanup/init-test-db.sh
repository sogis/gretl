#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username postgres <<-EOSQL
    CREATE DATABASE gretl;
    CREATE ROLE ddluser WITH LOGIN PASSWORD 'ddluser';
    GRANT ALL PRIVILEGES ON DATABASE gretl TO ddluser;
    CREATE ROLE dmluser WITH LOGIN PASSWORD 'dmluser';
    CREATE ROLE readeruser WITH LOGIN PASSWORD 'readeruser';
    CREATE EXTENSION "uuid-ossp";
    SELECT version();
EOSQL

psql -c 'create extension postgis;' -d gretl -U postgres
psql -c 'select postgis_full_version();' -d gretl -U postgres
