package com.example.mallproduct.service;

import com.example.mallproduct.dto.ProductDto;
import com.example.mallproduct.es.ProductDoc;
import com.example.mallproduct.productVo.PageResult;

import java.io.IOException;

public interface ProductEsService {
    PageResult<ProductDoc> search(ProductDto productDto) throws IOException;
}
