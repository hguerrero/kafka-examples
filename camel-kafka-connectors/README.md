# Timer Camel Kafka Connector Example

In this example we will be using the Camel Kafka Timer connector to trigger a new event every certain period into an specific topic.

## Download to local

1. Clone the GitHub repository

   ```sh
   git clone https://github.com/hguerrero/kafka-examples.git
   ```

2. Navigate to `/camel-kafka-connectors`directory.

   ```sh
   cd /kafka-examples/camel-kafka-connectors
   ```

## Download the Camel Timer Kafka connector

You will need to download the Camel connector plugin from the Apache Camel website [connector list](https://camel.apache.org/camel-kafka-connector/latest/connectors.html). In our example we will be using the version 0.6.1.

Issue the following command in the root folder of the project:

```sh
curl https://repo.maven.apache.org/maven2/org/apache/camel/kafkaconnector/camel-timer-kafka-connector/0.6.1/camel-timer-kafka-connector-0.6.1-package.tar.gz | tar xvz -C plugins
```

This will download the connector into the `plugins` folder. This folder will be mounted in the Kafka Connect cotnainer.

> Kafka Connect finds the plugins using a _plugin path_ where we add the list of directory paths.

## Start the environment

Start the platform by running Docker Compose in detached mode.

```sh
docker-compose up -d
```

You should see an output similar to the following:

```sh
Starting zookeeper ... done
Starting broker    ... done
Starting schema-registry ... done
Starting connect         ... done
```

This starts the containers for all the platform components required for this example: 1 Zookeeper node, 1 Kafka broker and 1 Kafka Connect node and the schema registry.

Check all the containers are running

```sh
docker ps
```

You should see something similar to the following:

```sh
CONTAINER ID        IMAGE                                         COMMAND                  CREATED             STATUS                             PORTS                                                                      NAMES
38b8403ece65        cnfldemos/kafka-connect-datagen:0.4.0-6.0.0   "/etc/confluent/dock…"   8 days ago          Up 34 seconds (health: starting)   0.0.0.0:8083->8083/tcp, 9092/tcp                                           connect
5e757bcd5027        confluentinc/cp-schema-registry:6.0.0         "/etc/confluent/dock…"   8 days ago          Up 34 seconds                      0.0.0.0:8081->8081/tcp                                                     schema-registry
d6b865894d38        confluentinc/cp-kafka:6.0.0                   "/etc/confluent/dock…"   8 days ago          Up 34 seconds                      0.0.0.0:9092->9092/tcp, 0.0.0.0:9101->9101/tcp, 0.0.0.0:29092->29092/tcp   broker
a28b5e2868fd        confluentinc/cp-zookeeper:6.0.0               "/etc/confluent/dock…"   8 days ago          Up 13 hours                        2888/tcp, 0.0.0.0:2181->2181/tcp, 3888/tcp
```

>Wait a few moments for all the containers to start.

Kafka Connect supports a REST API for managing connectors running on port `8083`. We can query this API to check the information regarding the _connector-plugins_ and the _connectors_.

Let's check the plugins available by calling the following command

```sh
curl -s http://localhost:8083/connector-plugins
```

If you are using `jq`

```sh
curl -s http://localhost:8083/connector-plugins | jq
```

> We are using jq for JSON processing

The output should be a list of the plugins available where you will find the `CamelTimerSourceConnector`

```json
[
  {
    "class": "io.confluent.kafka.connect.datagen.DatagenConnector",
    "type": "source",
    "version": "null"
  },
  {
    "class": "org.apache.camel.kafkaconnector.CamelSinkConnector",
    "type": "sink",
    "version": "0.6.1"
  },
  {
    "class": "org.apache.camel.kafkaconnector.CamelSourceConnector",
    "type": "source",
    "version": "0.6.1"
  },
  {
    "class": "org.apache.camel.kafkaconnector.timer.CamelTimerSourceConnector",
    "type": "source",
    "version": "0.6.1"
  },
...
]
```

## Add Camel Timer connector using REST API

Now that we have the plugin availble, we can configure a `connector` that will trigger our event. Because we are using the REST API, we need to configure our connector using a JSON object where we will define the properties required to run our connector.

We will be using the following configuration from the `timer.json` file:

```json
{
    "name": "timer",
    "config": {
        "connector.class": "org.apache.camel.kafkaconnector.timer.CamelTimerSourceConnector",
        "topics": "camel.timer.1",
        "camel.source.path.timerName": "timer",
        "camel.source.endpoint.period": "5000",
        "key.converter": "org.apache.kafka.connect.storage.StringConverter",
        "value.converter": "org.apache.kafka.connect.json.JsonConverter",
        "value.converter.schemas.enable": "false",
        "tasks.max": "1",
        "transforms": "HoistField,InsertField",
        "transforms.HoistField.type": "org.apache.kafka.connect.transforms.HoistField$Value",
        "transforms.HoistField.field": "timer",
        "transforms.InsertField.type": "org.apache.kafka.connect.transforms.InsertField$Value",
        "transforms.InsertField.timestamp.field": "timer",
        "transforms.InsertField.topic.field": "topic"
    }
}
```

We will define some required configuration: the name of our connector: `timer`and the proper `config` properties. Then adding:

* `connector.class` pointing to the FQN of the `CamelTimerSourceConnector`
* `topics`name where we will be delivering the events
* `camel.source.path.timerName`the required specific Camel component minimum configuration and
* `camel.source.endpoint.period` to send the event every 5 seconds

As you can notice, there are other configuration values that are Kafka Connect generics like the key and value converters, the number of task and some transformations just to get a better record structure.

Time to POST it with the following command:

```sh
curl -s -d @timer.json -H "Content-Type: application/json" -X POST http://localhost:8083/connectors
```

if you are using `jq`:

```sh
curl -s -d @timer.json -H "Content-Type: application/json" -X POST http://localhost:8083/connectors | jq
```

If the command was successful it will return the information of the new connector:

```json
{
  "name": "timer",
  "config": {
    "connector.class": "org.apache.camel.kafkaconnector.timer.CamelTimerSourceConnector",
    "topics": "camel.timer.1",
    "camel.source.path.timerName": "timer",
    "camel.source.endpoint.period": "5000",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",
    "tasks.max": "1",
    "transforms": "HoistField,InsertField",
    "transforms.HoistField.type": "org.apache.kafka.connect.transforms.HoistField$Value",
    "transforms.HoistField.field": "timer",
    "transforms.InsertField.type": "org.apache.kafka.connect.transforms.InsertField$Value",
    "transforms.InsertField.timestamp.field": "timer",
    "transforms.InsertField.topic.field": "topic",
    "name": "timer"
  },
  "tasks": [],
  "type": "source"
}
```

## Verify Kafka events

We can now verify the setup is working correctly. We will be using the `kafkacat`utility for this purpose:

```sh
kafkacat -b localhost:9092 -t camel.timer.1
```

You should see events coming in every few seconds:

```json
{"timer":null,"topic":"camel.timer.1"}
{"timer":null,"topic":"camel.timer.1"}
{"timer":null,"topic":"camel.timer.1"}
```

## Conclusion

Congratulations! You just create a simplet timer to send events to Apache Kafka! You can check the [Camel Kafka Timer component documentation](https://camel.apache.org/camel-kafka-connector/latest/connectors/camel-timer-kafka-source-connector.html) to view how to configure options like the period or delay time of the timer.
