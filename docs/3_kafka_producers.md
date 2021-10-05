# 3. Kafka producers - writing messages to Kafka

Reference in the book: Pages 41 to 63. 

- apache kafka ships with built-in api's that clients use to connect with kafka
- In addition to the built-in clients, Kafka has a binary wire protocol
    * This means that it is possible for applications to read messages from Kafka or write messages to Kafka simply by sending the correct byte sequences to Kafka’s network port.

## Producer overview

- the different producer requirements will guide the way producers write messages to kafka and configure its use
    * is every message critical, or can we tolerate loss of messages? 
    * Are we OK with accidentally duplicating messages?
    * Are there any strict latency or throughput requirements we need to support? 
- **producer record**
    - **topic**
    - **partition (optional)**
    - **key (optional)**
    - **value**
- producing
    - creates a producer record
    - serializes the record to bytes array
    - data is sent to a partitioner (remember that if a partition is specified in the record the partitioner only returns it)
    - adds the record to a batch of messages to topic and partition
    - **a separate thread is responsible for sending those batches to appropriate kafka brokers**
    - the broker sends the response
        - RecordMetadata if there is success with topic, partition and offset of written data
        - Returns an error and the producer can decide if it retries or not

## Constructing a kafka producer

The first step to send messages to kafka is to create a producer. There are three mandatory properties to be configured:

- **bootstrap.servers**: list of pair *host:port* the producer will establish initial connection to the cluster
    - Id doesn't need to include all brokers, but it's recommended to include more than one in case some broker is down
- **key.serializer**: Name of a class that will be used to serialize the keys of the records we will produce to Kafka
    - kafka only knows byte arrays
    - mandatory even when the producer will only send values, not keys
- **value.serializer**: Name of a class that will be used to serialize the values of the records we will produce to Kafka 

There are three primary methods of sending a message to kafka:

- **fire and forget**: We send a message to the server and don’t really care if it arrives succesfully or not
- **synchronous send**: We send a message, the send() method returns a Future object, and we use get() to wait on the future and see if the send() was successful or not
- **asynchronous send**: We call the send() method with a callback function, which gets triggered when it receives a response from the Kafka broker
 
## Sending a message to kafka

### Sending a message synchronously

- **Future.get()** method waits synchronously until message is sent
- an error can be thrown
- the producer can retry (at a maximum amount of times) if is a recoverable error
    - returns an error if the error was not recoverable

```java
        final var record = new ProducerRecord<>(topic, key, value);
        try {
            // get() method waits synchronously until the message is sent or an error is risen
            this.producer.send(record).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
```

### Sending a message asynchronously

- async messages are sent when we use a callback handler when sending a record
- a callback handler should implement the *org.apache.kafka.clients.producer.Callback* interface, which has a single function *onCompletion()*
- callback method will receive a non-null exception when an error occurs

```java
    public void producer(final String topic, final String key, final String value) {
        final var record = new ProducerRecord<>(topic, key, value);
        // callback handler will handle response when message is sent or an error is thrown
        this.producer.send(record, new DemoProducerCallback());
    }

    private class DemoProducerCallback implements Callback {
        @Override
        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
            if (e != null) {
                e.printStackTrace();
            } else {
                System.out.println("Message send to topic " + recordMetadata.topic() + " and partition " + recordMetadata.partition() +
                        " with offset " + recordMetadata.offset());
            }
        }
    };
```

## Configuring producers

- **acks**: control how many replicas should receive the record before the producer consider it written
    - 0: producer does not wait confirmation from any replica
        - messages can be lost and the producer will not be notified
        - **can be used to achieve high throughput**
    - 1: producer waits only the confirmation from the leader replica
        - it should use asynchronous message to achieve higher throughput
        - throughput limited by the inflight requests limit
    - all: producer will receive the confirmation from the broker since all **in-sync** replicas receive the message
        - safer mode, for sure, but the latency is even bigger that acks=1
- **buffer.memory**: amount of memory producer will use to keep messages in memory before sending them to the cluster
    - a small value can be a problem if the application produces messages so fast
- **compression.type**: compress data before sending it to the broker
    - default is uncompressed 
    - possible values are snappy, gzip or lz4
    - by enabling it reduce network utilization and storage
- **retries**: it controls how many times the producer will retry sending the message before giving up and notifying the client of an issue when an error was received from the broker
    - that happens when error is transient, or recoverable (a too large message, for example, is not recoverable)
    - the parameter **retry.backoff.ms** defines how long the producer will wait until the next retry
