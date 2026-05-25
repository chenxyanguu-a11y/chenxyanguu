package com.example.mallproduct.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.example.mallproduct.entity.Product;
import com.example.mallproduct.es.ProductDoc;
import com.example.mallproduct.exception.EsExcetpionHandler;
import com.example.mallproduct.mapper.ProductMapper;
import com.example.mallproduct.service.ProductSyncService;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ProductSyncServiceImpl implements ProductSyncService {

    private static final String PRODUCT_INDEX = "product";
    private static final int BATCH_SIZE = 1000;

    private final ProductMapper productMapper;
    private final ElasticsearchClient elasticsearchClient;
    private final ElasticsearchOperations elasticsearchOperations;

    public ProductSyncServiceImpl(ProductMapper productMapper,
                                  ElasticsearchClient elasticsearchClient,
                                  ElasticsearchOperations elasticsearchOperations) {
        this.productMapper = productMapper;
        this.elasticsearchClient = elasticsearchClient;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public long syncProductsToEs() throws IOException {
        recreateProductIndex();

        long total = 0;
        long lastId = 0;

        while (true) {
            List<Product> products = productMapper.selectNextBatch(lastId, BATCH_SIZE);

            if (products.isEmpty()) {
                return total;
            }

            bulkSave(products);
            total += products.size();
            lastId = products.get(products.size() - 1).getId();

            if (products.size() < BATCH_SIZE) {
                return total;
            }
        }
    }

    private void recreateProductIndex() {
        IndexOperations indexOperations = elasticsearchOperations.indexOps(ProductDoc.class);
        if (indexOperations.exists()) {
            indexOperations.delete();
        }
        indexOperations.createWithMapping();
    }

    private void bulkSave(List<Product> products) throws IOException {
        BulkResponse response = elasticsearchClient.bulk(bulk -> {
            bulk.index(PRODUCT_INDEX);
            for (Product product : products) {
                ProductDoc productDoc = toProductDoc(product);
                bulk.operations(operation -> operation.index(index -> index
                        .id(String.valueOf(productDoc.getId()))
                        .document(productDoc)));
            }
            return bulk;
        });

        if (response.errors()) {
            String reason = response.items().stream()
                    .filter(item -> item.error() != null)
                    .findFirst()
                    .map(item -> item.error().reason())
                    .orElse("unknown error");
            throw new EsExcetpionHandler(400,"同步商品数据到 Elasticsearch 失败: " + reason);
        }
    }

    private ProductDoc toProductDoc(Product product) {
        System.out.println(product.getAvailableStock());
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
