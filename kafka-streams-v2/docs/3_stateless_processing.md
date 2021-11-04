# Chapter 3 - Stateless processing

The simplest form of stream processing requires no memory of previously seen events.
Each event is consumed, processed, 1 and subsequently forgotten.
This paradigm is called stateless processing, and Kafka Streams includes a rich set of operators for working with data in a stateless way.

- Filtering records
- Adding and removing fields
- Rekeying records
- Branching streams
- Merging streams
- Transforming records into one or more outputs
- Enriching records, one at a time

## Stateless versus stateful processing

- In stateless applications, each event handled by your Kafka Streams application is processed independently of other events, and only stream views are needed by
your application
- Stateful applications, on the other hand, need to remember information about previously seen events in one or more steps of your processor topology, usually for the purpose of aggregating, windowing, or joining event streams

If your Kafka Streams application requires only stateless operators (and therefore does not need to maintain any memory of previously seen events), then your application is considered stateless.

## Project setup

- Code: <https://github.com/mitch-seymour/mastering-kafka-streams-and-ksqldb.git>

## Adding a Kstream source processor

## Serialization/Deserialization

In Kafka Streams, serializer and deserializer classes are often combined into a single class called a Serdes, and the library ships with several implementations.
Whenever you need to deserialize/serialize data in Kafka Streams, you should first check to see whether or not one of the built-in Serdes classes fits your needs.

### Building a custom Serdes

```java
public class TweetSerdes implements Serde<Tweet> {
    @Override
    public Serializer<Tweet> serializer() {
        return new TweetSerializer();
    }
    @Override
    public Deserializer<Tweet> deserializer() {
        return new TweetDeserializer();
    }
}
```

### Handling Deserialization Errors in Kafka Streams

You should always be explicit with how your application handles deserialization errors, otherwise you may get an unwelcome surprise if a malformed record ends up in your source topic. 
Kafka Streams has a special config, DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG , that can be used to specify a deserialization exception handler.

## Filtering data

- Main filtering operations
   - **filter**
   - **filterNot**

If your application requires a filtering condition, you should filter as early as possible.
There’s no point in transforming or enriching data that will simply be thrown away in a subsequent step, especially if the logic for processing the unneeded event is computationally expensive.

## Branching data

Kafka Streams also allows us to use predicates to separate (or branch) streams. Branching is typically required when events need to be routed to different stream processing steps or output topics based on some attribute of the event itself.

```java
Predicate<byte[], Tweet> englishTweets = (key, value) -> value.getLang().equals("en");
Predicate<byte[], Tweet> nonEnglishTweets = (key, value) -> !value.getLang().equals("en");

KStream<byte[], Tweet>[] branches = filtered.branch(englishTweets, nonEnglishTweets);
KStream<byte[], Tweet> englishStream = branches[0];
KStream<byte[], Tweet> nonEnglishStream = branches[1];
```

## Translating tweets

- Operations for transforming:
    - **map**
    - **mapValues**

```java
KStream<byte[], Tweet> translatedStream = nonEnglishStream.map(
    (key, tweet) -> {
        byte[] newKey = tweet.getUsername().getBytes();
        Tweet translatedTweet = languageClient.translate(tweet, "en");
        return KeyValue.pair(newKey, translatedTweet);
    }
);
```

**It is recommended to use mapValues instead of map whenever possible, as it allows Kafka Streams to potentially execute the program more efficiently.**

## Merging streams

Kafka Streams provides an easy method for combining multiple streams into a single stream.
Merging streams can be thought of as the opposite of a branching operation, and is typically used when the same processing logic can be applied to more than one stream in your application.

## Enriching tweets

A very common stream processing task: we need to convert a single input record into a variable number of output records. Luckily for us, Kafka Streams includes two operators that can help with this use case:
- flatMap
- flatMapValues

### Avro Data class

Avro is a popular format in the Kafka community, largely due to its compact byte representation (which is advantageous for high throughput applications), native support
for record schemas, 14 and a schema management tool called Schema Registry, which works well with Kafka Streams and has had strong support for Avro since its inception.

Avro can work with two different types of records:

- **generic record**: you don't know the schema
- **specific record**: you know the schema

## Serializing Avro data

When we serialize data using Avro, we have two choices (Avro Serde classes are available for both approaches):
- Include the Avro schema in each record.
- Use an even more compact format, by saving the Avro schema in Confluent Schema Registry, and only including a much smaller schema ID in each record instead of the entire schema.

## Adding a sink processor

There are a few operators for adding sink processors:
- to: reach a terminal step in the stream
- through: return a new KStream instance for appending additional operators/stream processing logic (*deprecated*)
- repartition: return a new KStream instance for appending additional operators/stream processing logic
