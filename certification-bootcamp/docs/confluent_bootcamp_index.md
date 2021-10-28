# Confluent bootcamp

Table of Contents

    1. [Introduction](1_introduction.md)
    2. [Conceptual Knowledge of Kafka](2_conceptual_knowledes_of_kafka.md)
    3. Conceptual Knowledge of Confluent Platform
    4. Conceptual Knowledge of Building Applications
    5. Network Technologies Relating to Confluent and Kafka
    6. Kafka Producer Applications
    7. Kafka Consumer Applications
    8. Kafka Streams Applications
    9. ksqlDB Queries
    10. Apache Avro and the Schema Registry
    11. Integrating Kafka with External Systems with Kafka Connect
    12. Security
    13. Testing
    14. Tutorials

## 3. Conceptual Knowledge of Building Applications

Getting started with application development for Kafka and Confluent starts with understanding from a conceptual level how the applications connect, interact, and function within the Kafka and Confluent ecosystem.

    https://www.confluent.io/blog/event-streaming-platform-1/

        This link appeared above but pay close attention to the application section, which discusses an overview of apps within the ecosystem.

    https://developer.confluent.io/

    https://docs.confluent.io/platform/current/build-applications.html

    https://docs.confluent.io/platform/current/app-development/index.html

    https://developer.okta.com/blog/2019/11/19/java-kafka — This is important because it also discusses security, which should be considered from the start.

## Network Technologies Relating to Confluent and Kafka

Understanding the protocols and methods that data flows across the wire are an absolute necessity for developing applications. A great place to start is:

    https://kafka.apache.org/0100/protocol.html

    https://confluent.buzzsprout.com/186154/2055213-securing-the-cloud-with-vpc-peering-ft-daniel-lamotte

    https://segment.com/blog/kafka-optimization/

        Discusses strategies to optimize a Kafka network and how doing so can lead to big savings.

    https://blog.cloudflare.com/squeezing-the-firehose/

        Useful to understand what’s going on under the hood and work through it for better performance.

    https://docs.confluent.io/cloud/current/connectors/internet-resources.html

## Kafka Producer Applications

Creating a basic producer application is one of the most fundamental tasks for a Kafka developer.

    https://kafka-tutorials.confluent.io/creating-first-apache-kafka-producer-application/kafka.html

    https://kafka.apache.org/0110/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html

        Here you’ll find basic documentation for the KafkaProducer class

    https://dzone.com/articles/how-to-implement-kafka-producer

    https://kafka-tutorials.confluent.io/creating-first-apache-kafka-producer-application/confluent.htm

    https://kafka-python.readthedocs.io/en/master/

        If you’re a Python programmer, you’ll find helpful examples of both a producer and consumer written with the kafka-python framework. It’s designed to be Java-like in its approach and will be helpful to understand how a typical producer is built. Many other languages are supported. A web search can provide guidance for your chosen development language.

## Kafka Consumer Applications

This is the other part of the equation for Kafka app development. Consumers and producers have to work efficiently together.

Here are some helpful resources for study:

    https://docs.confluent.io/platform/current/clients/consumer.html

    https://kafka.apache.org/26/javadoc/index.html?org/apache/kafka/clients/consumer/KafkaConsumer.html

        Here’s the documentation for the basic KafkaConsumer class.

    https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/ch04.html

    https://kafka-tutorials.confluent.io/creating-first-apache-kafka-consumer-application/confluent.html

    https://docs.confluent.io/3.0.0/clients/consumer.html

    https://docs.confluent.io/platform/current/installation/configuration/consumer-configs.html

        Shows the various consumer configuration parameters available on Confluent Platform.

    https://www.sohamkamani.com/golang/working-with-kafka/

        If you’re working in Go/Golang, you’ll find this example helpful. Again, other languages are available.

## Kafka Streams Applications

Kafka Streams is a cross-platform client library for building elastic, secure, and fault-tolerant applications and microservices.

    https://kafka.apache.org/documentation/streams/

    https://www.confluent.io/blog/introducing-kafka-streams-stream-processing-made-simple/

    https://www.youtube.com/watch?v=UbNoL5tJEjc

        Tim Berglund breaks down what Kafka Streams are and why they’re important.

    https://docs.confluent.io/platform/current/streams/index.html

        Here, you’ll find a number of helpful links, including podcasts, recommended reading, and screencasts.

