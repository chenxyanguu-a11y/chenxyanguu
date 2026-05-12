package com.example.mallproduct.controller;

import com.example.mallcommon.core.Result;
import com.example.mallproduct.dto.ProductDto;
import com.example.mallproduct.es.ProductDoc;
import com.example.mallproduct.productVo.PageResult;
import com.example.mallproduct.service.ProductEsService;
import com.example.mallproduct.service.ProductSyncService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/product")
public class ProductSearchController {

    private final ProductEsService productEsService;
    private final ProductSyncService productSyncService;

    public ProductSearchController(ProductEsService productEsService, ProductSyncService productSyncService) {
        this.productEsService = productEsService;
        this.productSyncService = productSyncService;
    }

    @PostMapping("/search")
    public PageResult<ProductDoc> search(@RequestBody(required = false) ProductDto productDto) throws IOException {
        return productEsService.search(productDto);
    }

    @PostMapping("/sync")
    public Result<String> syncProductsToEs() throws IOException {
        long total = productSyncService.syncProductsToEs();
        return Result.success("同步成功，共同步 " + total + " 条商品数据");
    }
}
