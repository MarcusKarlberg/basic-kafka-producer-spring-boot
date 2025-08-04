package se.marcuskarlberg.ProductsMicroservice.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import se.marcuskarlberg.ProductsMicroservice.model.Product;
import se.marcuskarlberg.core.ProductCreatedEvent;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static se.marcuskarlberg.ProductsMicroservice.config.KafkaConfig.TOPIC_NAME;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
  private final static Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

  KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

  public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public String createProduct(Product product) throws ExecutionException, InterruptedException {
    String productId = UUID.randomUUID().toString();
    String messageId = UUID.randomUUID().toString();

    ProductCreatedEvent event = ProductCreatedEvent.builder()
      .productId(productId)
      .title(product.getTitle())
      .price(product.getPrice())
      .quantity(product.getQuantity())
      .build();

    ProducerRecord<String, ProductCreatedEvent> record = new ProducerRecord<>(TOPIC_NAME, productId, event);
    record.headers().add("messageId", messageId.getBytes());

    LOG.info("Publishing a ProductCreatedEvent...");
    LOG.info("Sending message to topic: {}", TOPIC_NAME);
    LOG.info("MessageId: {}", messageId);

    // Asynchronous
    SendResult<String, ProductCreatedEvent> result = kafkaTemplate.send(record).get();

    /**
    //Synchronous
    CompletableFuture<SendResult<String, ProductCreatedEvent>> future =
      kafkaTemplate.send(TOPIC_NAME, productId, event);

    future.whenComplete((result, e) -> {
      logger.info(String.format("Topic: %s -- Partition: %s -- Offset: %s",
        result.getRecordMetadata().topic(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset()));

      if (e != null) {
        logger.error("Unable to send product event", e.getMessage());
      } else {
        logger.info("Successfully sent product event - async");
      }
    });
     */

    LOG.info("Sent a ProductCreatedEvent");
    LOG.info("Partition: {}", result.getProducerRecord().partition());
    LOG.info("Offset: {}", result.getRecordMetadata().offset());
    LOG.info("Topic: {}", result.getProducerRecord().topic());

    return productId;
  }
}
