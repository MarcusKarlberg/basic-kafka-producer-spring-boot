package se.marcuskarlberg.ProductsMicroservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import se.marcuskarlberg.ProductsMicroservice.model.Product;
import se.marcuskarlberg.core.ProductCreatedEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static se.marcuskarlberg.ProductsMicroservice.config.KafkaConfig.TOPIC_NAME;

@Service
public class ProductServiceImpl implements ProductService {
  private final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

  KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

  public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public String createProduct(Product product) {
    String productId = UUID.randomUUID().toString();

    ProductCreatedEvent event = ProductCreatedEvent.builder()
      .productId(productId)
      .title(product.getTitle())
      .price(product.getPrice())
      .quantity(product.getQuantity())
      .build();

    // Asynchronous
    kafkaTemplate.send(TOPIC_NAME, productId, event);

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

    logger.info(String.format("Successfully sent product event ID: %s - sync", productId));
    return productId;
  }
}
