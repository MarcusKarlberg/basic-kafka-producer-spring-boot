package se.marcuskarlberg.ProductsMicroservice.service;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.*;
import se.marcuskarlberg.ProductsMicroservice.model.Product;
import se.marcuskarlberg.core.ProductCreatedEvent;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static se.marcuskarlberg.ProductsMicroservice.config.KafkaConfig.TOPIC_NAME;

public class ProductServiceImplTest {
  @Mock
  KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

  @InjectMocks
  ProductServiceImpl productService;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void createProduct_shouldSendEventAndReturnProductId() throws Exception {
    Product product = Product.builder()
      .title("Test Product")
      .price(BigDecimal.valueOf(99.99))
      .quantity(5)
      .build();

    // Mock the send() method to return a completed future
    CompletableFuture<SendResult<String, ProductCreatedEvent>> future = CompletableFuture.completedFuture(mock(SendResult.class));
    when(kafkaTemplate.send(anyString(), anyString(), any(ProductCreatedEvent.class))).thenReturn(future);

    String productId = productService.createProduct(product);

    assertNotNull(productId);

    // Verify send was called twice (once async, once sync)
    verify(kafkaTemplate, times(2)).send(eq(TOPIC_NAME), eq(productId), any(ProductCreatedEvent.class));

    // Capture the event and verify fields
    ArgumentCaptor<ProductCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ProductCreatedEvent.class);
    verify(kafkaTemplate, times(2)).send(anyString(), anyString(), eventCaptor.capture());

    for (ProductCreatedEvent sentEvent : eventCaptor.getAllValues()) {
      assertEquals(productId, sentEvent.getProductId());
      assertEquals(product.getTitle(), sentEvent.getTitle());
      assertEquals(product.getPrice(), sentEvent.getPrice());
      assertEquals(product.getQuantity(), sentEvent.getQuantity());
    }
  }
}
