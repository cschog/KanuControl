#!/bin/bash

DB="kanu"
USER="kcuser"

DATE=$(date +"%Y-%m-%d")
TARGET="./backup/$DATE"

mkdir -p "$TARGET"

echo "Starte Tenant-Backup..."

SCHEMAS=$(psql -U $USER -d $DB -t -c "
SELECT schema_name
FROM information_schema.schemata
WHERE schema_name NOT IN (
    'public',
    'information_schema',
    'pg_catalog',
    'kanu',
    'audit'
)
AND schema_name NOT LIKE 'pg_%';
")

for schema in $SCHEMAS
do
    echo "Backup Schema: $schema"

    pg_dump \
        -U $USER \
        -d $DB \
        -n $schema \
        > "$TARGET/$schema.sql"
done

echo "Backup abgeschlossen."