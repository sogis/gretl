#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username postgres <<-EOSQL
    DROP EXTENSION IF EXISTS "uuid-ossp";
    DROP DATABASE IF EXISTS gretl;
    DROP ROLE IF EXISTS ddluser;
    DROP ROLE IF EXISTS dmluser;
    DROP ROLE IF EXISTS readeruser;
EOSQL
