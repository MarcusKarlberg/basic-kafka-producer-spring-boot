package se.marcuskarlberg.ProductsMicroservice.exception;

import lombok.*;

import java.util.Date;

@Builder
public record ErrorMsg(Date timestamp, String message, String details) {
}
