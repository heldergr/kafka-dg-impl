# 11 - Stream processing

Starting from version 0.10.0, Kafka does more than provide a reliable source of data streams to every popular stream-processing framework. Now Kafka includes a powerful stream-processing library as part of its collection of client libraries.
This allows developers to consume, process, and produce events in their own apps, without relying on an external processing framework.

## What is stream processing?

**What is a data stream (also called an event stream or streaming data)? First and foremost, a data stream is an abstraction representing an unbounded dataset. Unbounded means infinite and ever growing. 
The dataset is unbounded because over time, new records keep arriving.**

There are few other attributes of event streams model, in addition to their unbounded nature:

- **Event streams are ordered**
- **Immutable data records**
    - Events, once occured, can never be modified
    - A financial transaction that is cancelled does not disapear
    - Instead, an additional event is written to the stream, recording a cancellation of previous transaction 
- **Event streams are replayable**
    - this is a desirable property 

**It is worth noting that neither the definition of event streams nor the attributes we later listed say anything about the data contained in the events or the number of events per second.**

### Stream processing

This is a contentious and nonblocking option.
Filling the gap between the request-response world where we wait for events that take two milliseconds to process and the batch processing world where data is processed once a day and takes eight hours to complete.

## Stream-processing concepts

### time

Stream-processing systems typically refer to the following notions of time:

- **event time**: time the events we are tracking occurred and the record was created
    - kafka automatically creates one field for timestamp that maybe represent what we want for time 
- **log append time**: time the event arrived to the Kafka broker and was stored there
    - normally less important than event time and useless 
- **processing time**: time at which a stream-processing application received the event in order to perform some calculation
- ****

### state

Stream processing becomes really interesting when you have operations that involve multiple events: counting the number of events by type, moving averages, joining two streams to create an enriched stream of information, etc.
In those cases, it is not enough to look at each event by itself; you need to keep track of more information -- how many events of each type did we see this hour, all events that require joining, sums, averages, etc.
We call the information that is stored between events a **state**.

Types of state:

- **local or internal state**: State that is accessible only by a specific instance of the stream-processing application
    - extremely fast but it is limited to the amount of memory available 
- **external state**: State that is maintained in an external datastore, often a NoSQL system like Cassandra
    - virtually unlimited memory and can be accessed by many stream applications
    - downside is extra latency and complexity

### stream-table duality

Unlike tables, streams contain a history of changes. Streams are a string of events wherein each event caused a change.
A table contains a current state of the world, which is the result of many changes.
From this description, it is clear that streams and tables are two sides of the same coin—the world always changes, and sometimes we are interested in the events that caused those changes, whereas other times we are interested in the current state of the world.

In order to convert a table to a stream, we need to capture the changes that modify the table. Take all those insert , update , and delete events and store them in a stream.

In order to convert a stream to a table, we need to apply all the changes that the stream contains. This is also called materializing the stream. We create a table, either in memory, in an internal state store, or in an external database, and start going over all the events in the stream from beginning to end, changing the state as we go.

### time windows

Most operations on streams are windowed operations—operating on slices of time: moving averages, top products sold this week, 99th percentile load on the system, etc. Join operations on two streams are also windowed—we join events that occurred at the same slice of time.

Some important features:

- **size of the window**
- **how often the window moves (advance interval)**: how often it refreshes
- **how long the window remains updatable**

## Stream-processing desing patterns

### single-event processing

The most basic pattern of stream processing is the processing of each event in isolation.
This is also known as a map/filter pattern because it is commonly used to filter unnecessary events from the stream or transform each event.

### processing with local state

Most stream-processing applications are concerned with aggregating information, especially time-window aggregation.
An example of this is finding the minimum and maximum stock prices for each day of trading and calculating a moving average.

These aggregations require maintaining a state for the stream. All these can be done using local state (rather than a shared state) because each opera‐
tion in our example is a group by aggregate.

Some issues must be addressed:

- memory usage
- persistence: We need to make sure the state is not lost when an application instance shuts down, and that the state can be recovered when the instance starts again or is replaced by a different instance
- rebalancing: Partitions sometimes get reassigned to a different consumer
    - When this happens, the instance that loses the partition must store the last good state, and the instance that receives the partition must know to recover the correct state

### multiphase processing/repartitioning

Local state is great if you need a group by type of aggregate.
But what if you need a result that uses all available information? 
For example, suppose we want to publish the top 10 stocks each day—the 10 stocks that gained the most from opening to closing during each day of trading.

### processing with external lookup: Stream-table join

Sometimes stream processing requires integration with data external to the stream—validating transactions against a set of rules stored in a database, or enriching click‐stream information with data about the users who clicked.

In order to get good performance and scale, we need to cache the information from the database in our stream-processing application.

Keeping the cache up to date is challenging but if we can capture all the changes that happen to the database table in a stream of events, we can have our stream-processing job listen to this stream and update the cache based on database change events. 

### streaming join

When you join two streams, you are joining the entire history, trying to match events in one stream with events in the other stream that have the same key and happened in the same time-windows.
This is why a streaming-join is also called a **windowed-join**.

### out-of-sequence events

### reprocessing

- There are two variants of reprocessing apps:
    - We have an improved version of our stream-processing application
    - The existing stream-processing app is buggy

## Kafka streams by example

Apache Kafka has two streams APIs:

- low-level Processor API (rarely required)
- high-level Streams DSL

1. An application that uses the DSL API always starts with using the StreamBuilder to
create a processing topology
    - a directed graph (DAG) of transformations that are applied to the events in the streams
2. Then you create a KafkaStreams execution object from the topology
3. Starting the KafkaStreams object will start multiple threads, each applying the processing topology to events in the stream
4. The processing will conclude when you close the KafkaStreams object

### word count

### stock market statistics

### click stream enrichment

## Kafka Streams: Architecture overview

### Building a topology

### scaling to topology

### Surviving failures

## Stream Processing Use Cases 

## How to Choose a Stream-Processing Framework