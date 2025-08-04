package se.marcuskarlberg.ProductsMicroservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import se.marcuskarlberg.core.ProductCreatedEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

  public final static String TOPIC_NAME = "product-created-events-topic";

  @Value("${spring.kafka.producer.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${spring.kafka.producer.key-serializer}")
  private String keySerializer;

  @Value("${spring.kafka.producer.value-serializer}")
  private String valueSerializer;

  @Value("${spring.kafka.producer.acks}")
  private String acks;

  @Value("${spring.kafka.producer.properties.enable.idempotence}")
  private boolean idempotence;

  @Value("${spring.kafka.producer.properties.max.in.flight.requests.per.connection}")
  private int maxInFlightRequestsPerConnection;

  public Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
    // Idempotent
    props.put(ProducerConfig.ACKS_CONFIG, acks);
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, idempotence);
    props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInFlightRequestsPerConnection);

    return props;
  }

  @Bean
  ProducerFactory<String, ProductCreatedEvent> producerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  @Bean
  KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate() {
    return new KafkaTemplate<String, ProductCreatedEvent>(producerFactory());
  }

  @Bean
  public NewTopic createTopic() {
    return TopicBuilder.name(TOPIC_NAME)
      .partitions(3)
      .replicas(1)
      .configs(Map.of("min.insync.replicas", "2"))
      .build();
  }
}
