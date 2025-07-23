package se.marcuskarlberg.ProductsMicroservice;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
public class KafkaConfig {

  final static String TOPIC_NAME = "product-created-events-topic";

  @Bean
  public NewTopic createTopic() {
    return TopicBuilder.name(TOPIC_NAME)
      .partitions(3)
      .replicas(1)
      .configs(Map.of("min.insync.replicas", "2"))
      .build();
  }
}
