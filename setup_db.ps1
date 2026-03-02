# Запускаємо контейнери primary та replica
docker-compose up postgres postgres-replica -d

# Копіюємо конфіги primary
Copy-Item -Verbose ./configdb/postgresql.server.conf -Destination ./datadb/postgres_data/postgresql.conf -Force
Copy-Item -Verbose ./configdb/pg_hba.server.conf -Destination ./datadb/postgres_data/pg_hba.conf -Force

# Чекаємо 10 секунд
Start-Sleep -Seconds 10

# Робимо base backup через docker exec
docker-compose exec postgres bash -c "pg_basebackup -D /backup/ -R -v -U postgres && mv /backup/ /var/lib/postgresql/data/ && chown -R postgres:postgres /var/lib/postgresql/data/backup/"

# Зупиняємо лише replica
docker-compose stop postgres-replica

# Видаляємо образ репліки
docker rmi esettlement-api-postgres-replica

# Очищаємо директорію репліки
Remove-Item -Recurse -Force -Verbose ./datadb/postgres_replica_data\*

# Переміщуємо файли з бекапу до репліки
Move-Item -Verbose ./datadb/postgres_data/backup\* -Destination ./datadb/postgres_replica_data/ -Force

# Створюємо архівну папку в контейнері
docker-compose exec postgres bash -c "mkdir /var/lib/postgresql/data/archiver/ && chown -R postgres:postgres /var/lib/postgresql/data/archiver/"

# Копіюємо конфіги для репліки
Copy-Item -Verbose ./configdb/postgresql.replica.conf -Destination ./datadb/postgres_replica_data/postgresql.conf -Force
Copy-Item -Verbose ./configdb/pg_hba.replica.conf -Destination ./datadb/postgres_replica_data/pg_hba.conf -Force
Copy-Item -Verbose ./configdb/postgresql.replica.auto.conf -Destination ./datadb/postgres_replica_data/postgresql.auto.conf -Force

# Запускаємо репліку
docker-compose up -d

