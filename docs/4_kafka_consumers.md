# 4. Kafka Consumers: Reading Data from Kafka

## Kafka consumers concepts

### Consumers and consumer groups

- kafka consumers are typically part of a consumer group
- When multiple consumers are subscribed to a topic and belong to the same consumer group, each consumer in the group will receive messages from a different subset of the partitions in the topic
- The main way we scale data consumption from a Kafka topic is by adding more consumers to a consumer group
- Keep in mind that there is no point in adding more consumers than you have partitions in a topic
    - some of the consumers will just be idle
- To make sure an application gets all the messages in a topic, ensure the application has its own consumer group

**To summarize, you create a new consumer group for each application that needs all the messages from one or more topics. 
You add consumers to an existing consumer group to scale the reading and processing of messages from the topics, so each additional consumer in a group will only get a subset of the messages.**

### Consumer groups and partition rebalance

- **Moving partition ownership from one consumer to another is called a rebalance**
- in normal course of events rebalance is fairly undesirable
- when do rebalance happens
    - add a new consumer to a group
    - remove a consumer from a group (shutdown or crashes)
    - topic a consumer group is consuming is modified (an admin add more partitions, for example)
- during a rebalance
    - consumers can’t consume messages, so a rebalance is basically a short window of unavailability of the entire consumer group
    - in addition, when partitions are moved from one consumer to another, the consumer loses its current state; if it was caching any
      data, it will need to refresh its caches—slowing down the application until the consumer sets up its state again 
- The way consumers maintain membership in a consumer group and ownership of the partitions assigned to them is by sending heartbeats to a Kafka broker designated as the group coordinator
- Heartbeats are sent when the consumer polls (i.e., retrieves records) and when it commits records it has consumed
- If the consumer stops sending heartbeats for long enough, its session will time out and the group coordinator will consider it dead and trigger a rebalance
- Heartbit
    - In release 0.10.1, the Kafka community introduced a separate heartbeat thread that will send heartbeats in between polls as well
    - This allows you to separate the heartbeat frequency (and therefore how long it takes for the consumer group to detect that a
  consumer crashed and is no longer sending heartbeats) from the frequency of polling (which is determined by the time it takes to process the data returned from the brokers)

#### Assigning partitions process

1. When a consumer wants to join a group, it sends a JoinGroup request to the group coordinator
    - 1.2. The first consumer to join the group becomes the group leader
2. The leader receives a list of all consumers in the group from the group coordinator (this will include all consumers that sent a heartbeat recently and which are therefore considered alive) and is responsible for assigning a subset of partitions to each consumer
3. It uses an implementation of PartitionAssignor to decide which partitions should be handled by which consumer
    - 3.1 Kafka has two built-in partition assignment policies
4. After deciding on the partition assignment, the consumer leader sends the list of assignments to the GroupCoordinator, which sends this information to all the consumers
5. Each consumer only sees his own assignment
    - 5.1. the leader is the only client process that has the full list of consumers in the group and their assignments
6. This process repeats every time a rebalance happens.

## Creating a kafka consumer

- create properties and there are three mandatory
    - **bootstrap.servers**
    - **key.deserializer**
    - **value.deserializer**
- **group.id**: group a consumer is part of

## Subscribing to topics

- subscribe to consume messages from one or more topics
    - one topic
    - several topics
    - regular expression

## The poll loop

- poll loop handles details of coordination, partition rebalances, heartbeats, and data fetching, leaving the developer with a clean API that simply returns available data from the assigned partitions
- The parameter we pass, poll(), is a timeout interval and controls how long poll() will block if data is not available in the consumer buffer
- **Always close() the consumer before exiting. This will close the network connections and sockets. It will also trigger a rebalance immediately rather than wait for
  the group coordinator to discover that the consumer stopped sending heartbeats and is likely dead, which will take longer and therefore result in a longer period
  of time in which consumers can’t consume messages from a subset of the partitions**
