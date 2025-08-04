package se.marcuskarlberg.ProductsMicroservice.service;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import se.marcuskarlberg.ProductsMicroservice.model.Product;
import se.marcuskarlberg.core.ProductCreatedEvent;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 3, count = 3, controlledShutdown = true)
@SpringBootTest(properties = "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class ProductServiceIntegrationTest {

  @Autowired
  private ProductService productService;

  @Autowired
  private EmbeddedKafkaBroker embeddedKafkaBroker;

  @Autowired
  private Environment environment;

  private KafkaMessageListenerContainer<String, ProductCreatedEvent> container;
  private BlockingQueue<ConsumerRecord<String, ProductCreatedEvent>> records;

  @BeforeAll
  void setup() {
    DefaultKafkaConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(getConsumerProperties());

    ContainerProperties containerProperties = new ContainerProperties(environment.getProperty("product-created-events-topic-name"));
    container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
    records = new LinkedBlockingQueue<>();
    container.setupMessageListener((MessageListener<String, ProductCreatedEvent>) records::add);
    container.start();
    ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
  }

  @Test
  //testCreateProduct_whenGivenValidProductData_successfullySendsKafkaMessage
  void createProductTest() throws Exception {
    Product product = Product.builder()
      .title("iphone 16")
      .price(new BigDecimal(300))
      .quantity(1)
      .build();

    productService.createProduct(product);

    // Assertions

    // Read from message queue - wating 3 seconds
    ConsumerRecord<String, ProductCreatedEvent> message = records.poll(3000, TimeUnit.MILLISECONDS);
    assertNotNull(message);
    assertNotNull(message.key());

    ProductCreatedEvent productCreatedEvent = (ProductCreatedEvent) message.value();
    assertEquals(product.getQuantity(), productCreatedEvent.getQuantity());
    assertEquals(product.getPrice(), productCreatedEvent.getPrice());
    assertEquals(product.getTitle(), productCreatedEvent.getTitle());
  }

  private Map<String, Object> getConsumerProperties() {
    return Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
      embeddedKafkaBroker.getBrokersAsString(),
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
      StringDeserializer.class,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
      ErrorHandlingDeserializer.class,
      ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS,
      JsonDeserializer.class,
      ConsumerConfig.GROUP_ID_CONFIG,
      environment.getProperty("spring.kafka.consumer.group-id"),
      JsonDeserializer.TRUSTED_PACKAGES,
      environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"),
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, environment.getProperty("spring.kafka.consumer.auto-offset-reset")
    );
  }

  @AfterAll
  void tearDown() {
    container.stop();
  }
}
