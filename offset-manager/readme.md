# Offset Manager

## Criar tópico

```shell
docker-compose up -d
docker exec -it broker bash
kafka-topics --bootstrap-server 127.0.0.1:9092 --create --topic offset-manager --partitions 3
```