- The first time you call poll() with a new consumer, it is responsible for finding the GroupCoordinator , joining the consumer group, and receiving a partition assignment

```java
try {
    while (true) {
        ConsumerRecords<String, String> records = consumer.poll(100);
        for (ConsumerRecord<String, String> record : records)
        {
            log.debug("topic = %s, partition = %s, offset = %d,
            customer = %s, country = %s\n",
            record.topic(), record.partition(), record.offset(),
            record.key(), record.value());
            int updatedCount = 1;
            if (custCountryMap.countainsValue(record.value())) {
            updatedCount = custCountryMap.get(record.value()) + 1;
        }
        custCountryMap.put(record.value(), updatedCount)
        JSONObject json = new JSONObject(custCountryMap);
        System.out.println(json.toString(4))
        }
    }
} finally {
    consumer.close();
}
```

### Thread Safety

You can’t have multiple consumers that belong to the same group in one thread and you can’t have multiple threads safely use the
same consumer. One consumer per thread is the rule. To run multiple consumers in the same group in one application, you will
need to run each in its own thread. It is useful to wrap the consumer logic in its own object and then use Java’s ExecutorService
to start multiple threads each with its own consumer.

## Configuring consumers

- **fetch.min.bytes**: This property allows a consumer to specify the minimum amount of data that it wants to receive from the broker when fetching records
- **fetch.max.wait.ms**: By setting fetch.min.bytes, you tell Kafka to wait until it has enough data to send before responding to the consumer. 
    - fetch.max.wait.ms lets you control how long to wait
- **max.partition.fetch.bytes**: This property controls the maximum number of bytes the server will return per partition
    - one consumer must have memory enough to fit this number multiplied by the number of partitions it is assigned to  
- **session.timeout.ms**: The amount of time a consumer can be out of contact with the brokers while still considered alive defaults to 3 seconds
- **heartbeat.interval.ms**: controls how frequently the KafkaConsumer poll() method will send a heartbeat to the
  group coordinator, whereas session.timeout.ms controls how long a consumer can go without sending a heartbeat
- **auto.offset.reset**: controls the behavior of the consumer when it starts reading a partition for which it doesn't have a committed offset or if the committed offset it has is invalid (usually because the consumer was down for so long that the record with that offset
  was already aged out of the broker)
    - latest: starts consuming from the newest values 
    - earliest: starts consuming from the oldest values
- **enable.auto.commit**: controls whether the consumer will commit offsets automatically, and defaults to true
    - If you set enable.auto.commit to true, then you might also want to control how frequently offsets will be committed using **auto.commit.interval.ms**
- **partition.assignment.strategy**: strategy to assign partitions to consumers
    - Range (org.apache.kafka.clients.consumer.RangeAssignor): Assigns to each consumer a consecutive subset of partitions from each topic it subscribes to
    - RoundRobin (org.apache.kafka.clients.consumer.RoundRobinAssignor): Takes all the partitions from all subscribed topics and assigns them to consumers sequentially, one by one 
    - A custom strategy can be implemented too
- **client.id**: this will be used by the brokers to identify messages sent from the client
    - It is used in logging and metrics, and for quotas
- **max.poll.records**: This controls the maximum number of records that a single call to poll() will return
- **receive.buffer.bytes and send.buffer.bytes**: These are the sizes of the TCP send and receive buffers used by the sockets when writing and reading data

## Commits and offsets

- **commit**: the action of updating the current position a consumer in a partition
    - it produces a message to Kafka, to a special **__consumer_offset** topic
- this is a consumer should start consuming messages from a partition, regardless of when is the previous one or not
- Important:
    - If the committed offset is smaller than the offset of the last message the client pro‐
      cessed, the messages between the last processed offset and the committed offset will
      be processed twice
    - If the committed offset is larger than the offset of the last message the client actually
      processed, all messages between the last processed offset and the committed offset
      will be missed by the consumer group

### Automatic commit

