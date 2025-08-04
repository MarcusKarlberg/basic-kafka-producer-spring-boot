package se.marcuskarlberg.ProductsMicroservice.service;

import se.marcuskarlberg.ProductsMicroservice.model.Product;

import java.util.concurrent.ExecutionException;

public interface ProductService {
  String createProduct(Product product) throws ExecutionException, InterruptedException;
}
