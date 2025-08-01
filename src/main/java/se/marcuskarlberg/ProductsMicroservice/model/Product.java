package se.marcuskarlberg.ProductsMicroservice.model;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Product {
  private String title;
  private BigDecimal price;
  private Integer quantity;
}
