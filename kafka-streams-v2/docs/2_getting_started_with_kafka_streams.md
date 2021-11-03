# Chapter 2: Getting started with Kafka Streams

## The Kafka ecosystem

Without Kafka Streams, if you want to do anything nontrivial, like aggregate records, join separate
streams of data, group events into windowed time buckets, or run ad hoc queries against your stream, you will hit a wall of complexity pretty quickly.
The Producer and Consumer APIs do not contain any abstractions to help you with these kinds of use cases, so you will be left to your own devices as soon as it’s time to do something more advanced with your event stream.

- Kafka Stream API join Consumer, Producer and Connect API in the Kafka ecosystem

Kafka Streams operates at an exciting layer of the Kafka ecosystem: the place where data from many sources converges.
This is the layer where sophisticated data enrichment, transformation, and processing can happen.
It’s the same place where, in a pre–Kafka Streams world, we would have tediously written our own stream processing abstractions (using the Consumer/Producer API approach) or absorbed a complexity hit by using another framework.

## Features at a glance

- A high-level DSL that looks and feels like Java’s streaming API
    - The DSL provides a fluent and functional approach to processing data streams that is easy to learn and use
- A low-level Processor API that gives developers fine-grained control when they need it
- Convenient abstractions for modeling data as either streams or tables
- The ability to join streams and tables, which is useful for data transformation and enrichment
- Operators and utilities for building both stateless and stateful stream processing applications
- Support for time-based operations, including windowing and periodic functions
- Easy installation. It’s just a library, so you can add Kafka Streams to any Java application
- Scalability, reliability, maintainability

## Operational characteristicas

- **Scalability**
    - Since the unit of work in Kafka Streams is a single topic-partition, and since topics can be expanded by adding more partitions, the amount of work a Kafka Streams application can undertake can be scaled by increasing the number of partitions on the source topics
    - By leveraging consumer groups, the total amount of work being handled by a Kafka Streams application can be distributed across multiple, cooperating instances of your application
    - **However, regardless of how many application instances you end up deploying, Kafka Streams’ ability to cope with increased load by adding more partitions (units of work) and application instances (workers) makes Kafka Streams scalable**
- **Reliability**
    - Kafka Streams comes with a few fault-tolerant features, but the most obvious one is automatic failovers and partition rebalancing via consumer groups.
- **Maintainability**
    - Since Kafka Streams is a Java library, troubleshooting and fixing bugs is relatively straightforward since we’re working with standalone applications, and patterns for both troubleshooting and monitoring Java applications are well established and may already be in use at your organization (collecting and analyzing application logs, capturing application and JVM metrics, profiling and tracing, etc.)

## Processor topologies

**Kafka Streams leverages a programming paradigm called dataflow programming (DFP), which is a data-centric method of representing programs as a series of inputs, outputs, and processing stages.**

- Instead of building a program as a sequence of steps, the stream processing logic in a Kafka Streams application is structured as a directed acyclic graph (DAG)
- Three kind of processors
    - source processor
    - stream processor
    - sink processor
- **A collection of processors forms a processor topology, which is often referred to as simply the topology in both this book and the wider Kafka Streams community**
- Kafka Streams also has the notion of sub-topologies
- **Depth-First Processing**
    - Kafka Streams uses a depth-first strategy when processing data
    - When a new record is received, it is routed through each stream processor in the topology before another record is processed.
        - *This can be an issue when processing time is slow*

### Tasks and stream threads

- A task is the smallest unit of work that can be performed in parallel in a Kafka Streams application...
- Slightly simplified, the maximum parallelism at which your application may run is bounded by the maximum number of stream tasks, which itself is determined by the maximum number of partitions of the input topic(s) the application is reading from.
- Maximum number of threads is configurable
    - A thread can run a many tasks

## High-Level DSL Versus Low-Level Processor API

The high-level DSL is built on top of the Processor API, but the interface each expo‐
ses is slightly different. If you would like to build your stream processing application
using a functional style of programming, and would also like to leverage some
higher-level abstractions for working with your data (streams and tables), then the
DSL is for you.
On the other hand, if you need lower-level access to your data (e.g., access to record
metadata), the ability to schedule periodic functions, more granular access to your
application state, or more fine-grained control over the timing of certain operations,
then the Processor API is a better choice.

DSL example:

```java
class DslExample {
    public static void main(String[] args) {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<Void, String> stream = builder.stream("users");
        stream.foreach(
            (key, value) -> {
            System.out.println("(DSL) Hello, " + value);
        });
        // omitted for brevity
        Properties config = ...;
        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();
        // close Kafka Streams when the JVM shuts down (e.g., SIGTERM)
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
```

Processor API (low level) example:

```java
public class SayHelloProcessor implements Processor<Void, String, Void, Void> {
    @Override
    public void init(ProcessorContext<Void, Void> context) {}
    @Override
    public void process(Record<Void, String> record) {
        System.out.println("(Processor API) Hello, " + record.value());
    }
    @Override
    public void close() {}
}

class DslExample {
    public static void main(String[] args) {
        Topology topology = new Topology();
        topology.addSource("UserSource", "users");
        topology.addProcessor("SayHello", SayHelloProcessor::new, "UserSource");

        KafkaStreams streams = new KafkaStreams(topology, config);
        streams.start();
    }
}
```

## Streams and Tables

- There are two ways to model the data in your Kafka topics: as a stream (also called a record stream) or a table (also known as a changelog stream)
- **Streams**: These can be thought of as inserts in database parlance. Each distinct record remains in this view of the log
- **Tables**: Tables can be thought of as updates to a database
    - In this view of the logs, only the current state (either the latest record for a given key or some kind of aggregation) for each key is retained
    - Tables are usually built from compacted topics (i.e., topics that are configured with a *cleanup.policy* of compact, which tells Kafka that you only want to keep the latest representation of each key)
    - **Tables, by nature, are stateful, and are often used for performing aggregations in Kafka Streams**
    - Table representation: the table is materialized on the Kafka Streams side using a key-value store which, by default, is implemented using RocksDB

### KStream, KTable, GlobalKTable

- Those are abstraction of the high level API
- **KStream**: A KStream is an abstraction of a partitioned record stream, in which data is represented using insert semantics
- **KTable**: A KTable is an abstraction of a partitioned table (i.e., changelog stream), in which data is represented using update semantics (the latest representation of a given
  key is tracked by the application). Since KTable s are partitioned, each Kafka Streams task contains only a subset of the full table
- **GlobalKTable**: This is similar to a KTable , except each GlobalKTable contains a complete (i.e., unpartitioned) copy of the underlying data