- The easiest way to commit offsets is to allow the consumer to do it for you
- If you configure **enable.auto.commit=true**, then every five seconds the consumer will
  commit the largest offset your client received from poll()
- The five-second interval is the default and is controlled by setting **auto.commit.interval.ms**
- With autocommit enabled, a call to poll will always commit the last offset returned by
  the previous poll
    - It doesn’t know which events were actually processed, so it is critical
  to always process all the events returned by poll() before calling poll() again

### Commit current offset

- By setting auto.commit.offset=false , offsets will only be committed when the
  application explicitly chooses to do so
- The simplest and most reliable of the commit APIs is commitSync()
- Drawback is that application is blocked until the broker responds to the commit request

```java
while (true) {
    ConsumerRecords<String, String> records = consumer.poll(100);
    for (ConsumerRecord<String, String> record : records) {
        System.out.printf("topic = %s, partition = %s, offset =
            %d, customer = %s, country = %s\n",
            record.topic(), record.partition(), record.offset(), record.key(), record.value());
    }
    try {
        consumer.commitSync();
    } catch (CommitFailedException e) {
        log.error("commit failed", e)
    }
}
```

### Asynchronous commit

- Drawback is that commit async does not retry. If it fails, it is lost
- It is possible to pass a callback handler but we should be careful to retry commit async with callback handler because another subsequent commit could be successful and so we can have an ordering problem
- 

```java
while (true) {
    ConsumerRecords<String, String> records = consumer.poll(100);
    for (ConsumerRecord<String, String> record : records) {
        System.out.printf("topic = %s, partition = %s, offset = %d, customer = %s, country = %s\n", 
            record.topic(), record.partition(), record.offset(), record.key(), record.value());
    }
    consumer.commitAsync();
}
```

#### Retrying Async Commits

A simple pattern to get commit order right for asynchronous
retries is to use a monotonically increasing sequence number.
Increase the sequence number every time you commit and add the
sequence number at the time of the commit to the commitAsync
callback. When you’re getting ready to send a retry, check if the
commit sequence number the callback got is equal to the instance
variable; if it is, there was no newer commit and it is safe to retry. If
the instance sequence number is higher, don’t retry because a
newer commit was already sent.

### Combining synchronous and asynchronous commits

- use async commit but when closing the consumer use a sync one to make sure every message has its offset committed

```java
try {
  while (true) {
    ConsumerRecords<String, String> records = consumer.poll(100);
    for (ConsumerRecord<String, String> record : records) {
      System.out.printf("topic = %s, partition = %s, offset = %d,
      customer = %s, country = %s\n",
      record.topic(), record.partition(),
      record.offset(), record.key(), record.value());
    }
    consumer.commitAsync();
  }
} catch (Exception e) {
  log.error("Unexpected error", e);
} finally {
  try {
    consumer.commitSync();
  } finally {
    consumer.close();
  }
}
```

### Commit specified offsets

- It's possible to commit in the middle of the batch to avoid having to process all those rows again if a rebalance occurs
- There is a method signature where we can pass a list of offsets and partitions to commit 

```java
private Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
int count = 0;
        
while (true) {
  ConsumerRecords<String, String> records = consumer.poll(100);
  for (ConsumerRecord<String, String> record : records) {
    System.out.printf("topic = %s, partition = %s, offset = %d,
        customer = %s, country = %s\n",
        record.topic(), record.partition(), record.offset(),
        record.key(), record.value());
    currentOffsets.put(new TopicPartition(record.topic(), record.partition()), new
        OffsetAndMetadata(record.offset()+1, "no metadata"));
    if (count % 1000 == 0)
        consumer.commitAsync(currentOffsets, null);
    count++;
  }
}
```

## Rebalance listeners

Some activities you may want to be before losing ownership of a partition in a rebalance:

- commit offsets of the last event you’ve processed
- if your consumer maintained a buffer with events that it only processes occasionally you will want to process the events
you accumulated before losing ownership of the partition
- perhaps you also need to close file handles, database connections, and such

