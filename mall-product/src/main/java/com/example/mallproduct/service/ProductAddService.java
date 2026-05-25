package com.example.mallproduct.service;

import com.example.mallproduct.entity.Product;

import java.io.IOException;

public interface ProductAddService {

    Product addProduct(Product product, Long userId, String role) throws IOException;
}
