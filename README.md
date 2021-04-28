# Apache Kafka Examples

This repository contains a set of examples to be used with Apache Kafka and other open source components.

* [Timer Camel Kafka Connector](camel-kafka-connectors): A simple example of how to use the Apache Camel subproject plugins with Kafka Connect using the Camel Kafka Timer connector.

## System Requirements

These applications are designed to be run with Apache Kafka 2.x versions. To compile and run these projects you will also need:

* Docker:
  * Docker version 1.11 or later is [installed and running](https://docs.docker.com/engine/installation/).
  * Docker Compose is [installed](https://docs.docker.com/compose/install/). Docker Compose is installed by default with Docker for Mac.
  * Docker memory is allocated minimally at 8 GB. When using Docker Desktop for Mac, the default Docker memory allocation is 2 GB.  You can change the default allocation to 8 GB in **Docker** > **Preferences** > **Advanced**.
* [Git](https://git-scm.com/downloads).
* [jq](https://stedolan.github.io/jq/) JSON processor
* [Kafkacat](https://github.com/edenhill/kafkacat) utility
* JDK 8 or 11+
* Maven 3
* GraalVM Community
* Internet connectivity.
