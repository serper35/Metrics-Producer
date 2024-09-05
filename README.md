# Система мониторинга на основе Spring Kafka

## Архитектура системы

### Обзор
Система мониторинга состоит из следующих компонентов:

- Metrics Producer: Микросервис, который собирает и отправляет метрики в Kafka.
- Metrics Consumer: Микросервис, который получает и обрабатывает метрики из Kafka.  
  Доступ можно получить по ссылке:  
  https://github.com/serper35/Metrics-Consumer
- Kafka Broker: Платформа для обмена сообщениями между продюсером и консюмером.
- 
### Поток данных
- Metrics Producer собирает данные о метриках (например, загрузка CPU, использование памяти) и отправляет их в Kafka топик metrics-topic.
- Kafka Broker принимает сообщения от продюсера и сохраняет их в топике.
- Metrics Consumer подписывается на топик metrics-topic, получает сообщения, анализирует их и сохраняет в базу данных для дальнейшего использования.

### Диаграмма
[Metrics Producer] --(Kafka)--> [Kafka Broker] --(Kafka)--> [Metrics Consumer]

## Конфигурация Kafka
### Настройки Kafka
```java
version: '2.1'

services:
  zoo1:
    image: confluentinc/cp-zookeeper:7.3.2
    hostname: zoo1
    container_name: zoo1
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_SERVER_ID: 1
      ZOOKEEPER_SERVERS: zoo1:2888:3888

  kafka1:
    image: confluentinc/cp-kafka:7.3.2
    hostname: kafka1
    container_name: kafka1
    ports:
      - "9092:9092"
      - "29092:29092"
      - "9999:9999"
    environment:
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka1:19092,EXTERNAL://${DOCKER_HOST_IP:-127.0.0.1}:9092,DOCKER://host.docker.internal:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT,DOCKER:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_ZOOKEEPER_CONNECT: "zoo1:2181"
      KAFKA_BROKER_ID: 1
      KAFKA_LOG4J_LOGGERS: "kafka.controller=INFO,kafka.producer.async.DefaultEventHandler=INFO,state.change.logger=INFO"
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_JMX_PORT: 9001
      KAFKA_JMX_HOSTNAME: ${DOCKER_HOST_IP:-127.0.0.1}
      KAFKA_AUTHORIZER_CLASS_NAME: kafka.security.authorizer.AclAuthorizer
      KAFKA_ALLOW_EVERYONE_IF_NO_ACL_FOUND: "true"
    depends_on:
      - zoo1

  kafka-schema-registry:
    image: confluentinc/cp-schema-registry:7.3.2
    hostname: kafka-schema-registry
    container_name: kafka-schema-registry
    ports:
      - "8081:8081"
    environment:
      SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: PLAINTEXT://kafka1:19092
      SCHEMA_REGISTRY_HOST_NAME: kafka-schema-registry
      SCHEMA_REGISTRY_LISTENERS: http://0.0.0.0:8081
    depends_on:
      - zoo1
      - kafka1

  
  kafka-rest-proxy:
    image: confluentinc/cp-kafka-rest:7.3.2
    hostname: kafka-rest-proxy
    container_name: kafka-rest-proxy
    ports:
      - "8082:8082"
    environment:
      # KAFKA_REST_ZOOKEEPER_CONNECT: zoo1:2181
      KAFKA_REST_LISTENERS: http://0.0.0.0:8082/
      KAFKA_REST_SCHEMA_REGISTRY_URL: http://kafka-schema-registry:8081/
      KAFKA_REST_HOST_NAME: kafka-rest-proxy
      KAFKA_REST_BOOTSTRAP_SERVERS: PLAINTEXT://kafka1:19092
    depends_on:
      - zoo1
      - kafka1
      - kafka-schema-registry

  
  kafka-connect:
    image: confluentinc/cp-kafka-connect:7.3.2
    hostname: kafka-connect
    container_name: kafka-connect
    ports:
      - "8083:8083"
    environment:
      CONNECT_BOOTSTRAP_SERVERS: "kafka1:19092"
      CONNECT_REST_PORT: 8083
      CONNECT_GROUP_ID: compose-connect-group
      CONNECT_CONFIG_STORAGE_TOPIC: docker-connect-configs
      CONNECT_OFFSET_STORAGE_TOPIC: docker-connect-offsets
      CONNECT_STATUS_STORAGE_TOPIC: docker-connect-status
      CONNECT_KEY_CONVERTER: io.confluent.connect.avro.AvroConverter
      CONNECT_KEY_CONVERTER_SCHEMA_REGISTRY_URL: 'http://kafka-schema-registry:8081'
      CONNECT_VALUE_CONVERTER: io.confluent.connect.avro.AvroConverter
      CONNECT_VALUE_CONVERTER_SCHEMA_REGISTRY_URL: 'http://kafka-schema-registry:8081'
      CONNECT_INTERNAL_KEY_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_INTERNAL_VALUE_CONVERTER: "org.apache.kafka.connect.json.JsonConverter"
      CONNECT_REST_ADVERTISED_HOST_NAME: "kafka-connect"
      CONNECT_LOG4J_ROOT_LOGLEVEL: "INFO"
      CONNECT_LOG4J_LOGGERS: "org.apache.kafka.connect.runtime.rest=WARN,org.reflections=ERROR"
      CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR: "1"
      CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR: "1"
      CONNECT_STATUS_STORAGE_REPLICATION_FACTOR: "1"
      CONNECT_PLUGIN_PATH: '/usr/share/java,/etc/kafka-connect/jars,/usr/share/confluent-hub-components'
    volumes:
      - ./connectors:/etc/kafka-connect/jars/
    depends_on:
      - zoo1
      - kafka1
      - kafka-schema-registry
      - kafka-rest-proxy
    command:
      - bash
      - -c
      - |
        confluent-hub install --no-prompt debezium/debezium-connector-mysql:latest
        confluent-hub install --no-prompt confluentinc/kafka-connect-datagen:0.4.0
        /etc/confluent/docker/run

  
  ksqldb-server:
    image: confluentinc/cp-ksqldb-server:7.3.2
    hostname: ksqldb-server
    container_name: ksqldb-server
    ports:
      - "8088:8088"
    environment:
      KSQL_BOOTSTRAP_SERVERS: PLAINTEXT://kafka1:19092
      KSQL_LISTENERS: http://0.0.0.0:8088/
      KSQL_KSQL_SERVICE_ID: ksqldb-server_
    depends_on:
      - zoo1
      - kafka1

  postgresql:
    hostname: postgresql
    container_name: postgresql
    extends:
      service: postgresql
      file: conduktor.yml 
            
  conduktor-console:
    hostname: conduktor-console
    container_name: conduktor-console
    extends:
      service: conduktor-console
      file: conduktor.yml

volumes:
  pg_data: {}
  conduktor_data: {}  
 ```

