package com.example.mallproduct.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.mallproduct.dto.ProductUpdateDTO;
import com.example.mallproduct.entity.Product;
import com.example.mallproduct.es.ProductDoc;
import com.example.mallproduct.exception.BusinessException;
import com.example.mallproduct.mapper.ProductEditMapper;
import com.example.mallproduct.service.ProductEditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class ProductEditServiceImpl implements ProductEditService {

    private static final String PRODUCT_INDEX = "product";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MERCHANT = "MERCHANT";
    private static final int STATUS_ON_SALE = 1;
    private static final int AUDIT_WAITING = 0;
    private static final int AUDIT_PASSED = 1;
    private static final int NOT_DELETED = 0;
    private static final int EDIT_SEARCH_SIZE = 20;

    @Autowired
    private ProductEditMapper productEditMapper;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long id, ProductUpdateDTO productUpdateDTO, Long userId, String role) {
        Product product = productEditMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        boolean admin = ROLE_ADMIN.equals(role);
        boolean merchant = ROLE_MERCHANT.equals(role);
        Integer status;
        Integer auditStatus;

        if (admin) {
            status = STATUS_ON_SALE;
            auditStatus = AUDIT_PASSED;
        } else if (merchant && product.getMerchantId() != null && product.getMerchantId().equals(userId)) {
            status = STATUS_ON_SALE;
            auditStatus = AUDIT_WAITING;
        } else {
            throw new BusinessException(403, "无权限修改商品");
        }

        productEditMapper.updateProductById(id, productUpdateDTO, status, auditStatus);
        Product updatedProduct = productEditMapper.selectById(id);
        registerEsSyncAfterCommit(id, updatedProduct);
    }

    @Override
    public List<ProductDoc> searchProductsForEdit(String productName, Long userId, String role) throws IOException {
        if (!StringUtils.hasText(productName)) {
            throw new BusinessException(400, "商品名称不能为空");
        }

        if (ROLE_ADMIN.equals(role)) {
            return searchProductsFromEs(productName.trim(), null);
        }
        if (ROLE_MERCHANT.equals(role)) {
            return searchProductsFromEs(productName.trim(), userId);
        }
        throw new BusinessException(401, "无权限查询商品");
    }

    @Override
    public List<Product> listAllProductsForEdit(String role) {
        if (!ROLE_ADMIN.equals(role)) {
            throw new BusinessException(401, "无权限查询全部商品");
        }
        return productEditMapper.selectAllProducts();
    }

    private void registerEsSyncAfterCommit(Long id, Product product) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    deleteProductFromEs(id);
                    if (shouldSyncToEs(product)) {
                        syncProductToEs(product);
                    }
                } catch (IOException e) {
                    throw new BusinessException(500, "同步商品到 Elasticsearch 失败");
                }
            }
        });
    }

    private boolean shouldSyncToEs(Product product) {
        return product != null
                && Integer.valueOf(STATUS_ON_SALE).equals(product.getStatus())
                && Integer.valueOf(AUDIT_PASSED).equals(product.getAuditStatus())
                && Integer.valueOf(NOT_DELETED).equals(product.getDeleted());
    }

    private List<ProductDoc> searchProductsFromEs(String productName, Long merchantId) throws IOException {
        SearchResponse<ProductDoc> response = elasticsearchClient.search(request -> request
                .index(PRODUCT_INDEX)
                .size(EDIT_SEARCH_SIZE)
                .query(query -> query.bool(bool -> {
                    bool.must(must -> must.match(match -> match
                            .field("productName")
                            .query(productName)));
                    bool.filter(termQuery("status", STATUS_ON_SALE));
                    bool.filter(termQuery("auditStatus", AUDIT_PASSED));
                    bool.filter(termQuery("deleted", NOT_DELETED));
                    if (merchantId != null) {
                        bool.filter(termQuery("merchantId", merchantId));
                    }
                    return bool;
                })), ProductDoc.class);

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .toList();
    }

    private Query termQuery(String field, long value) {
        return Query.of(query -> query.term(term -> term
                .field(field)
                .value(value)));
    }

    private void syncProductToEs(Product product) throws IOException {
        ProductDoc productDoc = toProductDoc(product);
        elasticsearchClient.index(index -> index
                .index(PRODUCT_INDEX)
                .id(String.valueOf(productDoc.getId()))
                .document(productDoc));
    }

    private void deleteProductFromEs(Long id) throws IOException {
        try {
            elasticsearchClient.delete(delete -> delete
                    .index(PRODUCT_INDEX)
                    .id(String.valueOf(id)));
        } catch (ElasticsearchException e) {
            if (e.status() != 404) {
                throw e;
            }
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
