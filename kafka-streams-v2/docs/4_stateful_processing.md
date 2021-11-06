# Chapter 4 - Stateful processing

## Benefits of stateful processing 

Stateful processing helps us understand the relationships between events and leverage these relationships for more advanced stream processing use cases.
When we are able to understand how an event relates to other events, we can:
- Recognize patterns and behaviors in our event streams
- Perform aggregations
- Enrich data in more sophisticated ways using joins

Another benefit of stateful stream processing is that it gives us an additional abstraction for representing data.
By replaying an event stream one event at a time, and saving the latest state of each key in an embedded key-value store, we can build a point-in-time representation of continuous and unbounded record streams, which we call **tables**.

- **Stateless: fact driven**
- **Stateful: behavior driven** (the accumulation of facts captures behavior)

## Preview of stateful operations

| Use case | Purpose | Operators |
| -------- | ------- | --------- |
| Joining data | Enrich an event with additional information or context that was captured in a separate stream or table | **join, leftJoin, outerJoin** |
| Aggregating data | Compute a continuously updating mathematical or combinatorial transformation of related events | **aggregate, count, reduce** |
| Windowing data | Group events that have close temporal proximity | **windowedBy** |

Compared to the stateless operators we encountered in the previous chapter, stateful operators are more complex under the hood and **have additional compute and storage requirements**.

## State stores

To support stateful operations, we need a way of storing and retrieving the remembered data, or state, required by 
each stateful operator in our application (e.g., count , aggregate , join , etc.). 

**The storage abstraction that addresses these needs in Kafka Streams is called a state store**, and since a single Kafka 
Streams application can leverage many stateful operators, a single application may contain several state stores.

### Common characteristics

#### Embedded

- The default state store implementations that are included in Kafka Streams are embedded within your Kafka Streams application at the task level
    - No need for network calls or worrying about availability
- All of the default state stores leverage RocksDB under the hood

#### Multiple access modes

- State store should be changed (write access) only from the topology, external interested must have read only access 

#### Fault tolerant

By default, state stores are backed by changelog topics in Kafka. 4 In the event of failure, state stores can be restored by replaying the individual events from the underlying changelog topic to reconstruct the state of an application.

#### key-based

Operations that leverage state stores are key-based. A record’s key defines the relationship between the current event and other events.

### Persistent versus in-memory store

- Benefits of persistent store
    - State can exceed the size of available memory
    - In the event of failure, persistent stores can be restored quicker than in-memory stores
- Downside
    - State stores are operationally more complex and can be slower than a pure in-memory store, which always pulls data from RAM 

## Data models

## Adding the source processors

### KStream

- Always changing 
- We are interested in all values, not only the last one
- It is also very common to use stateless KStreams alongside a KTable or GlobalKTable when the mutable table semantics aren’t needed for one or more data sources

### KTable

- we are only interested in the latest state of a data 

**One important thing to look at when deciding between using a KTable or GlobalKTable is the keyspace.
If the keyspace is very large (i.e., has high cardinality/lots of unique keys), or is expected to grow into a very large keyspace, then it makes more sense to use a KTable so that you can distribute fragments of the entire state across all of your running application instances.
By partitioning the state in this way, we can lower the local storage overhead for each individual Kafka Streams instance.**

**Perhaps a more important consideration when choosing between a KTable or GlobalKTable is whether or not you need time synchronized processing.**

### GlobalKTable

**A GlobalKTable should be used when your keyspace is small, you want to avoid the co-partitioning requirements of a join, and when time synchronization is not needed.**

## Registering streams and tables

```java
public class BoardGame {
    public static void main(String[] args) {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<byte[], ScoreEvent> scoreEvents =
                builder.stream(
                        "score-events",
                        Consumed.with(Serdes.ByteArray(), JsonSerdes.ScoreEvent()));
        KTable<String, Player> players =
                builder.table(
                        "players",
                        Consumed.with(Serdes.String(), JsonSerdes.Player()));
        GlobalKTable<String, Product> products =
                builder.globalTable(
                        "products",
                        Consumed.with(Serdes.String(), JsonSerdes.Product()));    
    }
}
```