The consumer API allows you to run your own code when partitions are added or
removed from the consumer

- pass a ConsumerRebalanceListener when calling the subscribe()
    - **public void onPartitionsRevoked(Collection<TopicPartition> partitions)**: called before the rebalancing starts and after the consumer stopped consuming messages
        - this is where you want to commit offsets, so whoever gets this partition next will know where to start
    - **public void onPartitionsAssigned(Collection<TopicPartition> partitions)**

## Consuming records with specific offsets

- **seekToBeginning(TopicPartition tp)**: starts consuming at beginning
- **seekToEnd(TopicPartition tp)**: starts consuming at the end
- **seek()**: starts consuming at a specific position 

```java
public class SaveOffsetsOnRebalance implements ConsumerRebalanceListener {
  public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
    commitDBTransaction();
  }
  public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
    for(TopicPartition partition: partitions)
        consumer.seek(partition, getOffsetFromDB(partition));
  }
}

consumer.subscribe(topics, new SaveOffsetOnRebalance(consumer));
consumer.poll(0);
for (TopicPartition partition: consumer.assignment())
    consumer.seek(partition, getOffsetFromDB(partition));

while (true) {
    ConsumerRecords<String, String> records = consumer.poll(100);
    for (ConsumerRecord<String, String> record : records) {
        processRecord(record);
        storeRecordInDB(record);
        storeOffsetInDB(record.topic(), record.partition(), record.offset());
    }
    commitDBTransaction();
}
```

## But how do we exit?

## Deserializers

- Kafka consumers require deserializers to convert byte arrays recieved from Kafka into Java objects
- It should be obvious that the serializer used to produce events to Kafka must match the deserializer that will be used when consuming event

### Custom deserializer

```java
import org.apache.kafka.common.errors.SerializationException;
import java.nio.ByteBuffer;
import java.util.Map;

public class CustomerDeserializer implements  Deserializer<Customer> {

  @Override
  public void configure(Map configs, boolean isKey) {
  // nothing to configure
  }
  
  @Override
  public Customer deserialize(String topic, byte[] data) {
    int id;
    int nameSize;
    String name;
    
    try {
        if (data == null)
            return null;
        if (data.length < 8)
            throw new SerializationException("Size of data received by
        IntegerDeserializer is shorter than expected");
        ByteBuffer buffer = ByteBuffer.wrap(data);
        id = buffer.getInt();
        String nameSize = buffer.getInt();
        byte[] nameBytes = new Array[Byte](nameSize);
        buffer.get(nameBytes);
        name = new String(nameBytes, 'UTF-8');
        return new Customer(id, name);
    } catch (Exception e) {
        throw new SerializationException("Error when serializing Customer to byte[] " + e);
    }
  }
  
  @Override
  public void close() {
      // nothing to close
  }
}
```

### Using Avro deserialization with Kafka consumer

```java
Properties props = new Properties();
props.put("bootstrap.servers", "broker1:9092,broker2:9092");
props.put("group.id", "CountryCounter");
props.put("key.serializer", "org.apache.kafka.common.serialization.StringDeserializer");
props.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
props.put("schema.registry.url", schemaUrl);

String topic = "customerContacts";

KafkaConsumer consumer = new  KafkaConsumer(createConsumerConfig(brokers, groupId, url));
consumer.subscribe(Collections.singletonList(topic));
System.out.println("Reading topic:" + topic);

while (true) {
    ConsumerRecords<String, Customer> records = consumer.poll(1000);
    for (ConsumerRecord<String, Customer> record: records) {
        System.out.println("Current customer name is: " +
        record.value().getName());
    }
    consumer.commitSync();
}
```

## Standalone consumer: Why and How to Use a Consumer Without a Group

- When you know exactly which partitions the consumer should read, you don’t subscribe to a topic—instead, you assign yourself a few partitions
- A consumer can either subscribe to topics (and be part of a consumer group), or assign itself partitions, but not both at the same time
