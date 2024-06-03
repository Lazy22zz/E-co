#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Create the database
cs166_createdb $USER"_project_phase_3_DB"

# Create tables
cs166_psql -p $PGPORT $USER"_project_phase_3_DB" < $DIR/../src/create_tables.sql

# Create indexes
cs166_psql -p $PGPORT $USER"_project_phase_3_DB" < $DIR/../src/create_indexes.sql

# Load data
cs166_psql -p $PGPORT $USER"_project_phase_3_DB" < $DIR/../src/load_data.sql


