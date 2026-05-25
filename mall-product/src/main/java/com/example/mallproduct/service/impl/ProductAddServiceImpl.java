package com.example.mallproduct.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.example.mallproduct.entity.Product;
import com.example.mallproduct.es.ProductDoc;
import com.example.mallproduct.exception.BusinessException;
import com.example.mallproduct.mapper.ProductMapper;
import com.example.mallproduct.service.ProductAddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
public class ProductAddServiceImpl implements ProductAddService {

    private static final String PRODUCT_INDEX = "product";
    private static final String ROLE_MERCHANT = "MERCHANT";
    private static final int STATUS_ON_SALE = 1;
    private static final int AUDIT_PASSED = 1;
    private static final int NOT_DELETED = 0;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product addProduct(Product product, Long userId, String role) throws IOException {
        if (!ROLE_MERCHANT.equals(role)) {
            throw new BusinessException(403, "无权限新增商品");
        }

        product.setMerchantId(userId);
        product.setAuditStatus(AUDIT_PASSED);
        if (product.getStatus() == null) {
            product.setStatus(STATUS_ON_SALE);
        }
        if (product.getDeleted() == null) {
            product.setDeleted(NOT_DELETED);
        }

        productMapper.insertProduct(product);
        Product savedProduct = productMapper.selectById(product.getId());

        if (savedProduct != null && AUDIT_PASSED == savedProduct.getAuditStatus()) {
            saveProductToEs(savedProduct);
        }

        return savedProduct;
    }

    private void saveProductToEs(Product product) throws IOException {
        ensureProductIndexExists();
        ProductDoc productDoc = toProductDoc(product);
        elasticsearchClient.index(index -> index
                .index(PRODUCT_INDEX)
                .id(String.valueOf(productDoc.getId()))
                .document(productDoc));
    }

    private void ensureProductIndexExists() {
        IndexOperations indexOperations = elasticsearchOperations.indexOps(ProductDoc.class);
        if (!indexOperations.exists()) {
            indexOperations.createWithMapping();
        }
    }

    private ProductDoc toProductDoc(Product product) {
        ProductDoc productDoc = new ProductDoc();
        productDoc.setId(product.getId());
        productDoc.setMerchantId(product.getMerchantId());
        productDoc.setCategoryId(product.getCategoryId());
        productDoc.setProductName(product.getProductName());
        productDoc.setProductDesc(product.getProductDesc());
        productDoc.setPrice(product.getPrice());
        productDoc.setMainImage(product.getMainImage());
        productDoc.setAvailableStock(product.getAvailableStock());
        productDoc.setStatus(product.getStatus());
        productDoc.setAuditStatus(product.getAuditStatus());
        productDoc.setDeleted(product.getDeleted());
        productDoc.setCreateTime(product.getCreateTime());
        return productDoc;
    }
}
