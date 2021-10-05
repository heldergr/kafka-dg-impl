# Meet Kafka

- data is produced and should move to where it is supposed to be used
    * that should be done faster

## Publish/subscribe messaging

- a sender (publisher) sends pieces of data not specifically to receiver
- a receiver (subscriber) subscribes to certain classes of messages
- the pattern normally includes a broker

## Enter Kafka

- publish/subscribe messaging system
- also called as
    * distributed commit log
    * distributed streaming platform

### Messages and batches

- a single unit of data is called a message
- messages and keys are only arrays of bytes for Kafka
- messages can be grouped in batches to reduce network traffic
    * they can be compressed too

### Schema

- tells data structure and data types, instead of JSON and XML that don't specify data types
- Apache Avro is one example

### Topics and partitions

- messages are categorized in topics, which are broken into partitions
- there is guarantee of ordering in messages inside a partition
- partitions are a key to redundancy and scalability, since they allow topics to be scaled horizontally across many servers

### Producers and consumers

- Kafka clients are users of the systems and normally have two types: producers and consumers
- Producers
    * publishers
    * all messages with the same key are directed to the same partition
- consumers
    * read messages (subscribers)
    * consumers work as part of a consumer group that where one or more consumer work together to consume a topic

### Brokers and clusters

- **broker** is a single kafka server
- it receives messages from producers and responds to consumers fetch messages
- brokers can be part of a cluster
    * one of the brokers is the **controller**
        * administrative operations
        * assign partitions to brokers
        * monitoring broker failures
    * a partition is owned by a single broker, which is the **leader**, but it can be replicated to other brokers depending on the repl factor
    * all consumers and producers connect to the leader
- a key feature is a **retention**, which determines for how long or how much of data should be kept in disk
    * there is also a log compacted topic, which means Kafka will only maintain the last messages produced to a topic

## Why Kafka?

- multiple producers
- multiple consumers, which can be part of a group to consume to same stream or be in different groups
- disk retention
    * with disk retention rules
    * it can be done in a per topic basis
- flexible scalability
    * expansions (include more brokers) can be done with the cluster online, no impact on availability of systems
    * multiple brokers can handle the failure of a single broker
- high performance

## The Data Ecosystem

### Use cases

- activity tracking: user activity can be converted into message and published in topics to be consumed
- messaging
- metrics and logging: one benefit of kafka is that when the destination system needs to change there is no need to alter the frontend applications or the mean of aggregation
- commit log: durable retention is useful here for providing a buffer for the changelog, meaning it can be replayed in the event of a failure of the consuming
  applications
- stream processing

## Kafka's Origin
