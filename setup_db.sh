#!/bin/bash

docker-compose up postgres postgres-replica -d &&
    sudo cp -v ./configdb/postgresql.server.conf ./datadb/postgres_data/postgresql.conf &&
    sudo cp -v ./configdb/pg_hba.server.conf ./datadb/postgres_data/pg_hba.conf &&
    sleep 10 &&
    docker-compose exec postgres bash -c "pg_basebackup -D /backup/ -R -v -U postgres && mv /backup/ /var/lib/postgresql/data/ && chown -R postgres:postgres /var/lib/postgresql/data/backup/" &&
    docker-compose down postgres-replica &&
    docker rmi esettlement-api-postgres-replica &&
    sudo bash -c "rm -rfv ./datadb/postgres_replica_data/*" &&
    sudo bash -c "mv -vf ./datadb/postgres_data/backup/* ./datadb/postgres_replica_data/" &&
    docker-compose exec postgres bash -c "mkdir /var/lib/postgresql/data/archiver/ && chown -R postgres:postgres /var/lib/postgresql/data/archiver/" &&
    sudo cp -v ./configdb/postgresql.replica.conf ./datadb/postgres_replica_data/postgresql.conf &&
    sudo cp -v ./configdb/pg_hba.replica.conf ./datadb/postgres_replica_data/pg_hba.conf &&
    sudo cp -v ./configdb/postgresql.replica.auto.conf ./datadb/postgres_replica_data/postgresql.auto.conf &&
    docker-compose up postgres postgres-replica -d
