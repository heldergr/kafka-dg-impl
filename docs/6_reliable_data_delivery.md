# Chapter 6 - Reliable Data Delivery

- Reliability is a property of a system — not of a single component — so even when we are talking
  about the reliability guarantees of Apache Kafka, you will need to keep the entire system and its use cases in mind

## Reliability Guarantees

- ACID in relational databases is an example of guarantee
- Kafka guarantees
    - Kafka provides order guarantee of messages in a partition
    - Produced messages are considered “committed” when they were written to the partition on all its in-sync replicas (but not necessarily flushed to disk) 
    - Messages that are committed will not be lost as long as at least one replica remains alive
    - Consumers can only read messages that are committed
- There are trade-offs involving how important it is to reliably and consistently store messages versus other important considerations such as availability, high throughput, low latency, and hardware costs

## Replication

- Having a message written in multiple replicas is how Kafka provides durability of messages in the event of a crash
- A replica is considered in-sync if it is the leader for a partition, or if it is a follower that:
    - Has an active session with Zookeeper—meaning, it sent a heartbeat to Zookeeper in the last 6 seconds (configurable)
    - Fetched messages from the leader in the last 10 seconds (configurable)
    - Fetched the most recent messages from the leader in the last 10 seconds
        - That is, it isn’t enough that the follower is still getting messages from the leader; it must have almost no lag

## Broker Configuration

### Replication factor

- The topic-level configuration is **replication.factor**
- At the broker level, you control the **default.replication.factor** for automatically created topics
- Even after a topic exists, you can choose to add or remove replicas and thereby modify the replication factor
- On the flip side, for a replication factor of N, you will need at least N brokers and you will store N copies of the data, meaning   you will need N times as much disk space
    - We are basically trading availability for hardware
- Placement of replicas is also very important
    - By default, Kafka will make sure each replica for a partition is on a separate broker
    - However, in some cases, this is not safe enough
    - If all replicas for a partition are placed on brokers that are on the same rack and the top-of-rack switch misbehaves, you will lose availability of the partition regardless of the replication factor

### Unclean leader election

- The parameter name is **unclean.leader.election.enable** and by default it is set to true
- We need to make a difficult decision:
    - If we don’t allow the out-of-sync replica to become the new leader, the partition will remain offline until we bring the old leader (and the last in-sync replica) back online
        - In some cases (e.g., memory chip needs replacement), this can take many hours.
    - If we do allow the out-of-sync replica to become the new leader, we are going to lose all messages that were written to the old leader while that replica was out of sync and also cause some inconsistencies in consumers
- In summary, if we allow out-of-sync replicas to become leaders, we risk data loss and data inconsistencies
    - If we don’t allow them to become leaders, we face lower availability as we must wait for the original leader to become available before the partition is back online.
- We typically see unclean leader election disabled in systems where data quality and consistency are critical—banking systems are a good example (most banks would rather be unable to process credit card payments for few minutes or even hours than risk processing a payment incorrectly)

### Minimum in-sync replicas

- If you would like to be sure that committed data is written to more than one replica, you need to set the minimum number of in-sync replicas to a higher value

## Using Producers in a Reliable System

- everyone who writes applications that produce to Kafka must pay attention to:
    - Use the correct acks configuration to match reliability requirements
    - Handle errors correctly both in configuration and in code

### Send acknowledgements

- **acks=0** means that a message is considered to be written successfully to Kafka if the producer managed to send it over the network
- **acks=1** means that the leader will send either an acknowledgment or an error the moment it got the message and wrote it to the partition data file (but not necessarily synced to disk)
- **acks=all** means that the leader will wait until all in-sync replicas got the message before sending back an acknowledgment or an error 
    - In conjunction with the **min.insync.replica** configuration on the broker, this lets you control how many replicas get the message before it is acknowledged
    - **safest** option but with a performance cost

### Configuring producer retries

- The producer can handle automatically retriable errors that are returned by the broker for you
- **In general, if your goal is to never lose a message, your best approach is to configure the producer to keep trying to send the messages when it encounters a retriable error**
- Note that retrying to send a failed message often includes a small risk that both messages were successfully written to the broker, leading to duplicates
    - **In newer versions there is an option to set the producer as idempotent via properties configuration** 

### Additional error handlings

Using the built-in producer retries is an easy way to correctly handle a large variety of errors without loss of messages, but as a developer, you must still be able to handle other types of errors.

## Using Consumer in a Reliable System

- As we saw in the first part of this chapter, data is only available to consumers after it has been committed to Kafka—meaning it was written to all in-sync replicas. This means that consumers get data that is guaranteed to be consistent.
- Commit offset
    - For each partition it is consuming, the consumer stores its current location, so they or another consumer will know where to continue after a restart

### Committed messages vs committed offsets

This is different from a committed message, which, as discussed previously, is a message that was written to all in-sync replicas and is available to consumers. 
Committed offsets are offsets the consumer sent to Kafka to acknowledge that it received and processed all the messages in a partition up to this specific offset.

### Important consumer configuration properties for reliable processing

- **group.id**: a way to spread the messages over many consumers
    - If you need a consumer to see, on its own, every single message in the topics it is subscribed to—it will need a unique group.id
- **auto.offset.reset**: controls what the consumer will do when no offsets were committed (e.g., when the consumer first starts) or when the consumer asks for offsets that don’t exist in the broker
    - earliest
    - latest
- **enable.auto.commit**: controls if you are you going to let the consumer commit offsets for you based on schedule, or are you planning on committing offsets manually in your code
- **auto.commit.interval.ms**: If you choose to commit offsets automatically, this configuration lets you configure how frequently they will be committed

### Explicitly committing offsets in consumers

- Important considerations when developing a consumer to handle data reliably
    - Always commit offsets after events were processed
    - Commit frequency is a trade-off between performance and number of duplicates in the event of a crash
    - Make sure you know exactly what offsets you are committing
    - Rebalances
        - the bigger picture is that this usually involves committing offsets before partitions are revoked and cleaning any state you maintain when you are assigned new partitions
    - Consumers may need to retry
        - One alternative is put the records with error on a buffer and keep trying to reprocess 
            - pause() and resume() can help you to put some retriable records in a buffer for latter processing
        - Write the record that caused the retriable error to a separate topic and continue
            - a second consumer with a separate group.id can consume those records in parallel
    - Consumers may need to maintain state
    - Handling long processing times
        - Even if you don’t want to process additional records, you must continue polling so the client can send heartbeats to the broker
    - Exactly-once delivery
        - The easiest and probably most common way to do exactly-once is by writing results to a system that has some support for unique keys

## Validating System Reliability

### Validating configuration

Kafka includes two important tools to help with this validation. The org.apache.kafka.tools package includes VerifiableProducer and Verifiable consumer classes. These can run as command-line tools, or be embedded in an automated testing framework.

### Validating applications

Test your application under the following failure conditions:

- Clients lose connectivity to the server (your system administrator can assist you in simulating network failures)
- Leader election
- Rolling restart of brokers
- Rolling restart of consumers
- Rolling restart of producers

The application developers should know the expected behavior the application should have one the failures happen.

### Monitoring reliability production

- JMX metrics via Kafka's Java clients
   - for producers is important to monitor **error-rate** and **retry-rate**
   - in the consumer side the most important metric is the consumer lag
- For even better monitoring, you can add a monitoring consumer on critical topics that will count events and compare them to the events produced, so you will get accurate monitoring of producers even if no one is consuming the events at a given point in time
