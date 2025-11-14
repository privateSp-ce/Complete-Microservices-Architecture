#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE users_db;
    CREATE DATABASE restaurants_db;
    CREATE DATABASE orders_db;
    CREATE DATABASE payments_db;
    CREATE DATABASE pricing_db;
EOSQL