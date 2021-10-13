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

### Produce requests

### Fetch requests

### Other requests

## Physical storage

### Partition allocation

### File management

### File format

### Indexes

### Compaction

### How compaction works

### Deleted events

### When are topics compacted


