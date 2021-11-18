# Offset Manager

## Criar tópico

```shell
docker-compose up -d
docker exec -it broker bash
kafka-topics --bootstrap-server 127.0.0.1:9092 --create --topic offset-manager --partitions 3
```

## Problemas encontrados

### Erro de no current assignment for partition

```
Exception in thread "main" java.lang.IllegalStateException: No current assignment for partition offset-manager-0
	at org.apache.kafka.clients.consumer.internals.SubscriptionState.assignedState(SubscriptionState.java:367)
	at org.apache.kafka.clients.consumer.internals.SubscriptionState.lambda$requestOffsetReset$3(SubscriptionState.java:619)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
```