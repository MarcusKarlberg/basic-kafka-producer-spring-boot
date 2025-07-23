package se.marcuskarlberg.ProductsMicroservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;

@RestController
@RequestMapping("/products")
public class ProductController {

  ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @PostMapping
  public ResponseEntity<Object> createProduct(@RequestBody Product product) {
    String productId;
    try {
      productId = productService.createProduct(product);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorMsg.builder()
        .timestamp(Date.from(Instant.now()))
        .message("Unable to send product event")
        .details("/products")
        .build());
    }
    return ResponseEntity.ok().body(productId);
  }
}