- **batch.size**: it controls the amount of memory in bytes (not messages) that will be used for each batch
    - setting the batch too small adds overhead to the cluster since the producer will send messages very often
- **linger.ms**: controls the amount of time to wait for additional messages before sending the current batch
- **client.id**: This can be any string, and will be used by the brokers to identify messages sent from the client
    - It is used in logging and metrics, and for quotas
- **max.in.flight.requests.per.connection**: This controls how many messages the producer will send to the server without receiving responses
    - Setting this high can increase memory usage while improving throughput, but setting it too high can reduce throughput as batching becomes less efficient
    - Setting this to 1 will guarantee that messages will be written to the broker in the order in which they were sent, even when retries occur
- **timeout.ms, request.timeout.ms, and metadata.fetch.timeout.ms**: 
   - These parameters control how long the producer will wait for a reply from the server when sending data ( request.timeout.ms ) and when requesting metadata such as the
  current leaders for the partitions we are writing to (metadata.fetch.timeout.ms)
- **max.block.ms**: This parameter controls how long the producer will block when calling send() and when explicitly requesting metadata via partitionsFor()
- **max.request.size**: This setting controls the size of a produce request sent by the producer
    - It caps both the size of the largest message that can be sent and the number of messages that the producer can send in one request
- **receive.buffer.bytes and send.buffer.bytes**: These are the sizes of the TCP send and receive buffers used by the sockets when writing and reading data

## Serializers

### Custom serializers

- avro, protobuf and thrift are generic options and one can implement its own at all
- To write your own custom serializer you should implement the interface **org.apache.kafka.common.serialization**
    - the interface method **byte[] serialize(String var1, T var2);** is required but there are three default that can be overriden 

### Serializing using Apache Avro 

- Apache Avro is a language-neutral data serialization format
- The schema is usually described in JSON and the serialization is usually to binary files, although serializing to JSON is also supported
- It normally embeds the schema in the files themselves
- Why is Avro a good fit for evolving data? 
    - When the application that is writing messages switches to a new schema, the applications reading the data can continue processing messages without requiring any change or update.
    - The fields that does not exist any longer will just return null
- Even though we changed the schema in the messages without changing all the applications reading the data, there will be no exceptions or breaking errors and no need for expensive updates of exist ing data

### Using Avro records with kafka

- better them embedding the schema in every record message is to use some structure like schema registry, where you just add the reference to the schema in the registry
- 

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("key.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
props.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
props.put("schema.registry.url", schemaUrl);
    
String topic = "customerContacts";
int wait = 500;

Producer<String, Customer> producer = new KafkaProducer<String, Customer>(props);

// We keep producing new events until someone ctrl-c
while (true) {
    Customer customer = CustomerGenerator.getNext();
    System.out.println("Generated customer " + customer.toString());
    ProducerRecord<String, Customer> record = new ProducerRecord<>(topic, customer.getId(), cusstomer);
    producer.send(record);
}
```

## Partitions

- all messages with the same key are written to the same partition
- When the key is null and the default partitioner is used, the record will be sent to one of the available partitions of the topic at random
- A round-robin algorithm will be used to balance the messages among the partitions.
- If a key exists and the default partitioner is used, Kafka will hash the key, and use the result to map the message to a specific partition
- The mapping of keys to partitions is consistent only as long as the number of partitions in a topic does not change
- When partitioning keys is important, the easiest solution is to create topics with sufficient partitions and never add partitions

### Custom partitioning strategy

```java
import io.confluent.common.utils.Utils;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.InvalidRecordException;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;

public class BananaPartitioner implements Partitioner {

    public void configure(Map<String, ?> configs) {}

    public int partition(String topic, Object key, byte[] keyBytes,
                         Object value, byte[] valueBytes, Cluster cluster) {
        final List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int numPartitions = partitions.size();
        if ((keyBytes == null) || (!(key instanceof String))) {
            throw new InvalidRecordException("We expect all messages to have customer name as key");
        }
        if (((String) key).equals("Banana"))
            return numPartitions; // Banana will always go to last partition

        // Other records will get hashed to the rest of the partitions
        return (Math.abs(Utils.murmur2(keyBytes)) % (numPartitions - 1))
    }
    
    public void close() {}
}
```

## Old producer apis
