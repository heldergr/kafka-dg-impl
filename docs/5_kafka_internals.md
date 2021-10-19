# Kafka internals

## Cluster membership

- Kafka uses Apache Zookeeper to maintain the list of brokers that are currently members of a cluster
- Every broker has a unique identifier that is either set in the broker configuration file or automatically generated
- Every time a broker process starts, it registers itself with its ID in Zookeeper by creating an ephemeral node
- Different Kafka components subscribe to the /brokers/ids path in Zookeeper where brokers are registered so they get notified when brokers are added or removed

## The controller

- The controller is one of the Kafka brokers that, in addition to the usual broker functionality, is responsible for electing partition leaders
- When the controller broker is stopped or loses connectivity to Zookeeper, the ephemeral node will disappear
- Other brokers in the cluster will be notified through the Zookeeper watch that the controller is gone and will attempt to create the controller node in Zookeeper themselves
- When the controller notices that a broker left the cluster (by watching the relevant Zookeeper path), it knows that all the partitions that had a leader on that broker will need a new leader
- To summarize, Kafka uses Zookeeper’s ephemeral node feature to elect a controller and to notify the controller when nodes join and leave the cluster
    - The controller is responsible for electing leaders among the partitions and replicas whenever it notices nodes join and leave the cluster
    - The controller uses the epoch number to prevent a “split brain” scenario where two nodes believe each is the current controller

## Replication

- Replication is critical because it is the way Kafka guarantees availability and durability when individual nodes inevitably fail
- **Leader replica**
    - Each partition has a single replica designated as the leader
    - All produce and consume requests go through the leader, in order to guarantee consistency
_ **Follower replica**
    - Followers (not leader) don’t serve client requests; their only job is to replicate messages from the leader and stay up-to-date with the most recent messages the leader has
    - In the event that a leader replica for a partition crashes, one of the follower replicas will be promoted to become the new leader for the partition
- Another task the leader is responsible for is knowing which of the follower replicas is up-to-date with the leader

### In (out of) sync replicas

- In order to stay in sync with the leader, the replicas send the leader Fetch requests, the exact same type of requests that consumers send in order to consume messages
    - In response to those requests, the leader sends the messages to the replicas
    - Those Fetch requests contain the offset of the message that the replica wants to receive next, and will always be in order
    - If a replica hasn’t requested a message in more than 10 seconds or if it has requested messages but hasn’t caught up to the most recent message in more than 10 seconds, the replica is considered out of sync
- The inverse of this, replicas that are consistently asking for the latest messages, is called in-sync replicas
    - Only in-sync replicas are eligible to be elected as partition leaders in case the existing leader fails
- The amount of time a follower can be inactive or behind before it is considered out of sync is controlled by the **replica.lag.time.max.ms** configuration parameter
- 

## Request processing

- Most of what a Kafka broker does is process requests sent to the partition leaders from clients, partition replicas, and the controller
- Kafka has a binary protocol (over TCP) that specifies the format of the requests and how brokers respond to them both when the request is processed successfully or when the broker encounters errors while processing the request
- Clients always initiate connections and send requests, and the broker processes the requests and responds to them
- All requests sent to the broker from a specific client will be processed in the order in which they were received—this guarantee is what allows Kafka to behave as a message queue and provide ordering guarantees on the messages it stores
- Messages: 
    - request type
    - request version
    - correlation ID
    - client ID

Most common types of requests:

- **Produce requests**: Sent by producers and contain messages the clients write to Kafka brokers
- **Fetch requests**: Sent by consumers and follower replicas when they read messages from Kafka brokers

Metadata request:

Kafka clients use another request type called a metadata request, which includes a list of topics the client is
interested in. The server response specifies which partitions exist in the topics, the replicas for each partition, and which replica is the leader. Metadata requests can be
sent to any broker because all brokers have a metadata cache that contains this information.

### Produce requests

- What leader replica does when receive a produce request for a partition
    - Does the user sending the data have write privileges on the topic?
    - Is the number of acks specified in the request valid (only 0, 1, and “all” are allowed)?
    - If acks is set to all , are there enough in-sync replicas for safely writing the message?
- On Linux, the messages are written to the filesystem cache and there is no guarantee about when they will be written to disk
    - Kafka does not wait for the data to get persisted to disk—it relies on replication for message durability

### Fetch requests

- The client sends a request, asking the broker to send messages from a list of topics, partitions, and offsets—something like “Please send me messages starting at offset 53 in partition 0 of topic Test and messages starting at offset 64 in  partition 3 of topic Test.” 
    - Clients also specify a limit to how much data the broker can return for each partition
    - The limit is important because clients need to allocate memory that will hold the response sent back from the broker
    - Without this limit, brokers could send back replies large enough to cause clients to run out of memory
- If the offset exists, the broker will read messages from the partition, up to the limit set by the client in the request, and send the messages to the client

### Other requests

- **Most common types: metadata, produce and fetch**
- LeaderAndIsr: controller tells a broker it is the new leader for a partition and it can starts receiving requests
- Currently there are 20 different request types in the Kafka protocol
    -  OffsetCommitRequest , OffsetFetchRequest , and ListOffsetsRequest: related to topic offset commits

## Physical storage

- The basic storage unit of Kafka is a partition replica
    - Partitions cannot be split between multiple brokers and not even between multiple disks on the same broke

### Partition allocation

- When doing the allocations, the goals are:
    - To spread replicas evenly among brokers
    - To make sure that for each partition, each replica is on a different broker
    - If the brokers have rack information (available in Kafka release 0.10.0 and higher), then assign the replicas for each partition to different racks if possible
        - This is great, because if the first rack goes offline, we know that we still have a surviving replica and therefore the partition is still available
        - This will be true for all our replicas, so we have guaranteed availability in the case of rack failure

### File management

- Finding the messages to be removed (retention by time or space) that need purging in a large file and then deleting a portion of the file is both time-consuming and error-prone, we instead split each partition into segments
    - By default, each segment contains either 1 GB of data or a week of data, whichever is smaller
    - As a Kafka broker is writing to a partition, if the segment limit is reached, we close the file and start a new one
- **The segment we are currently writing to is called an active segment**

### File format

- Each segment is stored in a single data file
- Inside the file, we store Kafka messages and their offsets
- The format of the data on the disk is identical to the format of the messages that we send from the producer to the broker and later from the broker to the consumers
    - Using the same message format on disk and over the wire is what allows Kafka to use zero-copy optimization when sending messages to consumers and also avoid decompressing and recompressing messages that the producer already compressed

### Indexes

- Kafka allows consumers to start fetching messages from any available offset
- This means that if a consumer asks for 1 MB messages starting at offset 100, the broker must be able to quickly locate the message for offset 100 (which can be in any of the segments for the partition) and start reading the messages from that offset on
- In order to help brokers quickly locate the message for a given offset, Kafka maintains an index for each partition
- The index maps offsets to segment files and positions within the file

### Compaction

- Kafka supports such use cases by allowing the retention policy on a topic to be delete, which deletes events older than retention time, to compact, which only stores the most recent value for each key in the topic
- Enabled compaction: **log.cleaner.enabled**
- Messages can be
    - clean: messages that have been compacted before
    - dirty: messages that were written after the last compaction

### How compaction works

### Deleted events

- produce a message with a null value for the key we want to remove from the topic

### When are topics compacted

- active segments (those that were not closed yet) are not compacted