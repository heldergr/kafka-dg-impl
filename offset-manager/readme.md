# Offset Management

- [Posicionamento de offset](#posicionamento-de-offset)
    - [API Java](#api-java)
    - [Linha de comando](#linha-de-comando)

O Apache Kafka é uma ferramenta de *publish/subscribe* que pode ser usada em arquiteturas orientadas a eventos. Uma de 
suas principais características, que a diferenciam de outras soluções de comunicação assíncrona, é o fato de ser *replayable*,
o que significa que mensagens que são mantidas persistidas em um tópico podem ser reprocessadas de acordo com a necessidade
das aplicações.

## Posicionamento de offset

Para fazer o reprocessamento muitas vezes precisamos fazer uma operação onde movemos o offset para determinada posição para 
então recomeçar o processamento. Outra situação em que o reposicionamento pode ser necessário é quando queremos consumir mensagens individuais, onde o offset
deve ser movido para a posição da mensagem desejada.

Esta operação pode ser feita através de API Java de alto nível ou ferramenta de linha de comando.

### API Java

Através da API Java de [*Consumer*](https://kafka.apache.org/26/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html) 
é possível fazer operações de *seek*, onde posicionamos o offset em local desejado. 

As opções são:

- *seekToBeginning*: Move para o primeiro offset de cada partição
- *seekToEnd*: Move para o último offset de cada partição
- *seek*: Reposiciona o offset de uma partição para algum valor específico

As operações *seekToBeginning* e *seekToEnd* são operações *lazy*, o que significa que não têm resultado efetivo de imediato.
Para que tenha efeito é necessário fazer um *poll* ou executar o método *position* para cada partição e assim dessa forma
o reposicionamento ter efeito.

#### Seek to beginning

```java
    final var props = ConsumerProperties.createProperties();
    try (final var consumer = new KafkaConsumer<String, String>(props)) {
        // Obtem informação das partições
        final var partitionInfos = consumer.partitionsFor(topic);
        
        // Cria informações de tópicos/partições
        final var topicPartitions = partitionInfos.stream()
            .map(this::createTopicPartition).collect(Collectors.toList());
        
        // Atribui partições ao consumer
        consumer.assign(topicPartitions);
        
        // Move offset para início
        consumer.seekToBeginning(topicPartitions);
        
        // Executa método para efetivar o reposicionamento dos offsets
        topicPartitions.forEach(consumer::position);
    }
```

#### Seek to end

```java

```

#### Seek

```java

```

### Linha de comando

*Documentação a ser feita.*

## Referências

### Artigos

- [API Consumer](https://kafka.apache.org/26/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html)

### Criação de tópicos para execução local

```shell
docker-compose up -d
docker exec -it broker bash
kafka-topics --bootstrap-server 127.0.0.1:9092 --create --topic offset-manager --partitions 3
```

### Problemas encontrados

Ao executar testes desta documentação alguns problemas foram encontrados e aqui documentados.

#### Erro de no current assignment for partition

Antes de executar a operação de seek é necessário que as partições cujos offsets serão movidos estejam atribuídas ao
consumidor que vai fazer a execução.

```
Exception in thread "main" java.lang.IllegalStateException: No current assignment for partition offset-manager-0
	at org.apache.kafka.clients.consumer.internals.SubscriptionState.assignedState(SubscriptionState.java:367)
	at org.apache.kafka.clients.consumer.internals.SubscriptionState.lambda$requestOffsetReset$3(SubscriptionState.java:619)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
```

  