### Настройки микросервисов
application.properties для Metrics Producer:
```
server.port=8008
spring.kafka.bootstrap-servers=localhost:9092  
spring.kafka.consumer.group-id=myGroup  
```
и Consumer:  
```
server.port=8000

spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=myGroup

spring.datasource.url=jdbc:postgresql://localhost:5432/metric
spring.datasource.username=user
spring.datasource.password=password

spring.jpa.hibernate.ddl-auto=update
```

## Инструкции по запуску
### Предварительные требования
- IntelliJ IDEA  
- Java 21  
- SpringBoot
- Spring Boot Test
- Spring Kafka
- Spring Kafka Test
- Springfox
- Spring Web
- PostgreSQL
- Lombok
- Docker

### Запуск Kafka
Запустите Zookeeper и Kafka Broker с помошью Docker.

### Запуск микросервисов
#### Metrics Producer:  

Перейдите в каталог проекта и выполните:

mvn spring-boot:run
#### Metrics Consumer:

Перейдите в каталог проекта и выполните:

mvn spring-boot:run

## Использование системы
### Отправка метрик
Используйте следующий HTTP запрос для отправки метрики в Metrics Producer:

POST /metrics
Content-Type: application/json
```
{
    "appName": "MyApp",
    "metricName": "CPU Usage",
    "metricVakue": 75.5,
    "numberOfMistakes": 2,
    "processTime": 123.45,
    "usingMemory": 256.78,
    "timestamp": "2024-09-03T17:44:00.000Z"
}
```

### Получение метрик
Используйте следующие эндпоинты Metrics Consumer для получения метрик:  

Получение всех метрик: GET /metrics  

Получение метрики по ID: GET /metrics/{id}  
## Заключениe
Эта документация должна помочь вам развернуть и использовать систему мониторинга на основе Kafka.
