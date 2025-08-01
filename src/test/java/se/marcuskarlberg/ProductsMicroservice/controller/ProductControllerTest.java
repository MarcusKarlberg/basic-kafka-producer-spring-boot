package se.marcuskarlberg.ProductsMicroservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import se.marcuskarlberg.ProductsMicroservice.model.Product;
import se.marcuskarlberg.ProductsMicroservice.service.ProductService;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ProductService productService;

  @Test
  void shouldReturnProductIdWhenProductIsCreated() throws Exception {
    Product product = Product.builder().build();
    String productId = "product1";

    Mockito.when(productService.createProduct(any(Product.class))).thenReturn(productId);

    mockMvc.perform(post("/products")
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(product)))
      .andExpect(status().isOk())
      .andExpect(content().string(productId));
  }

  @Test
  void shouldReturn500WhenServiceFails() throws Exception {
    Product product = Product.builder().build();

    Mockito.when(productService.createProduct(any(Product.class)))
      .thenThrow(new RuntimeException("Kafka failure"));

    mockMvc.perform(post("/products")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(product)))
      .andExpect(status().isInternalServerError());
  }
}
