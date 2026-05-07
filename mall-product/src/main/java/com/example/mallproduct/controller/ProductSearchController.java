package com.example.mallproduct.controller;

import com.example.mallproduct.dto.ProductDto;
import com.example.mallproduct.es.ProductDoc;
import com.example.mallproduct.productVo.PageResult;
import com.example.mallproduct.service.ProductEsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/product")
public class ProductSearchController {

    @Autowired
    private  ProductEsService productEsService;

    @PostMapping("/search")
    public PageResult<ProductDoc> search(@RequestBody(required = false) ProductDto productDto) throws IOException {
        return productEsService.search(productDto);
    }
}
