# Installing Kafka

## First things first

- OS: prefer linux
- both kafka and zookeeper need Java to run
- zookeeper
    * kafka uses zookeeper to store cluster metadata and consumer details
    * it's recommended that an ensemble (zookeeper cluster) has an odd (i.e., 3, 5, etc) number of nodes
  
## Installing a kafka broker

- run some scripts
- test with simple operations like create topics, produce and consume messages

## Broker configuration

- general broker
    * **broker.id**: it must be and integer and should be unique in the same cluster
    * **port**: the port where kafka listens, default to 9092
    * **zookeeper.connect**: list of zookeepers hosts separated by semicolon (ex: host1:2181;host2:2181)
    * **log.dirs**: directory where kafka stores and log messages on disk. It can be more than one directory and kafka will distribute the partitions among the options
    * **num.recovery.threads.per.data.dir**: size of pool of threads kafka uses for handling log segments 
    * **auto.create.topics.enable**: tells if kafka will create topics automatically when a producers write a message, a consumer starts reading of a client requests metadata of a topic
- topic defaults: default properties related to properties
    * **num.partitions**: how many partitions a topic is created with. Care should be taken because number of partitions can only increase. If a topic should be created with less than default, so it should be created manually
    * **log.retention.ms**: determines for how long kafka will retain messages in disk per time unit
        * It can be specified in 3 different properties (*.minutes and *.hours) but the smaller unit takes precedence
    * **log.retention.bytes**: expires messages based on total number of bytes used by the retained messages
        * *this property is set per partition*
    * **log.segment.bytes**: size to close a log segment and open a new one
        * important note: only when one segment is closed it is considered for retention count, so log.retention properties are not message based
        * if produce rate is very low the log.retention.ms cannot work so well because one segment can take so long to be closed and considered in the retention
    * **log.segment.ms**: amount of time a segment should be closed so it will be considered for retention
    * **message.max.bytes**: maximum number of bytes one message can have to be accepted by the broker

## Hardware selection

- disk throughput: the performance of producer clients will be most directly influenced by the through  put of the broker disk that is used for storing log segments
- storage capacity: the amount of disk capacity that is needed is determined by how many messages need to be retained at any time
- memory: the way memory impacts kafka performance is on the amount of space available for page cache
- network: The available network throughput will specify the maximum amount of traffic that Kafka can handle. This is often the governing factor, combined with disk storage, for
cluster sizing
- cpu: it is not as important as other configurations but can be helpful to optimize network traffic and disk usage through compression

## Kafka in the cloud

- there are several combinations of cpu, memory and disk capacity to choose the right instances

## Kafka clusters

- scale and replication

### How many brokers

Main factors:

- The first factor to consider is how much **disk capacity** is required for retaining messages and how much storage is available on a single broker
- The other factor to consider is the capacity of the cluster to **handle requests**, for example, what is the capacity of the network interfaces

### Broker configuration

- All brokers must have two configurations
    * the same **zookeeper.connect** value pointing to the same zookeeper
    * a unique **broker.id**

### OS tunning

- virtual memory systems
- disk
- networking

## Production concerns

- Garbage collector options
- Datacenter layouts
- Collocation applications on zookeeper