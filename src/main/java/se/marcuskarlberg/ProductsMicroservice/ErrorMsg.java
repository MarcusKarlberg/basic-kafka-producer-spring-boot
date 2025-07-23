package se.marcuskarlberg.ProductsMicroservice;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Date;

@Builder
@AllArgsConstructor
public class ErrorMsg {
  private final Date timestamp;
  private final String message;
  private final String details;
}