## Joins

Joins, however, can be thought of as a special kind of conditional merge that cares about the relationship between events, and where the records are not copied verbatim into the output stream but rather combined.

- Join operators
    - **join**: inner
    - **leftJoin**
    - **outerJoin**

### Join types

| type | windowed | operators | Co-partitioning required |
| ---- | -------- | --------- | ------------------------ |
| KStream-KStream | **Yes** | join, leftJoin, outerJoin | Yes |
| KTable-KTable | No | join, leftJoin, outerJoin | Yes |
| KStream-KTable | No | join, leftJoin | Yes |
| KStream-GlobalKTable | No | join, leftJoin | **No** |

### Co-Partitioning

**In order to understand the relationship between events (through joins) or compute aggregations on a sequence of events, we need to ensure that related events are routed to the same partition, and so are handled by the same task.**

To ensure related events are routed to the same partition, we must ensure the following co-partitioning requirements are met:
- Records on both sides must be keyed by the same field, and must be partitioned on that key using the same partitioning strategy.
- The input topics on both sides of the join must contain the same number of partitions
    - This is the one requirement that is checked at startup. If this requirement is not met, then a TopologyBuilderException will be thrown

Other notes:
- **selectKey** is a function we can use when we need to rekey records.
- **co-partitioning is not required for GlobalKTable joins since the state is fully replicated across each instance of our Kafka Streams app**

### ValueJoiner

**In Kafka Streams, we need to use a ValueJoiner to specify how different records should be combined. A ValueJoiner simply takes each record that is involved in the join, and produces a new, combined record.**

## Grouping records

- Grouping streams
    - groupBy
    - groupByKey
- If your records don’t need to be rekeyed, then it is preferable to use the groupByKey operator instead. groupByKey will not mark the stream for repartitioning
- Grouping tables
    - groupBy 

## Aggregations

- Some common operators
    - aggregate
    - reduce
    - count

### Aggregate streams

#### Initializer

- Used when a new key is seen

```java
Initializer<Long> countInitializer = () -> 0L;
```

#### Adder

The next thing we need to do in order to build a stream aggregator is to define the logic for combining two aggregates.
This is accomplished using the Aggregator interface, which, like Initializer , is a functional interface that can be implemented using a lambda.
The implementing function needs to accept three parameters:
- The record key
- The record value
- The current aggregate value

### Aggregate tables

#### Subtractor 

The process of aggregating tables is pretty similar to aggregating streams.
We need an initializer and an adder function.
However, tables are mutable, and need to be able to update an aggregate value whenever a key is deleted. 16 We also need a third parameter, called a subtractor function.

## Interactive queries

### Materialized Stores

If we want to enable read-only access of the underlying state store for ad hoc queries, we can use one of the overloaded methods to force the materialization of the state store locally.
Materialized state stores differ from internal state stores in that they are explicitly named and are queryable outside of the processor topology.

```java
KTable<String, HighScores> highScores = grouped.aggregate(
    highScoresInitializer,
    highScoresAdder,
    Materialized.<String, HighScores, KeyValueStore<Bytes, byte[]>>
        as("leader-boards")
        .withKeySerde(Serdes.String())
        .withValueSerde(JsonSerdes.HighScores()));
```

### Accessing Read-Only State Stores

Types of read-only state stores:
- QueryableStoreTypes.keyValueStore()
- QueryableStoreTypes.timestampedKeyValueStore()
- QueryableStoreTypes.windowStore()
- QueryableStoreTypes.timestampedWindowStore()
- QueryableStoreTypes.sessionStore()

```java
ReadOnlyKeyValueStore<String, HighScores> stateStore =
    streams.store(
        StoreQueryParameters.fromNameAndType("leader-boards",
            QueryableStoreTypes.keyValueStore()));
```

### 

### 

### 

