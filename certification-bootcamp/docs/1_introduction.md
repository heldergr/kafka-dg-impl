# Introduction

- [Confluent - What is apache kafka](#confluent_what_is_apache_kafka)
- [Apacha kafka - intro how kafka works](#apacha_kafka_intro_how_kafka_works)

In this module, we’ll look at instruction that addresses specific areas of Confluent and Kafka development. While these areas are called out in the certification exam, you’ll find them to be critical for success in the Kafka and Confluent developer role. This isn’t an exhaustive list, but it will give you an understanding of where to start and what to research for a further deep dive.
Conceptual Knowledge of:

Topics, brokers, producers, consumers, schema, clusters…​ This is the foundation upon which everything else you learn will be built. Make sure that you fully understand the basics of Kafka before moving on to anything else.

:ok: - One of the best places to start with this is this video from Confluent’s Tim Berglund: https://www.confluent.io/what-is-apache-kafka/

More useful study materials can be found here:

    :ok: https://www.confluent.io/blog/apache-kafka-intro-how-kafka-works/

    :ok: https://kafka.apache.org/intro

    :ok: https://dzone.com/articles/what-is-kafka

    Kafka summit presentation, left for watching if I have time...   
    https://www.confluent.io/resources/kafka-summit-2020/welcome-to-kafka-were-glad-youre-here/

    https://www.confluent.io/online-talks/fundamentals-for-apache-kafka/

    https://kafka.apache.org/videos#kafka_internals_fundamentals

    https://www.confluent.io/blog/event-streaming-platform-1/

## Confluent - What is apache kafka

- Reference: <https://www.confluent.io/what-is-apache-kafka/>

How can kafka help you?

- publish/subscribe
- store
    - durable storage, source of truth, distributed 
- process
    - the Streams API within Apache Kafka is a powerful, lightweight library that allows for on-the-fly processing, letting you aggregate, create windowing parameters, perform joins of data within a stream, and more 

## Apacha kafka - intro how kafka works

- Reference: <https://www.confluent.io/blog/apache-kafka-intro-how-kafka-works/>

### Events

- **an event is just a thing that has happened**
- combination of
   - notification (when)
   - state
- modeled a key/value pair and it's represented innerly as an array of bytes
    - key in the kafka world is not unique like in database tables 

### Brokers

- computer, instance, of container running the kafka process
- manage partitions
- handle write and read requests
- manage replication of partitions
- intentionally very simple

### Replication

- brokers can fail and partitions can be replicated
- leander replica (one per partition)
- follower replicas (rep factor - 1)
- in general write and read happen to the leader
- if one node dies there are replicas to do the work

### kafka producers

### kafka consumers

### the kafka ecosystem

### kafka connect

- way to connect non-kafka systems to kafka in a declarative way without requiring you to write a bunch of integration code
- ecosystem of pluggable connectors
- abstract a lot of integration code from the sources/sinks
- horizontally scalable
- fault tolerant

connectors

- pluggable software component
- interfaces to external system and to kafka
- also exist as runtime entities
- source connectors acts as producers
- sink connectors acts as consumers
    - for the cluster is only producers or consumers

### schema registry

- schema change is a constant fact of life

schema registry

- server process external do kafka brokers
- maintain a database of schemas
    - which is stored in an internal kafka topic
- HA deployment option available
- also a consumer/producer api component
    - listen to schemas
- defines schema compatibility rules per topic
- producer api prevents incompatible messages from being produced
- consumer api prevents incompatible messages from being consumed

supported formats

- JSON schema
- Avro
- Protocol buffers

### kafka streams

- prevent consumers to get so complex

kafka streams

- Java functional api
- easy access to all computational primitives of streams
- filtering, grouping, aggregating, joining, and more
    - built in top of consumers api
- scalable, fault-tolerant state management
- scalable computation based on consumer groups
- integrates with other frameworks like spring boot, micronault
- kafka streams is a library, not a separate piece of infrastructure
    - runs in the context of your application
- Does not require special infrastructure

### ksqldb

- alternative for non-java applications
- **it is a database optimized for stream processing**
- it runs on its own scalable, fault-tolerant cluster adjacent to the kafka cluster
- stream processing programs written in sql
    - it does not matter which programming language your application is 
- simpler approach to just use SQL
- provides a command line interface
- make tables queryable through a lightweight JSON API
    - rest api for application integration
- provides a java library to integrate with the stream in ksqldb
- integration with kafka connect
- not intended to replace traditional relational databases even its possible

## Introduction

- Reference: <https://kafka.apache.org/intro>

- topic: ordered collection of events stored (for hours, or days, or years...) in disk

Kafka APIs

In addition to command line tooling for management and administration tasks, Kafka has five core APIs for Java and Scala:

- The **Admin API** to manage and inspect topics, brokers, and other Kafka objects.
- The **Producer API** to publish (write) a stream of events to one or more Kafka topics.
- The **Consumer API** to subscribe to (read) one or more topics and to process the stream of events produced to them.
- The **Kafka Streams API** to implement stream processing applications and microservices. It provides higher-level functions to process event streams, including transformations, stateful operations like aggregations and joins, windowing, processing based on event-time, and more. Input is read from one or more topics in order to generate output to one or more topics, effectively transforming the input streams to output streams.
- The **Kafka Connect API** to build and run reusable data import/export connectors that consume (read) or produce (write) streams of events from and to external systems and applications so they can integrate with Kafka. For example, a connector to a relational database like PostgreSQL might capture every change to a set of tables. However, in practice, you typically don't need to implement your own connectors because the Kafka community already provides hundreds of ready-to-use connectors.

## What is Kafka 

- Reference: <https://dzone.com/articles/what-is-kafka>

### Why Is Kafka So Popular?

Kafka has operational simplicity. Kafka is easy to set up and use, and it is easy to figure out how Kafka works. However, the main reason Kafka is very popular is its excellent performance. It is stable, provides reliable durability, has a flexible publish-subscribe/queue that scales well with N-number of consumer groups, has robust replication, provides producers with tunable consistency guarantees, and it provides preserved ordering at the shard level (i.e. Kafka topic partition).

### Why Is Kafka so Fast?

Kafka relies heavily on the OS kernel to move data around quickly. It relies on the principals of zero copy. Kafka enables you to batch data records into chunks. These batches of data can be seen end-to-end from producer to file system (Kafka topic log) to the consumer.

Batching allows for more efficient data compression and reduces I/O latency. Kafka writes to the immutable commit log to the disk sequential, thus avoiding random disk access and slow disk seeking. Kafka provides horizontal scale through sharding. It shards a topic log into hundreds (potentially thousands) of partitions to thousands of servers. This sharding allows Kafka to handle massive load.


