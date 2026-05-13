package com.example.mallproduct.controller;

import com.example.mallcommon.core.Result;
import com.example.mallproduct.dto.ProductDto;
import com.example.mallproduct.entity.Product;
import com.example.mallproduct.es.ProductDoc;
import com.example.mallproduct.productVo.PageResult;
import com.example.mallproduct.service.ProductAddService;
import com.example.mallproduct.service.ProductEsService;
import com.example.mallproduct.service.ProductSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductAddService productAddService;

    @Autowired
    private ProductEsService productEsService;

    @Autowired
    private ProductSyncService productSyncService;

    /**
     * 新增商品：保存商品到 MySQL，服务层默认设置审核通过，审核通过后同步到 Elasticsearch。
     */
    @PostMapping("/add")
    public Result<Product> addProduct(@RequestBody Product product) throws IOException {
        Product savedProduct = productAddService.addProduct(product);
        return Result.success(savedProduct);
    }

    /**
     * 搜索商品：从 Elasticsearch 中按关键词、分类、价格区间、排序和分页条件查询商品。
     */
    @PostMapping("/search")
    public PageResult<ProductDoc> search(@RequestBody(required = false) ProductDto productDto) throws IOException {
        return productEsService.search(productDto);
    }

    /**
     * 同步商品：后台管理员手动触发，将 MySQL 商品数据全量同步到 Elasticsearch。
     */
    @PostMapping("/sync")
    public Result<String> syncProductsToEs() throws IOException {
        long total = productSyncService.syncProductsToEs();
        return Result.success("同步成功，共同步 " + total + " 条商品数据");
    }
}
