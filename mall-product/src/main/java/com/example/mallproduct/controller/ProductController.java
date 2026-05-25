package com.example.mallproduct.controller;

import com.example.mallcommon.core.Result;
import com.example.mallproduct.dto.ProductDto;
import com.example.mallproduct.dto.ProductUpdateDTO;
import com.example.mallproduct.entity.Product;
import com.example.mallproduct.es.ProductDoc;
import com.example.mallproduct.productVo.PageResult;
import com.example.mallproduct.service.ProductAddService;
import com.example.mallproduct.service.ProductEditService;
import com.example.mallproduct.service.ProductEsService;
import com.example.mallproduct.service.ProductSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductAddService productAddService;

    @Autowired
    private ProductEditService productEditService;

    @Autowired
    private ProductEsService productEsService;

    @Autowired
    private ProductSyncService productSyncService;

    @PostMapping("/add")
    public Result<Product> addProduct(@RequestBody Product product,
                                      @RequestHeader("X-User-Id") Long userId,
                                      @RequestHeader("X-Role") String role) throws IOException {
        System.out.println(userId+"11111"+role);
        Product savedProduct = productAddService.addProduct(product, userId, role);
        return Result.success(savedProduct);
    }

    @PutMapping("/edit/{id}")
    public Result<String> updateProduct(@PathVariable Long id,
                                        @RequestBody ProductUpdateDTO productUpdateDTO,
                                        @RequestHeader("X-User-Id") Long userId,
                                        @RequestHeader("X-Role") String role) {
        productEditService.updateProduct(id, productUpdateDTO, userId, role);
        return Result.success("update success");
    }

    @GetMapping("/edit/search")
    public Result<List<ProductDoc>> searchProductsForEdit(@RequestParam String productName,
                                                          @RequestHeader("X-User-Id") Long userId,
                                                          @RequestHeader("X-Role") String role) throws IOException {
        List<ProductDoc> products = productEditService.searchProductsForEdit(productName, userId, role);
        return Result.success(products);
    }

    @GetMapping("/edit/all")
    public Result<List<Product>> listAllProductsForEdit(@RequestHeader("X-Role") String role) {
        List<Product> products = productEditService.listAllProductsForEdit(role);
        return Result.success(products);
    }

    @PostMapping("/search")
    public PageResult<ProductDoc> search(@RequestBody(required = false) ProductDto productDto) throws IOException {
        return productEsService.search(productDto);
    }

    @PostMapping("/sync")
    public Result<String> syncProductsToEs() throws IOException {
        long total = productSyncService.syncProductsToEs();
        return Result.success("sync success, total: " + total);
    }
}
