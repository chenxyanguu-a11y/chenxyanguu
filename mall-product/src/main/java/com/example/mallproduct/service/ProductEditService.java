package com.example.mallproduct.service;

import com.example.mallproduct.dto.ProductUpdateDTO;
import com.example.mallproduct.entity.Product;
import com.example.mallproduct.es.ProductDoc;

import java.io.IOException;
import java.util.List;

public interface ProductEditService {

    void updateProduct(Long id, ProductUpdateDTO productUpdateDTO, Long userId, String role);

    List<ProductDoc> searchProductsForEdit(String productName, Long userId, String role) throws IOException;

    List<Product> listAllProductsForEdit(String role);
}