## ksqlDB Queries

ksqlDB is one of the most important tools in your toolbox for getting work done. Here are a few resources to help you understand and apply its powerful abilities.

    https://www.confluent.io/kafka-summit-lon19/ksql-in-practice/

    https://www.confluent.io/kafka-summit-london18/ksql-201-a-deep-dive-into-query-processing/

    https://www.confluent.io/blog/intro-to-ksqldb-sql-database-streaming/

    https://www.confluent.io/blog/kafka-streams-vs-ksqldb-compared/

        This is a helpful blog post about the differences between ksqlDB and Kafka Streams and how to choose the best fit for your use case.

    https://ksqldb.io/

        Here’s the main site for ksqlDB

## Apache Avro and the Schema Registry

Understanding Apache Avro and how to use it will be important as you work with data and the Schema Registry. Here are several resources to dive into:

    https://www.confluent.io/blog/avro-kafka-data/

        This talks about the importance of Avro and provides context for using it with Kafka data.

    http://avro.apache.org/docs/current/spec.html

    https://docs.confluent.io/platform/current/schema-registry/index.html

        Confluent Platform provides some additional features for working with Avro

    https://docs.confluent.io/platform/current/schema-registry/schema_registry_tutorial.html

        This link provides some great tutorials for working with the Schema Registry.

    https://medium.com/@stephane.maarek/introduction-to-schemas-in-apache-kafka-with-the-confluent-schema-registry-3bf55e401321

        Here you can find some great context for working with the Schema Registry.

    https://docs.confluent.io/platform/current/schema-registry/develop/api.html

        Here’s the reference for the Schema Registry API

## Integrating Kafka with External Systems with Kafka Connect

One of the strengths of Kafka is its ability to integrate with a variety of external resources. Here are several links to help you understand the concepts and practical application of Kafka Connect and working with connectors.

    https://docs.confluent.io/platform/current/connect/index.html

        This is a great resource for understanding what Kafka Connect is and how to use it.

    https://docs.confluent.io/5.5.0/connect/index.html

        This will help get you started

    https://confluent.buzzsprout.com/186154/1265780-why-kafka-connect-ft-robin-moffatt

        This podcast provides a case for using Kafka Connect. The discussion covers a variety of use cases as well as some of the lesser-known features.

    https://docs.confluent.io/home/connect/overview.html

        This page provides a description of different types of connectors as well as how to install and use them.

## Security

Security is of paramount importance and keeping your Kafka data secure should be a consideration from the moment you start to develop an application. Implementing security from the beginning is much easier than refactoring your code later.

Here are a few resources to help you understand the security considerations and implications:

    https://kafka.apache.org/documentation/#security

    https://docs.confluent.io/platform/current/security/index.html

        Here’s the documentation for the Confluent-specific implementations of security, as well as a basic overview and tutorial.

    https://medium.com/@stephane.maarek/introduction-to-apache-kafka-security-c8951d410adf

        This is a great blog post that discusses why you should care about security for Kafka and talks through some of the standard security protocols and countermeasures for the platform.

    https://developer.ibm.com/components/kafka/tutorials/kafka-authn-authz/

        This is a handy guide that talks through encryption and ACLs.

## Testing

Testing is one of the best ways to ensure that your applications function as expected.

Here are a number of resources to help get you started:

    https://docs.confluent.io/cloud/current/client-apps/testing.html

        This covers unit, integration, performance, chaos, and other forms of testing.

    https://kafka.apache.org/21/documentation/streams/developer-guide/testing.html

        This discusses testing for Kafka Streams

    https://www.confluent.io/blog/testing-kafka-streams/

    https://dzone.com/articles/a-quick-and-practical-example-of-kafka-testing

## Tutorials

Along the way you’ll find tutorials for many of the topics listed above. Here’s a great collection of them, all in one place:

    https://kafka-tutorials.confluent.io/
