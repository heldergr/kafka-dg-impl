# Offset Management

- [Posicionamento de offset](#posicionamento-de-offset)
    - [API Java](#api-java)
    - [Utilitário kafka-consumer-groups](#utilitrio-kafka-consumer-groups)
    - [Utilitário kafka-streams-application-reset](#utilitrio-kafka-streams-application-reset)
- [Referências](#referncias)

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
    final var props = ConsumerProperties.createProperties();
    try (final var consumer = new KafkaConsumer<String, String>(props)) {
        // Obtem informação das partições
        final var partitionInfos = consumer.partitionsFor(topic);
        
        // Cria informações de tópicos/partições
        final var topicPartitions = partitionInfos.stream()
            .map(this::createTopicPartition).collect(Collectors.toList());

        // Atribui partições ao consumer
        consumer.assign(topicPartitions);

        // Move offset para fim
        consumer.seekToEnd(topicPartitions);

        // Executa método para efetivar o reposicionamento dos offsets
        topicPartitions.forEach(consumer::position);
    }
```

#### Seek

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

        // Move offset para particao especifica e executa position() para efetivacao do novo offset
        topicPartitions.forEach(topicPartition -> {
            consumer.seek(topicPartition, offset);
            consumer.position(topicPartition);
        });
    }
```

### Utilitário kafka-consumer-groups

O reposicionamento de offset via linha de comando é feito pelo utilitário *kafka-consumer-groups*, que assim como outras 
ferramentas do Kafka tem como parâmetro obrigatório --bootstrap-server para informar a lista de brokers.

A operação que faz a mudança é a *--reset-offsets*, que espera outros parâmetros informando a estratégia para definição
dos novos offsets. Muito importante notar que as opções *to_latest*, *to_earlist* e *to_offset* serão aplicadas as 
**todas as partições do tópico**. Caso se deseje informar valores específicos para partições específicas, deve-se
usar a opção *from-file*, onde são informados tópicos, partições e offsets específicos.

Nas seções seguintes há exemplos de variadas formas como o offset pode ser reposicionado. Na versão atual do Kafka ainda
é necessário usar a opção *--execute* no final do comando para que o valor seja realmente alterado. Segundo a documentação, 
em versões futuras pode ser que não seja necessário.

#### Reset para início (to-earliest)

Move o offset de todas as partições de um tópico para um grupo de consumo (ou todos, depende do parâmetro) para o offset
mais antigo de cada partição.

```shell
# Comando
$ kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --reset-offsets --to-earliest \ 
--group group-offset-manager --topic offset-manager --execute

# Resultado
GROUP                          TOPIC                          PARTITION  NEW-OFFSET     
group-offset-manager           offset-manager                 0          0              
group-offset-manager           offset-manager                 1          0              
group-offset-manager           offset-manager                 2          0   
```

#### Reset para fim (to-latest)

Move o offset de todas as partições de um tópico para um grupo de consumo (ou todos, depende do parâmetro) para o offset
mais recente de cada partição.

```shell
# Comando
$ kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --reset-offsets --to-latest \
--group group-offset-manager --topic offset-manager --execute

# Resultado
GROUP                          TOPIC                          PARTITION  NEW-OFFSET     
group-offset-manager           offset-manager                 0          26             
group-offset-manager           offset-manager                 1          38             
group-offset-manager           offset-manager                 2          36 
```

#### Reset para offset específico (to-offset)

Move o offset de todas as partições de um tópico para um grupo de consumo (ou todos, depende do parâmetro) para o offset
específico informado como parâmetro da execução. No exemplo abaixo as partições do tópico *offset-manager*, para o grupo
de consumo *group-offset-manager*, são movidas para o offset 10.

```shell
# Comando
$ kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --reset-offsets --to-offset 10 \
--group group-offset-manager --topic offset-manager --execute

# Resultado
GROUP                          TOPIC                          PARTITION  NEW-OFFSET     
group-offset-manager           offset-manager                 0          10             
group-offset-manager           offset-manager                 1          10             
group-offset-manager           offset-manager                 2          10
```

#### Reset com offsets diferentes

Para ter um maior controle e possibilidade de mover o offset para valores diferentes em partições diferentes, pode 
ser usada a opção **from-file**, onde em um arquivo csv podem ser informados os offsets específicos para cada partição de 
um tópico. 

```shell
# Comando
$ kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --reset-offsets \
--from-file offsets.csv --group group-offset-manager --execute

# Conteudo do offsets.csv
# topico,particao,offset
offset-manager,0,5
offset-manager,1,15
offset-manager,2,25

# Resultado
GROUP                          TOPIC                          PARTITION  NEW-OFFSET     
group-offset-manager           offset-manager                 0          5              
group-offset-manager           offset-manager                 1          15             
group-offset-manager           offset-manager                 2          25 
```

#### Verificando os offsets das partiçoes

Para efeito de consulta e monitoraçao e possivel listar os offsets e lags de consumo dos grupos de consumidores em cada
partiçao dos topicos. Para isto deve-se utilizar a opção *--describe* do comando *kafka-consumer-groups*, conforme exemplo abaixo.

É possível também filtrar por grupo de consumo usando a opção *--group* informando o nome do grupo a ser exibido.

```shell
# Comando
$ kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --describe --all-groups

# Resultado
Consumer group 'group-offset-manager' has no active members.

GROUP                TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
group-offset-manager offset-manager  0          26              26              0               -               -               -
group-offset-manager offset-manager  1          38              38              0               -               -               -
group-offset-manager offset-manager  2          36              36              0               -               -               -
```

### Utilitário kafka-streams-application-reset

Além do *kafka-consumer-groups* existe também o utilitário *kafka-streams-application-reset* para reset de aplicações 
de stream. É útil quando se quer fazer o reprocessamento de uma aplicação de streams. 

Aplicações de stream têm algumas diferenças em relação a consumidores normais, então há uma diferença em relação a como 
o reset trata os vários tipos de tópicos.

- Input topics: Faz reset dos offsets para posição informada
- Intermediate topics: Move o offset para o fim (latest) de cada partição
- Internal topics: Remove o internal topic, automaticamente deletando qualquer offset comitado
- Output topics: Nada é feito com eles

É muito parecido com a ferramenta anterior mas diferente em alguns parâmetros. Aqui ao invés do group é informado o 
**--application-id*. A operação *--reset-offsets* também é dispensada, podendo ser informado diretamente a estratégia 
desejada. Estas opções são idênticas às da ferramenta de consumer groups, inclusive com a opção de passar arquivo csv.  

Para mais informações sobre os parâmetros, favor consultar a [documentação de referência do utilitário]
(https://kafka.apache.org/27/documentation/streams/developer-guide/app-reset-tool).

## Referências

### Artigos

- [API Consumer](https://kafka.apache.org/26/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html)
- [Operações de consumer lag](https://kafka.apache.org/documentation/#basic_ops_consumer_lag)
- [Operações de reset de Streams Application](https://kafka.apache.org/27/documentation/streams/developer-guide/app-reset-tool)

### Dicas para execução local

#### Criação de tópicos para execução local

```shell
docker-compose up -d
docker exec -it broker bash
kafka-topics --bootstrap-server 127.0.0.1:9092 --create --topic offset-manager --partitions 3
```

### Problemas que pode ser encontrados

#### Erro de no current assignment for partition

Antes de executar a operação de seek é necessário que as partições cujos offsets serão movidos estejam atribuídas ao
consumidor que vai fazer a execução.

```
Exception in thread "main" java.lang.IllegalStateException: No current assignment for partition offset-manager-0
	at org.apache.kafka.clients.consumer.internals.SubscriptionState.assignedState(SubscriptionState.java:367)
	at org.apache.kafka.clients.consumer.internals.SubscriptionState.lambda$requestOffsetReset$3(SubscriptionState.java:619)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
```